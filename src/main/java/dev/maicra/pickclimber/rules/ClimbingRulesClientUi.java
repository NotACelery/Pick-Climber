package dev.maicra.pickclimber.rules;

import net.minecraft.core.BlockPos;

public final class ClimbingRulesClientUi {
    private static final RulesEditorOpener NO_OP = (position, sessionToken, definition) -> {
    };
    private static final RuleBookViewerOpener NO_OP_VIEWER = definition -> {
    };
    private static final RuleBookExporterOpener NO_OP_EXPORTER = definition -> {
    };
    private static RulesEditorOpener editorOpener = NO_OP;
    private static RuleBookViewerOpener viewerOpener = NO_OP_VIEWER;
    private static RuleBookExporterOpener exporterOpener = NO_OP_EXPORTER;

    private ClimbingRulesClientUi() {
    }

    public static void install(RulesEditorOpener opener) {
        editorOpener = opener == null ? NO_OP : opener;
    }

    public static void installViewer(RuleBookViewerOpener opener) {
        viewerOpener = opener == null ? NO_OP_VIEWER : opener;
    }

    public static void openViewer(ClimbingRuleBookDefinition definition) {
        viewerOpener.open(definition);
    }

    public static void installExporter(RuleBookExporterOpener opener) {
        exporterOpener = opener == null ? NO_OP_EXPORTER : opener;
    }

    public static void openExporter(ClimbingRuleBookDefinition definition) {
        exporterOpener.open(definition);
    }

    public static void openEditor(
            BlockPos position,
            int sessionToken,
            ClimbingRuleBookDefinition definition
    ) {
        editorOpener.open(position, sessionToken, definition);
    }

    @FunctionalInterface
    public interface RuleBookViewerOpener {
        void open(ClimbingRuleBookDefinition definition);
    }

    @FunctionalInterface
    public interface RuleBookExporterOpener {
        void open(ClimbingRuleBookDefinition definition);
    }

    @FunctionalInterface
    public interface RulesEditorOpener {
        void open(BlockPos position, int sessionToken, ClimbingRuleBookDefinition definition);
    }
}
