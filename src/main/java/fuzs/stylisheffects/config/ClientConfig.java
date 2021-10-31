package fuzs.stylisheffects.config;

import fuzs.puzzleslib.config.v2.AbstractConfig;
import fuzs.puzzleslib.config.v2.annotation.Config;
import net.minecraft.util.text.TextFormatting;

public class ClientConfig extends AbstractConfig {
    @Config
    private GeneralConfig generalConfig = new GeneralConfig();
    @Config
    private InventoryEffectConfig inventoryEffects = new InventoryEffectConfig();
    @Config
    private HudEffectConfig hudEffectsConfig = new HudEffectConfig();

    public ClientConfig() {
        super("");
    }

    @Override
    protected void afterConfigReload() {
        this.inventoryEffects().afterConfigReload();
        this.hudEffectsConfig().afterConfigReload();
    }

    public GeneralConfig generalConfig() {
        return this.generalConfig;
    }

    public InventoryEffectConfig inventoryEffects() {
        return this.inventoryEffects;
    }

    public HudEffectConfig hudEffectsConfig() {
        return this.hudEffectsConfig;
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
        LEFT, RIGHT
    }

    public static class GeneralConfig extends AbstractConfig {
        @Config(description = "Effect renderer to be used in hud.")
        public EffectRenderer hudEffectsRenderer = EffectRenderer.VANILLA;
        @Config(description = "Effect renderer to be used in inventory.")
        public EffectRenderer inventoryEffectRenderer = EffectRenderer.COMPACT;
        @Config(description = "Render active status effects in every container, not just in the player inventory.")
        public boolean effectsEverywhere = true;

        public GeneralConfig() {
            super("general");
        }
    }

    public static abstract class EffectConfig extends AbstractConfig {
        public static final String EFFECT_FORMATTING = "EFFECT";

        @Config(description = "Maximum amount of status effects rendered in a single row.")
        @Config.IntRange(min = 1, max = 255)
        public int maxWidth = 255;
        @Config(description = "Maximum amount of status effects rendered in a single column.")
        @Config.IntRange(min = 1, max = 255)
        public int maxHeight = 255;
        @Config(description = "Screen side to render status effects on.")
        public ScreenSide screenSide = ScreenSide.RIGHT;
        @Config(description = "Display string to be used for an effect duration that is too long to show.")
        public LongDurationString longDurationString = LongDurationString.INFINITY;
        @Config(description = "Scale for effect widgets.")
        @Config.FloatRange(min = 1.0F, max = 24.0F)
        public float widgetScale = 8.0F;
        @Config(description = "Alpha value for effect widgets.")
        @Config.FloatRange(min = 0.0F, max = 1.0F)
        public float widgetAlpha = 1.0F;
        @Config(description = "Should the effects icon start to blink when the effect is running out.")
        public boolean blinkingAlpha = true;
        @Config(description = "Show a tooltip when hovering over an effect widget in the inventory.")
        public boolean hoveringTooltip = true;
        @Config(description = "Show remaining status effect duration on tooltip.")
        public boolean tooltipDuration = false;
        @Config(description = "Top corner to draw effect amplifier in for \"COMPACT\" effect renderer, or none.")
        public EffectAmplifier effectAmplifier = EffectAmplifier.TOP_RIGHT;
        @Config(name = "duration_color", description = "Effect name color for \"VANILLA\" effect renderer. Setting this to \"EFFECT\" will use potion color.")
        @Config.AllowedValues(values = {EFFECT_FORMATTING, "BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE", "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN", "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE"})
        private String nameColorRaw = "WHITE";
        @Config(name = "duration_color", description = "Effect duration color. Setting this to \"EFFECT\" will use potion color.")
        @Config.AllowedValues(values = {EFFECT_FORMATTING, "BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE", "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN", "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE"})
        private String durationColorRaw = EFFECT_FORMATTING;
        @Config(name = "amplifier_color", description = "Effect amplifier color for \"COMPACT\" effect renderer. Setting this to \"EFFECT\" will use potion color.")
        @Config.AllowedValues(values = {EFFECT_FORMATTING, "BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE", "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN", "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE"})
        private String amplifierColorRaw = "WHITE";

        public TextFormatting nameColor;
        public TextFormatting durationColor;
        public TextFormatting amplifierColor;

        public EffectConfig(String name) {
            super(name);
        }

        @Override
        protected void afterConfigReload() {
            this.nameColor = TextFormatting.getByName(this.nameColorRaw);
            this.durationColor = TextFormatting.getByName(this.durationColorRaw);
            this.amplifierColor = TextFormatting.getByName(this.amplifierColorRaw);
        }
    }

    public static class InventoryEffectConfig extends EffectConfig {
        public InventoryEffectConfig() {
            super("inventory");
            this.screenSide = ScreenSide.LEFT;
            this.longDurationString = LongDurationString.VANILLA;
        }
    }

    public static class HudEffectConfig extends EffectConfig {
        public HudEffectConfig() {
            super("hud");
        }
    }
}
