package dev.maicra.pickclimber.rules.network;

import dev.maicra.pickclimber.ModItems;
import dev.maicra.pickclimber.rules.ClimbingRuleBookCodec;
import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.ClimbingRuleBookValidationResult;
import dev.maicra.pickclimber.rules.ClimbingRuleBookValidator;
import dev.maicra.pickclimber.rules.ClimbingRulesClientUi;
import dev.maicra.pickclimber.rules.ClimbingRulesProfile;
import dev.maicra.pickclimber.rules.ClimbingRulesService;
import dev.maicra.pickclimber.rules.MapmakerPermissions;
import dev.maicra.pickclimber.rules.WorldRulesSnapshot;
import dev.maicra.pickclimber.rules.block.ClimbingRulesTableBlockEntity;
import dev.maicra.pickclimber.rules.item.ClimbingRuleBookData;
import dev.maicra.pickclimber.rules.menu.ClimbingRulesTableMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Optional;
import java.util.function.BiConsumer;

public final class ClimbingRulesTableNetworking {
    private ClimbingRulesTableNetworking() {
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(CreateRuleBookPayload.TYPE, CreateRuleBookPayload.STREAM_CODEC,
                ClimbingRulesTableNetworking::handleCreate);
        registrar.playToServer(ImportRuleBookPayload.TYPE, ImportRuleBookPayload.STREAM_CODEC,
                ClimbingRulesTableNetworking::handleImport);
        registrar.playToServer(ImportCurrentRulesPayload.TYPE, ImportCurrentRulesPayload.STREAM_CODEC,
                ClimbingRulesTableNetworking::handleImportCurrent);
        registrar.playToServer(DuplicateRuleBookPayload.TYPE, DuplicateRuleBookPayload.STREAM_CODEC,
                ClimbingRulesTableNetworking::handleDuplicate);
        registrar.playToServer(EjectRuleBookPayload.TYPE, EjectRuleBookPayload.STREAM_CODEC,
                ClimbingRulesTableNetworking::handleEject);
        registrar.playToServer(RestoreWorldDefaultsPayload.TYPE, RestoreWorldDefaultsPayload.STREAM_CODEC,
                ClimbingRulesTableNetworking::handleRestore);
        registrar.playToServer(OpenRulesEditorRequestPayload.TYPE, OpenRulesEditorRequestPayload.STREAM_CODEC,
                ClimbingRulesTableNetworking::handleOpenEditor);
        registrar.playToServer(UpdateRuleBookPayload.TYPE, UpdateRuleBookPayload.STREAM_CODEC,
                ClimbingRulesTableNetworking::handleUpdateRuleBook);
        registrar.playToClient(OpenRulesEditorPayload.TYPE, OpenRulesEditorPayload.STREAM_CODEC,
                ClimbingRulesTableNetworking::handleEditorPayload);
        registrar.playToClient(RulesActionResultPayload.TYPE, RulesActionResultPayload.STREAM_CODEC,
                ClimbingRulesTableNetworking::handleResult);
    }

    private static void handleCreate(CreateRuleBookPayload payload, IPayloadContext context) {
        withTable(context, payload.position(), (player, table) -> {
            if (!canStartCreation(player, table)) {
                return;
            }
            DyeColor cover = dyeColor(table).orElse(DyeColor.WHITE);
            ClimbingRulesProfile profile = ClimbingRulesProfile.defaults(payload.profileName());
            ClimbingRuleBookDefinition definition = new ClimbingRuleBookDefinition(
                    ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION,
                    payload.profileName(),
                    cover,
                    profile,
                    dev.maicra.pickclimber.rules.RuleBookActivationMode.PERMANENT,
                    dev.maicra.pickclimber.rules.RuleBookScope.WORLD,
                    0
            );
            openDraft(
                    player,
                    payload.position(),
                    RulesEditorSessionStore.Operation.CREATE,
                    Optional.empty(),
                    definition
            );
        });
    }

    private static void handleImport(ImportRuleBookPayload payload, IPayloadContext context) {
        withTable(context, payload.position(), (player, table) -> {
            if (!canStartCreation(player, table)) {
                return;
            }
            if (!RuleBookNetworkLimits.accepts(payload.profileTag())) {
                result(player, false, "message.pickclimber.rules.network_payload_too_large");
                return;
            }
            Optional<ClimbingRuleBookDefinition> decoded = ClimbingRuleBookCodec.decodeFromNbt(payload.profileTag())
                    .result();
            if (decoded.isEmpty()) {
                result(player, false, "message.pickclimber.rules.json_invalid_profile");
                return;
            }
            ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(decoded.get());
            if (!validation.valid()) {
                result(player, false, validationMessageKey(validation));
                return;
            }
            openDraft(
                    player,
                    payload.position(),
                    RulesEditorSessionStore.Operation.IMPORT,
                    Optional.empty(),
                    validation.normalizedDefinition()
            );
        });
    }

    private static void handleImportCurrent(ImportCurrentRulesPayload payload, IPayloadContext context) {
        withTable(context, payload.position(), (player, table) -> {
            if (!canStartCreation(player, table)) {
                return;
            }
            WorldRulesSnapshot snapshot = ClimbingRulesService.snapshot(player.serverLevel().getServer());
            Optional<ClimbingRuleBookDefinition> current = snapshot.effectiveDefinition();
            if (current.isEmpty()) {
                result(player, false, "message.pickclimber.rules.world_defaults_no_import");
                return;
            }
            DyeColor cover = dyeColor(table).orElse(DyeColor.WHITE);
            ClimbingRuleBookDefinition source = current.get();
            ClimbingRuleBookDefinition definition = new ClimbingRuleBookDefinition(
                    source.formatVersion(),
                    source.bookName(),
                    cover,
                    source.profile(),
                    source.activationMode(),
                    source.scope(),
                    source.durationSeconds()
            );
            openDraft(
                    player,
                    payload.position(),
                    RulesEditorSessionStore.Operation.IMPORT,
                    Optional.empty(),
                    definition
            );
        });
    }


    private static void handleDuplicate(DuplicateRuleBookPayload payload, IPayloadContext context) {
        withTable(context, payload.position(), (player, table) -> {
            int copies = payload.copies();
            if (copies < 1 || copies > 64) {
                result(player, false, "message.pickclimber.rules.invalid_copy_count");
                return;
            }
            ItemStack sourceStack = table.getItem(ClimbingRulesTableBlockEntity.RULE_BOOK_SLOT);
            Optional<ClimbingRuleBookDefinition> source = ClimbingRuleBookData.readDefinitionValidated(sourceStack);
            if (source.isEmpty()) {
                result(player, false, "message.pickclimber.rules.valid_book_required");
                return;
            }
            ItemStack material = table.getItem(ClimbingRulesTableBlockEntity.MATERIAL_BOOK_SLOT);
            if (!material.is(Items.BOOK) || material.getCount() < copies) {
                result(player, false, "message.pickclimber.rules.duplicate_books_required");
                return;
            }

            DyeColor cover = dyeColor(table).orElse(source.get().coverColor());
            boolean recolor = cover != source.get().coverColor();
            if (recolor) {
                ItemStack dye = table.getItem(ClimbingRulesTableBlockEntity.DYE_SLOT);
                if (!hasMatchingDye(table, cover) || dye.getCount() < copies) {
                    result(player, false, "message.pickclimber.rules.duplicate_dye_required");
                    return;
                }
            }

            ClimbingRuleBookDefinition duplicate = withCover(source.get(), cover);
            ItemStack output = ClimbingRuleBookData.create(duplicate);
            if (output.isEmpty()) {
                result(player, false, "message.pickclimber.rules.invalid_profile");
                return;
            }
            output.setCount(copies);
            material.shrink(copies);
            if (recolor) {
                table.getItem(ClimbingRulesTableBlockEntity.DYE_SLOT).shrink(copies);
            }
            table.setChanged();
            if (!player.getInventory().add(output)) {
                player.drop(output, false);
            }
            result(player, true, "message.pickclimber.rules.duplicated");
        });
    }

    private static void handleEject(EjectRuleBookPayload payload, IPayloadContext context) {
        withTable(context, payload.position(), (player, table) -> {
            ItemStack stack = table.removeItemNoUpdate(ClimbingRulesTableBlockEntity.RULE_BOOK_SLOT);
            if (stack.isEmpty()) {
                return;
            }
            RulesEditorSessionStore.invalidate(player);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            table.setChanged();
        });
    }

    private static void handleRestore(RestoreWorldDefaultsPayload payload, IPayloadContext context) {
        withTable(context, payload.position(), (player, table) -> {
            ClimbingRulesService.restoreDefaults(player.serverLevel().getServer());
            result(player, true, "message.pickclimber.rules.restored");
        });
    }

    private static void handleOpenEditor(OpenRulesEditorRequestPayload payload, IPayloadContext context) {
        withTable(context, payload.position(), (player, table) -> {
            ItemStack ruleBook = table.getItem(ClimbingRulesTableBlockEntity.RULE_BOOK_SLOT);
            Optional<ClimbingRuleBookDefinition> source = ClimbingRuleBookData.readDefinitionValidated(ruleBook);
            if (source.isEmpty()) {
                result(player, false, "message.pickclimber.rules.valid_book_required");
                return;
            }
            DyeColor draftCover = dyeColor(table).orElse(source.get().coverColor());
            ClimbingRuleBookDefinition draft = withCover(source.get(), draftCover);
            openDraft(player, payload.position(), RulesEditorSessionStore.Operation.EDIT, source, draft);
        });
    }

    private static void handleUpdateRuleBook(UpdateRuleBookPayload payload, IPayloadContext context) {
        Optional<TableAccess> access = tableAccess(context, payload.position(), false);
        if (access.isEmpty()) {
            return;
        }

        ServerPlayer player = access.get().player();
        ClimbingRulesTableBlockEntity table = access.get().table();
        Optional<RulesEditorSessionStore.Session> sessionOptional = RulesEditorSessionStore.get(player);
        if (sessionOptional.isEmpty() || !sessionOptional.get().matchesLocation(player, payload.position())) {
            RulesEditorSessionStore.invalidate(player);
            result(player, false, "message.pickclimber.rules.editor_session_expired");
            return;
        }
        RulesEditorSessionStore.Session session = sessionOptional.get();
        if (payload.sessionToken() != session.token()) {
            result(player, false, "message.pickclimber.rules.editor_session_expired");
            return;
        }

        if (!RuleBookNetworkLimits.accepts(payload.definitionTag())) {
            result(player, false, "message.pickclimber.rules.network_payload_too_large");
            return;
        }
        Optional<ClimbingRuleBookDefinition> decoded = ClimbingRuleBookCodec.decodeFromNbt(payload.definitionTag())
                .result();
        if (decoded.isEmpty()) {
            result(player, false, "message.pickclimber.rules.invalid_profile");
            return;
        }
        ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(decoded.get());
        if (!validation.valid()) {
            result(player, false, validationMessageKey(validation));
            return;
        }
        ClimbingRuleBookDefinition target = validation.normalizedDefinition();
        if (target.coverColor() != session.allowedCoverColor()) {
            result(player, false, "message.pickclimber.rules.dye_changed_during_edit");
            return;
        }

        if (session.operation() == RulesEditorSessionStore.Operation.EDIT) {
            saveEdit(player, table, payload.position(), session, target);
            return;
        }
        saveCreation(player, table, payload.position(), session, target);
    }

    private static void saveEdit(
            ServerPlayer player,
            ClimbingRulesTableBlockEntity table,
            BlockPos position,
            RulesEditorSessionStore.Session session,
            ClimbingRuleBookDefinition target
    ) {
        ItemStack ruleBook = table.getItem(ClimbingRulesTableBlockEntity.RULE_BOOK_SLOT);
        Optional<ClimbingRuleBookDefinition> current = ClimbingRuleBookData.readDefinitionValidated(ruleBook);
        if (!ruleBook.is(ModItems.CLIMBING_RULE_BOOK.get())
                || current.isEmpty()
                || !session.matchesSource(current.get())) {
            result(player, false, "message.pickclimber.rules.stale_book");
            if (current.isPresent()) {
                openEditFromTable(player, table, position, current.get());
            } else {
                RulesEditorSessionStore.invalidate(player);
            }
            return;
        }

        boolean recolor = target.coverColor() != current.get().coverColor();
        if (recolor && !hasMatchingDye(table, target.coverColor())) {
            result(player, false, "message.pickclimber.rules.matching_dye_required");
            return;
        }
        if (!ClimbingRuleBookData.write(ruleBook, target)) {
            result(player, false, "message.pickclimber.rules.invalid_profile");
            return;
        }
        if (recolor) {
            table.getItem(ClimbingRulesTableBlockEntity.DYE_SLOT).shrink(1);
        }
        table.setChanged();
        openEditFromTable(player, table, position, target);
        result(player, true, "message.pickclimber.rules.book_saved");
    }

    private static void saveCreation(
            ServerPlayer player,
            ClimbingRulesTableBlockEntity table,
            BlockPos position,
            RulesEditorSessionStore.Session session,
            ClimbingRuleBookDefinition target
    ) {
        if (!table.getItem(ClimbingRulesTableBlockEntity.RULE_BOOK_SLOT).isEmpty()) {
            result(player, false, "message.pickclimber.rules.rule_book_slot_occupied");
            return;
        }
        ItemStack material = table.getItem(ClimbingRulesTableBlockEntity.MATERIAL_BOOK_SLOT);
        if (!material.is(Items.BOOK)) {
            result(player, false, "message.pickclimber.rules.book_required");
            return;
        }
        boolean consumeDye = target.coverColor() != DyeColor.WHITE;
        if (consumeDye && !hasMatchingDye(table, target.coverColor())) {
            result(player, false, "message.pickclimber.rules.matching_dye_required");
            return;
        }

        ItemStack ruleBook = ClimbingRuleBookData.create(target);
        if (ruleBook.isEmpty()) {
            result(player, false, "message.pickclimber.rules.invalid_profile");
            return;
        }
        material.shrink(1);
        if (consumeDye) {
            table.getItem(ClimbingRulesTableBlockEntity.DYE_SLOT).shrink(1);
        }
        table.setItem(ClimbingRulesTableBlockEntity.RULE_BOOK_SLOT, ruleBook);
        table.setChanged();
        openEditFromTable(player, table, position, target);
        result(player, true, "message.pickclimber.rules.book_created");
    }

    private static boolean canStartCreation(ServerPlayer player, ClimbingRulesTableBlockEntity table) {
        if (!table.getItem(ClimbingRulesTableBlockEntity.RULE_BOOK_SLOT).isEmpty()) {
            result(player, false, "message.pickclimber.rules.rule_book_slot_occupied");
            return false;
        }
        if (!table.getItem(ClimbingRulesTableBlockEntity.MATERIAL_BOOK_SLOT).is(Items.BOOK)) {
            result(player, false, "message.pickclimber.rules.book_required");
            return false;
        }
        return true;
    }

    private static void openEditFromTable(
            ServerPlayer player,
            ClimbingRulesTableBlockEntity table,
            BlockPos position,
            ClimbingRuleBookDefinition source
    ) {
        DyeColor draftCover = dyeColor(table).orElse(source.coverColor());
        openDraft(
                player,
                position,
                RulesEditorSessionStore.Operation.EDIT,
                Optional.of(source),
                withCover(source, draftCover)
        );
    }

    private static void openDraft(
            ServerPlayer player,
            BlockPos position,
            RulesEditorSessionStore.Operation operation,
            Optional<ClimbingRuleBookDefinition> source,
            ClimbingRuleBookDefinition draft
    ) {
        ClimbingRuleBookValidationResult validation = ClimbingRuleBookValidator.validateAndNormalize(draft);
        if (!validation.valid()) {
            result(player, false, validationMessageKey(validation));
            return;
        }
        ClimbingRuleBookDefinition normalized = validation.normalizedDefinition();
        Optional<CompoundTag> encoded = ClimbingRuleBookCodec.encodeToNbt(normalized).result();
        if (encoded.isEmpty()) {
            result(player, false, "message.pickclimber.rules.invalid_profile");
            return;
        }
        if (!RuleBookNetworkLimits.accepts(encoded.get())) {
            result(player, false, "message.pickclimber.rules.network_payload_too_large");
            return;
        }
        int token = RulesEditorSessionStore.open(
                player,
                position,
                operation,
                source,
                normalized.coverColor()
        );
        PacketDistributor.sendToPlayer(player, new OpenRulesEditorPayload(position, token, encoded.get()));
    }

    private static void handleEditorPayload(OpenRulesEditorPayload payload, IPayloadContext context) {
        if (!RuleBookNetworkLimits.accepts(payload.definitionTag())) {
            context.player().displayClientMessage(
                    Component.translatable("message.pickclimber.rules.network_payload_too_large"),
                    true
            );
            return;
        }
        ClimbingRuleBookCodec.decodeFromNbt(payload.definitionTag()).result().ifPresent(definition ->
                ClimbingRulesClientUi.openEditor(payload.position(), payload.sessionToken(), definition)
        );
    }

    private static void handleResult(RulesActionResultPayload payload, IPayloadContext context) {
        ChatFormatting color = payload.success() ? ChatFormatting.GREEN : ChatFormatting.RED;
        context.player().displayClientMessage(Component.translatable(payload.messageKey()).withStyle(color), true);
    }

    private static Optional<DyeColor> dyeColor(ClimbingRulesTableBlockEntity table) {
        ItemStack dye = table.getItem(ClimbingRulesTableBlockEntity.DYE_SLOT);
        return dye.getItem() instanceof DyeItem dyeItem ? Optional.of(dyeItem.getDyeColor()) : Optional.empty();
    }

    private static boolean hasMatchingDye(ClimbingRulesTableBlockEntity table, DyeColor color) {
        return dyeColor(table).filter(found -> found == color).isPresent();
    }

    private static ClimbingRuleBookDefinition withCover(
            ClimbingRuleBookDefinition definition,
            DyeColor cover
    ) {
        return new ClimbingRuleBookDefinition(
                definition.formatVersion(),
                definition.bookName(),
                cover,
                definition.profile(),
                definition.activationMode(),
                definition.scope(),
                definition.durationSeconds()
        );
    }

    private static String validationMessageKey(ClimbingRuleBookValidationResult validation) {
        boolean invalidName = validation.issues().stream()
                .anyMatch(issue -> issue.code().equals("empty_book_name")
                        || issue.code().equals("book_name_too_long")
                        || issue.code().equals("book_name_not_portable"));
        if (invalidName) {
            return "message.pickclimber.rules.invalid_book_name";
        }
        boolean tooLarge = validation.issues().stream()
                .anyMatch(issue -> issue.code().equals("rule_book_too_large"));
        return tooLarge
                ? "message.pickclimber.rules.network_payload_too_large"
                : "message.pickclimber.rules.invalid_profile";
    }

    private static void withTable(
            IPayloadContext context,
            BlockPos position,
            BiConsumer<ServerPlayer, ClimbingRulesTableBlockEntity> action
    ) {
        tableAccess(context, position, true).ifPresent(access -> action.accept(access.player(), access.table()));
    }

    private static Optional<TableAccess> tableAccess(
            IPayloadContext context,
            BlockPos position,
            boolean requireOpenMenu
    ) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return Optional.empty();
        }
        if (!MapmakerPermissions.canManage(player)) {
            result(player, false, "message.pickclimber.rules.permission_denied");
            return Optional.empty();
        }
        if (!MapmakerPermissions.isNear(player, position)) {
            result(player, false, "message.pickclimber.rules.table_too_far");
            return Optional.empty();
        }
        if (requireOpenMenu && (!(player.containerMenu instanceof ClimbingRulesTableMenu menu)
                || !menu.blockPos().equals(position))) {
            result(player, false, "message.pickclimber.rules.table_session_expired");
            return Optional.empty();
        }
        if (!(player.level().getBlockEntity(position) instanceof ClimbingRulesTableBlockEntity table)) {
            result(player, false, "message.pickclimber.rules.table_missing");
            return Optional.empty();
        }
        return Optional.of(new TableAccess(player, table));
    }

    private static void result(ServerPlayer player, boolean success, String key) {
        PacketDistributor.sendToPlayer(player, new RulesActionResultPayload(success, key));
    }

    private record TableAccess(ServerPlayer player, ClimbingRulesTableBlockEntity table) {
    }
}
