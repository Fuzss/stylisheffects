package fuzs.stylisheffects.config;

import fuzs.puzzleslib.config.v2.AbstractConfig;
import fuzs.puzzleslib.config.v2.annotation.Config;
import net.minecraft.util.text.TextFormatting;

public class ClientConfig extends AbstractConfig {
    @Config
    private RenderersConfig renderers = new RenderersConfig();
    @Config
    private WidgetsConfig widgets = new WidgetsConfig();

    public ClientConfig() {
        super("");
    }

    @Override
    protected void afterConfigReload() {
        this.vanillaWidget().afterConfigReload();
        this.compactWidget().afterConfigReload();
    }

    public InventoryRendererConfig inventoryRenderer() {
        return this.renderers.inventoryRenderer;
    }

    public HudRendererConfig hudRenderer() {
        return this.renderers.hudRenderer;
    }

    public VanillaWidgetConfig vanillaWidget() {
        return this.widgets.vanillaWidget;
    }

    public CompactWidgetConfig compactWidget() {
        return this.widgets.compactWidget;
    }

    public enum EffectRenderer {
        NONE, COMPACT, VANILLA
    }

    public enum LongDurationString {
        VANILLA, INFINITY, NONE
    }

    public enum EffectAmplifier {
        NONE, TOP_LEFT, TOP_RIGHT
    }

    public enum ScreenSide {
        LEFT, RIGHT;

        public boolean right() {
            return this == RIGHT;
        }

        public ScreenSide inverse() {
            return this.right() ? LEFT : RIGHT;
        }
    }

    public enum OverflowMode {
        CONDENSE, SKIP
    }

    public static class RenderersConfig extends AbstractConfig {
        @Config
        InventoryRendererConfig inventoryRenderer = new InventoryRendererConfig();
        @Config
        HudRendererConfig hudRenderer = new HudRendererConfig();

        public RenderersConfig() {
            super("renderers");
        }
    }

    public static class WidgetsConfig extends AbstractConfig {
        @Config
        VanillaWidgetConfig vanillaWidget = new VanillaWidgetConfig();
        @Config
        CompactWidgetConfig compactWidget = new CompactWidgetConfig();

        public WidgetsConfig() {
            super("widgets");
        }
    }

    public static abstract class EffectRendererConfig extends AbstractConfig {
        @Config(description = "Effect renderer to be used.")
        public EffectRenderer rendererType = EffectRenderer.COMPACT;
        @Config(description = "Maximum amount of status effects rendered in a single row.")
        @Config.IntRange(min = 1, max = 255)
        public int maxWidth = 255;
        @Config(description = "Maximum amount of status effects rendered in a single column.")
        @Config.IntRange(min = 1, max = 255)
        public int maxHeight = 255;
        @Config(description = "Screen side to render status effects on.")
        public ScreenSide screenSide = ScreenSide.RIGHT;
//        @Config(description = "Scale for effect widgets.")
//        @Config.FloatRange(min = 1.0F, max = 24.0F)
//        public float widgetScale = 8.0F;
        @Config(description = "Alpha value for effect widgets.")
        @Config.FloatRange(min = 0.0F, max = 1.0F)
        public float widgetAlpha = 1.0F;
        @Config(description = "What to do when there are more effects to display than there is room on-screen.")
        public OverflowMode overflowMode = OverflowMode.CONDENSE;
        @Config(description = "Space between individual effect widgets.")
        @Config.IntRange(min = 0)
        public int widgetSpace = 1;

        public EffectRendererConfig(String name) {
            super(name);
        }
    }

    public static class InventoryRendererConfig extends EffectRendererConfig {
        @Config(description = "Render active status effects in every container, not just in the player inventory.")
        public boolean effectsEverywhere = true;
        @Config(description = "Show a tooltip when hovering over an effect widget.")
        public boolean hoveringTooltip = true;
        @Config(description = "Show remaining status effect duration on tooltip.")
        public boolean tooltipDuration = true;

        public InventoryRendererConfig() {
            super("inventory_renderer");
            this.screenSide = ScreenSide.LEFT;
            this.overflowMode = OverflowMode.CONDENSE;
        }
    }
    public static class HudRendererConfig extends EffectRendererConfig {
        @Config(description = "Offset on x-axis.")
        @Config.IntRange(min = 0)
        public int offsetX = 0;
        @Config(description = "Offset on y-axis.")
        @Config.IntRange(min = 0)
        public int offsetY = 0;

        public HudRendererConfig() {
            super("hud_renderer");
            this.screenSide = ScreenSide.RIGHT;
            this.overflowMode = OverflowMode.SKIP;
        }
    }

    public static abstract class EffectWidgetConfig extends AbstractConfig {
        public static final String EFFECT_FORMATTING = "EFFECT";

        @Config(description = "Should the effect icon start to blink when the effect is running out.")
        public boolean blinkingAlpha = true;
        @Config(description = "Display string to be used for an effect duration that is too long to show.")
        public LongDurationString longDurationString = LongDurationString.INFINITY;
        @Config(name = "duration_color", description = "Effect duration color. Setting this to \"EFFECT\" will use potion color.")
        @Config.AllowedValues(values = {EFFECT_FORMATTING, "BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE", "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN", "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE"})
        protected String durationColorRaw = "GRAY";
        @Config(description = "Should ambient effect widgets have a cyan colored border.")
        public boolean ambientBorder = true;
        @Config(description = "Show duration for ambient effects.")
        public boolean ambientDuration = true;

        public TextFormatting durationColor;

        public EffectWidgetConfig(String name) {
            super(name);
        }

        @Override
        protected void afterConfigReload() {
            this.durationColor = TextFormatting.getByName(this.durationColorRaw);
        }
    }

    public static class VanillaWidgetConfig extends EffectWidgetConfig {
        @Config(name = "name_color", description = "Effect name color. Setting this to \"EFFECT\" will use potion color.")
        @Config.AllowedValues(values = {EFFECT_FORMATTING, "BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE", "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN", "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE"})
        private String nameColorRaw = "WHITE";

        public TextFormatting nameColor;

        public VanillaWidgetConfig() {
            super("vanilla_widget");
            this.longDurationString = LongDurationString.VANILLA;
            this.ambientDuration = true;
        }

        @Override
        protected void afterConfigReload() {
            super.afterConfigReload();
            this.nameColor = TextFormatting.getByName(this.nameColorRaw);
        }
    }

    public static class CompactWidgetConfig extends EffectWidgetConfig {
        @Config(description = "Top corner to draw effect amplifier in, or none.")
        public EffectAmplifier effectAmplifier = EffectAmplifier.TOP_RIGHT;
        @Config(name = "amplifier_color", description = "Effect amplifier color. Setting this to \"EFFECT\" will use potion color.")
        @Config.AllowedValues(values = {EFFECT_FORMATTING, "BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE", "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN", "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE"})
        private String amplifierColorRaw = "WHITE";
        @Config(description = "Draw harmful effects on a separate line from beneficial ones.")
        public boolean separateEffects = true;

        public TextFormatting amplifierColor;

        public CompactWidgetConfig() {
            super("compact_widget");
            this.longDurationString = LongDurationString.INFINITY;
            this.ambientDuration = false;
        }

        @Override
        protected void afterConfigReload() {
            super.afterConfigReload();
            this.amplifierColor = TextFormatting.getByName(this.amplifierColorRaw);
        }
    }
}
