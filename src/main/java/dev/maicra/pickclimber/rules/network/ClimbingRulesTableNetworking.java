package dev.maicra.pickclimber.rules.network;

import dev.maicra.pickclimber.rules.UnlistedPolicy;
import dev.maicra.pickclimber.ModItems;
import dev.maicra.pickclimber.rules.ClimbingRuleBookCodec;
import dev.maicra.pickclimber.rules.ClimbingRuleBookDefinition;
import dev.maicra.pickclimber.rules.ClimbingRuleBookValidationResult;
import dev.maicra.pickclimber.rules.ClimbingRuleBookValidator;
import dev.maicra.pickclimber.rules.ClimbingRulesClientUi;
import dev.maicra.pickclimber.rules.ClimbingRulesProfile;
import dev.maicra.pickclimber.rules.ClimbingRulesService;
import dev.maicra.pickclimber.rules.DefaultRuleProfileFactory;
import dev.maicra.pickclimber.rules.MapmakerPermissions;
import dev.maicra.pickclimber.rules.RuleBookActivationMode;
import dev.maicra.pickclimber.rules.RuleBookScope;
import dev.maicra.pickclimber.rules.RuleDefinitionId;
import dev.maicra.pickclimber.rules.WorldRulesSnapshot;
import dev.maicra.pickclimber.rules.block.ClimbingRulesTableBlockEntity;
import dev.maicra.pickclimber.rules.item.ClimbingRuleBookData;
import dev.maicra.pickclimber.rules.menu.ClimbingRuleBookProcessingMenu;
import dev.maicra.pickclimber.rules.menu.ClimbingRulesTableMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
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
        registrar.playToServer(OpenRuleBookProcessingPayload.TYPE, OpenRuleBookProcessingPayload.STREAM_CODEC,
                ClimbingRulesTableNetworking::handleOpenProcessing);
        registrar.playToServer(OpenRuleBookExportRequestPayload.TYPE, OpenRuleBookExportRequestPayload.STREAM_CODEC,
                ClimbingRulesTableNetworking::handleOpenExport);
        registrar.playToClient(OpenRuleBookExportPayload.TYPE, OpenRuleBookExportPayload.STREAM_CODEC,
                ClimbingRulesTableNetworking::handleExportPayload);
        registrar.playToServer(OpenRulesTablePayload.TYPE, OpenRulesTablePayload.STREAM_CODEC,
                ClimbingRulesTableNetworking::handleOpenRulesTable);
        registrar.playToServer(ClearRuleBookPayload.TYPE, ClearRuleBookPayload.STREAM_CODEC,
                ClimbingRulesTableNetworking::handleClearRuleBook);
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
            ClimbingRulesProfile profile = DefaultRuleProfileFactory.create(payload.profileName());
            ClimbingRuleBookDefinition definition = new ClimbingRuleBookDefinition(
                    ClimbingRuleBookDefinition.CURRENT_FORMAT_VERSION,
                    payload.profileName(),
                    DyeColor.WHITE,
                    profile,
                    RuleBookActivationMode.PERMANENT,
                    RuleBookScope.WORLD,
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
            importIntoTableBook(player, table, payload.position(), validation.normalizedDefinition());
        });
    }

    private static void handleImportCurrent(ImportCurrentRulesPayload payload, IPayloadContext context) {
        withTable(context, payload.position(), (player, table) -> {
            if (!canStartCreation(player, table)) {
                return;
            }
            WorldRulesSnapshot snapshot = ClimbingRulesService.snapshot(player.serverLevel().getServer());
            Optional<ClimbingRuleBookDefinition> current = snapshot.effectiveDefinition();
            ClimbingRuleBookDefinition definition = current.orElseGet(() -> {
                String name = Component.translatable("gui.pickclimber.rules.import_current_world").getString();
                ClimbingRulesProfile defaults = DefaultRuleProfileFactory.create(name);
                return ClimbingRuleBookDefinition.permanentWorld(name, defaults);
            });
            importIntoTableBook(player, table, payload.position(), definition);
        });
    }



    private static void handleOpenExport(OpenRuleBookExportRequestPayload payload, IPayloadContext context) {
        withTable(context, payload.position(), (player, table) -> {
            ItemStack stack = table.getItem(ClimbingRulesTableBlockEntity.WORK_SLOT);
            ClimbingRuleBookData.resolveDefinition(player.serverLevel().getServer(), stack)
                    .ifPresentOrElse(definition ->
                    ClimbingRuleBookCodec.encodeToNbt(definition).result().ifPresent(tag ->
                            PacketDistributor.sendToPlayer(player, new OpenRuleBookExportPayload(tag))
                    ),
                    () -> result(player, false, "message.pickclimber.rules.valid_book_required")
            );
        });
    }

    private static void handleExportPayload(OpenRuleBookExportPayload payload, IPayloadContext context) {
        if (!RuleBookNetworkLimits.accepts(payload.definitionTag())) {
            return;
        }
        ClimbingRuleBookCodec.decodeFromNbt(payload.definitionTag()).result()
                .ifPresent(ClimbingRulesClientUi::openExporter);
    }
    private static void handleOpenRulesTable(OpenRulesTablePayload payload, IPayloadContext context) {
        tableAccess(context, payload.position(), false).ifPresent(access -> {
            RulesEditorSessionStore.invalidate(access.player());
            openTable(access.player(), access.table(), payload.position());
        });
    }

    private static void handleOpenProcessing(OpenRuleBookProcessingPayload payload, IPayloadContext context) {
        withTable(context, payload.position(), (player, table) -> {
            ItemStack source = table.getItem(ClimbingRulesTableBlockEntity.WORK_SLOT);
            if (ClimbingRuleBookData.resolveDefinition(player.serverLevel().getServer(), source).isEmpty()) {
                result(player, false, "message.pickclimber.rules.valid_book_required");
                return;
            }
            RulesEditorSessionStore.invalidate(player);
            ContainerLevelAccess access = ContainerLevelAccess.create(player.level(), payload.position());
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inventory, ignored) -> new ClimbingRuleBookProcessingMenu(
                                    id,
                                    inventory,
                                    table,
                                    access
                            ),
                            Component.translatable("container.pickclimber.climbing_rule_book_processing")
                    ),
                    buffer -> buffer.writeBlockPos(payload.position())
            );
        });
    }

    private static void handleClearRuleBook(ClearRuleBookPayload payload, IPayloadContext context) {
        withTable(context, payload.position(), (player, table) -> {
            ItemStack stack = table.getItem(ClimbingRulesTableBlockEntity.WORK_SLOT);
            if (!stack.is(ModItems.CLIMBING_RULE_BOOK.get())) {
                return;
            }
            table.setItem(ClimbingRulesTableBlockEntity.WORK_SLOT, new ItemStack(Items.BOOK));
            table.setChanged();
            RulesEditorSessionStore.invalidate(player);
            result(player, true, "message.pickclimber.rules.book_cleared");
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
            ItemStack ruleBook = table.getItem(ClimbingRulesTableBlockEntity.WORK_SLOT);
            Optional<ClimbingRuleBookDefinition> source = ClimbingRuleBookData.resolveDefinition(
                    player.serverLevel().getServer(), ruleBook
            );
            if (source.isEmpty()) {
                result(player, false, "message.pickclimber.rules.valid_book_required");
                return;
            }
            openDraft(player, payload.position(), RulesEditorSessionStore.Operation.EDIT, source, source.get());
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
        ClimbingRuleBookDefinition target = completeUnclassifiedAsUnclimbable(
                validation.normalizedDefinition()
        );
        ClimbingRuleBookValidationResult completedValidation = ClimbingRuleBookValidator.validateAndNormalize(target);
        if (!completedValidation.valid()) {
            result(player, false, validationMessageKey(completedValidation));
            return;
        }
        target = completedValidation.normalizedDefinition();
        if (target.coverColor() != session.allowedCoverColor()) {
            result(player, false, "message.pickclimber.rules.dye_changed_during_edit");
            return;
        }

        if (session.operation() == RulesEditorSessionStore.Operation.EDIT) {
            saveEdit(player, table, payload.position(), session, target);
            return;
        }
        saveCreation(player, table, payload.position(), target);
    }

    private static void saveEdit(
            ServerPlayer player,
            ClimbingRulesTableBlockEntity table,
            BlockPos position,
            RulesEditorSessionStore.Session session,
            ClimbingRuleBookDefinition target
    ) {
        ItemStack ruleBook = table.getItem(ClimbingRulesTableBlockEntity.WORK_SLOT);
        Optional<ClimbingRuleBookDefinition> current = ClimbingRuleBookData.resolveDefinition(
                player.serverLevel().getServer(), ruleBook
        );
        if (!ruleBook.is(ModItems.CLIMBING_RULE_BOOK.get())
                || current.isEmpty()
                || !session.matchesSource(current.get())) {
            result(player, false, "message.pickclimber.rules.stale_book");
            if (current.isPresent()) {
                openEditFromTable(player, position, current.get());
            } else {
                RulesEditorSessionStore.invalidate(player);
            }
            return;
        }
        target = withAuthorIfMissing(target, player);
        String expectedDefinitionId = RuleDefinitionId.of(target.profile());
        if (!ClimbingRuleBookData.write(player.serverLevel().getServer(), ruleBook, target)) {
            result(player, false, "message.pickclimber.rules.invalid_profile");
            return;
        }

        Optional<ClimbingRuleBookData.Reference> persistedReference = ClimbingRuleBookData.readReference(ruleBook);
        Optional<ClimbingRuleBookDefinition> persistedDefinition = ClimbingRuleBookData.resolveDefinition(
                player.serverLevel().getServer(),
                ruleBook
        );
        boolean persisted = persistedReference
                .map(reference -> reference.definitionId().equals(expectedDefinitionId))
                .orElse(false)
                && persistedDefinition.map(target::equals).orElse(false);
        if (!persisted) {
            result(player, false, "message.pickclimber.rules.invalid_profile");
            return;
        }

        table.setItem(ClimbingRulesTableBlockEntity.WORK_SLOT, ruleBook.copy());
        table.setChanged();
        player.serverLevel().sendBlockUpdated(
                position,
                table.getBlockState(),
                table.getBlockState(),
                net.minecraft.world.level.block.Block.UPDATE_CLIENTS
        );
        RulesEditorSessionStore.invalidate(player);
        openTable(player, table, position);
        result(player, true, "message.pickclimber.rules.book_saved");
    }

    private static void saveCreation(
            ServerPlayer player,
            ClimbingRulesTableBlockEntity table,
            BlockPos position,
            ClimbingRuleBookDefinition target
    ) {
        ItemStack baseBook = table.getItem(ClimbingRulesTableBlockEntity.WORK_SLOT);
        if (!baseBook.is(Items.BOOK)) {
            result(player, false, "message.pickclimber.rules.book_required");
            return;
        }
        target = withAuthorIfMissing(target, player);
        ItemStack ruleBook = ClimbingRuleBookData.create(player.serverLevel().getServer(), target);
        if (ruleBook.isEmpty()) {
            result(player, false, "message.pickclimber.rules.invalid_profile");
            return;
        }
        table.setItem(ClimbingRulesTableBlockEntity.WORK_SLOT, ruleBook);
        table.setChanged();
        RulesEditorSessionStore.invalidate(player);
        openTable(player, table, position);
        result(player, true, "message.pickclimber.rules.book_created");
    }


    private static void importIntoTableBook(
            ServerPlayer player,
            ClimbingRulesTableBlockEntity table,
            BlockPos position,
            ClimbingRuleBookDefinition definition
    ) {
        definition = withAuthorIfMissing(definition, player);
        ItemStack ruleBook = ClimbingRuleBookData.create(player.serverLevel().getServer(), definition);
        if (ruleBook.isEmpty()) {
            result(player, false, "message.pickclimber.rules.invalid_profile");
            return;
        }
        table.setItem(ClimbingRulesTableBlockEntity.WORK_SLOT, ruleBook);
        table.setChanged();
        RulesEditorSessionStore.invalidate(player);
        openTable(player, table, position);
        result(player, true, "message.pickclimber.rules.book_imported");
    }

    private static void openTable(
            ServerPlayer player,
            ClimbingRulesTableBlockEntity table,
            BlockPos position
    ) {
        player.openMenu(table, buffer -> buffer.writeBlockPos(position));
    }

    private static boolean canStartCreation(ServerPlayer player, ClimbingRulesTableBlockEntity table) {
        if (!table.getItem(ClimbingRulesTableBlockEntity.WORK_SLOT).is(Items.BOOK)) {
            result(player, false, "message.pickclimber.rules.book_required");
            return false;
        }
        return true;
    }

    private static void openEditFromTable(
            ServerPlayer player,
            BlockPos position,
            ClimbingRuleBookDefinition source
    ) {
        openDraft(
                player,
                position,
                RulesEditorSessionStore.Operation.EDIT,
                Optional.of(source),
                source
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

    private static ClimbingRuleBookDefinition completeUnclassifiedAsUnclimbable(
            ClimbingRuleBookDefinition definition
    ) {
        ClimbingRulesProfile profile = definition.profile();
        ClimbingRulesProfile authorableBaseline = DefaultRuleProfileFactory.create(profile.profileName());

        Set<net.minecraft.resources.ResourceLocation> completedUnclimbable =
                new LinkedHashSet<>(profile.unclimbableBlocks());
        completedUnclimbable.removeAll(profile.stableBlocks());
        completedUnclimbable.removeAll(profile.unstableBlocks());

        Set<net.minecraft.resources.ResourceLocation> authorable = new LinkedHashSet<>();
        authorable.addAll(authorableBaseline.stableBlocks());
        authorable.addAll(authorableBaseline.unstableBlocks());
        authorable.addAll(authorableBaseline.unclimbableBlocks());

        for (net.minecraft.resources.ResourceLocation id : authorable) {
            if (!profile.stableBlocks().contains(id) && !profile.unstableBlocks().contains(id)) {
                completedUnclimbable.add(id);
            }
        }

        ClimbingRulesProfile completedProfile = new ClimbingRulesProfile(
                profile.formatVersion(),
                profile.profileName(),
                profile.stableBlocks(),
                profile.unstableBlocks(),
                completedUnclimbable,
                UnlistedPolicy.UNCLIMBABLE,
                profile.pickaxeWear(),
                profile.playerMiningEnabled(),
                profile.unmineableTerminals()
        );
        return new ClimbingRuleBookDefinition(
                definition.formatVersion(),
                definition.bookName(),
                definition.coverColor(),
                completedProfile,
                definition.activationMode(),
                definition.scope(),
                definition.durationSeconds(),
                definition.authorUuid(),
                definition.authorName()
        );
    }

    private static ClimbingRuleBookDefinition withAuthorIfMissing(
            ClimbingRuleBookDefinition definition,
            ServerPlayer player
    ) {
        if (!definition.authorName().isBlank()) {
            return definition;
        }
        return definition.withAuthor(player.getUUID().toString(), player.getGameProfile().getName());
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
