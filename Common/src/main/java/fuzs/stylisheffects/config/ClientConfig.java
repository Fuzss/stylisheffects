package fuzs.stylisheffects.config;

import com.google.common.collect.Lists;
import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;
import fuzs.puzzleslib.api.config.v3.serialization.ConfigDataSet;
import fuzs.stylisheffects.api.client.MobEffectWidgetContext;
import fuzs.stylisheffects.client.gui.effects.AbstractEffectRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

import java.util.List;

public class ClientConfig implements ConfigCore {
    @Config
    private final RenderersConfig renderers = new RenderersConfig();
    @Config
    private final WidgetsConfig widgets = new WidgetsConfig();

    public InventoryRendererConfig inventoryRenderer() {
        return this.renderers.inventoryRenderer;
    }

    public GuiRendererConfig guiRenderer() {
        return this.renderers.guiRenderer;
    }

    public InventoryCompactWidgetConfig inventoryCompactWidget() {
        return this.widgets.inventoryCompact;
    }

    public InventoryFullSizeWidgetConfig inventoryFullSizeWidget() {
        return this.widgets.inventoryFullSize;
    }

    public GuiWidgetConfig guiSmallWidget() {
        return this.widgets.guiSmall;
    }

    public GuiCompactWidgetConfig guiCompactWidget() {
        return this.widgets.guiCompact;
    }

    public enum LongDuration {
        INFINITY, ASTERISKS, NONE
    }

    public enum EffectAmplifier {
        NONE, TOP_LEFT, TOP_RIGHT
    }

    public static class RenderersConfig implements ConfigCore {
        @Config
        final InventoryRendererConfig inventoryRenderer = new InventoryRendererConfig();
        @Config
        final GuiRendererConfig guiRenderer = new GuiRendererConfig();
    }

    public static class WidgetsConfig implements ConfigCore {
        @Config
        final InventoryCompactWidgetConfig inventoryCompact = new InventoryCompactWidgetConfig();
        @Config
        final InventoryFullSizeWidgetConfig inventoryFullSize = new InventoryFullSizeWidgetConfig();
        @Config
        final GuiWidgetConfig guiSmall = new GuiWidgetConfig();
        @Config
        final GuiCompactWidgetConfig guiCompact = new GuiCompactWidgetConfig();
    }

    public static abstract class EffectRendererConfig implements ConfigCore {
        @Config(description = {"Effect renderer to be used.", "This setting might not be respected when not enough screen space is available. To force this setting disable \"allow_fallback\"."})
        public MobEffectWidgetContext.Renderer rendererType = MobEffectWidgetContext.Renderer.GUI_COMPACT;
        @Config(description = "Maximum amount of status effects rendered in a single row.")
        @Config.IntRange(min = 1, max = 255)
        public int maxColumns = 5;
        @Config(description = "Maximum amount of status effects rendered in a single column.")
        @Config.IntRange(min = 1, max = 255)
        public int maxRows = 255;
        @Config(description = "Screen side to render status effects on.")
        public MobEffectWidgetContext.ScreenSide screenSide = MobEffectWidgetContext.ScreenSide.RIGHT;
        @Config(description = "Alpha value for effect widgets.")
        @Config.DoubleRange(min = 0.0, max = 1.0)
        public double widgetAlpha = 1.0;
        @Config(description = "Space between individual effect widgets on x-axis.")
        @Config.IntRange(min = 0)
        public int widgetSpaceX = 1;
        @Config(description = "Space between individual effect widgets on y-axis.")
        @Config.IntRange(min = 0)
        public int widgetSpaceY = 1;
        @Config(description = "Respect vanilla's \"hideParticles\" flag which prevents a status effect from showing when set via commands.")
        public boolean respectHideParticles = true;
        @Config(description = "Allow effect renderer to fall back to a more compact version (when available) if not enough screen space exists. Otherwise effect widgets might run off-screen.")
        public boolean allowFallback = true;
        @Config(description = "Custom scale for effect renderer.")
        @Config.DoubleRange(min = 1.0, max = 12.0)
        public double scale = AbstractEffectRenderer.DEFAULT_WIDGET_SCALE;
    }

    public static class InventoryRendererConfig extends EffectRendererConfig {
        @Config(description = "Render active status effects in every menu screen, not just in the player inventory.")
        public boolean effectsEverywhere = true;
        @Config(name = "menu_blacklist", description = "Exclude certain menus from showing active status effects. Useful when effect icons overlap with other screen elements.")
        List<String> menuBlacklistRaw = Lists.newArrayList("curios:curios_container", "tconstruct:*", "mekanism:*");
        @Config(description = "Print menu type to game chat whenever a new menu screen is opened. Only intended to find menu types to be added to \"menu_blacklist\".")
        public boolean debugContainerTypes = false;
        @Config(description = "Show a tooltip when hovering over an effect widget.")
        public boolean hoveringTooltip = true;
        @Config(description = "Show remaining status effect duration on tooltip.")
        public boolean tooltipDuration = true;
        @Config(description = "Minimum screen border distance for effect widgets.")
        @Config.IntRange(min = 0)
        public int screenBorderDistance = 3;

        public ConfigDataSet<MenuType<?>> menuBlacklist;

        public InventoryRendererConfig() {
            this.screenSide = MobEffectWidgetContext.ScreenSide.LEFT;
            this.widgetAlpha = 1.0;
            this.respectHideParticles = false;
            this.allowFallback = true;
        }

        @Override
        public void afterConfigReload() {
            this.menuBlacklist = ConfigDataSet.from(Registries.MENU, this.menuBlacklistRaw);
        }
    }

    public static class GuiRendererConfig extends EffectRendererConfig {
        @Config(description = "Offset on x-axis.")
        @Config.IntRange(min = 0)
        public int offsetX = 3;
        @Config(description = "Offset on y-axis.")
        @Config.IntRange(min = 0)
        public int offsetY = 3;

        public GuiRendererConfig() {
            this.screenSide = MobEffectWidgetContext.ScreenSide.RIGHT;
            this.widgetAlpha = 0.85;
            this.respectHideParticles = true;
            this.allowFallback = false;
        }
    }

    public static abstract class EffectWidgetConfig implements ConfigCore {
        public static final String EFFECT_FORMATTING = "EFFECT";

        @Config(description = "Should the effect icon start to blink when the effect is running out.")
        public boolean blinkingAlpha = true;
        @Config(name = "duration_color", description = "Effect duration color. Setting this to \"EFFECT\" will use potion color.")
        @Config.AllowedValues(values = {EFFECT_FORMATTING, "BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE", "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN", "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE"})
        protected String durationColorRaw = "GRAY";
        @Config(description = "Should ambient effect widgets have a cyan colored border.")
        public boolean ambientBorder = true;
        @Config(description = "Should effect widgets have a blue or red border depening on if they are beneficial or not.")
        public boolean qualityBorder = false;
        @Config(description = "Show duration for ambient effects.")
        public boolean ambientDuration = true;

        public ChatFormatting durationColor;

        @Override
        public void afterConfigReload() {
            this.durationColor = ChatFormatting.getByName(this.durationColorRaw);
        }
    }

    public static class InventoryFullSizeWidgetConfig extends EffectWidgetConfig {
        @Config(name = "name_color", description = "Effect name color. Setting this to \"EFFECT\" will use potion color.")
        @Config.AllowedValues(values = {EFFECT_FORMATTING, "BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE", "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN", "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE"})
        String nameColorRaw = "WHITE";

        public ChatFormatting nameColor;

        public InventoryFullSizeWidgetConfig() {
            this.ambientDuration = true;
        }

        @Override
        public void afterConfigReload() {
            super.afterConfigReload();
            this.nameColor = ChatFormatting.getByName(this.nameColorRaw);
        }
    }

    public abstract static class CompactWidgetConfig extends EffectWidgetConfig {
        static final String COMPACT_DURATION_DESCRIPTION = "Display effect duration more compact, allows for always showing duration, even when it is very long.";

        @Config(description = "Display string to be used for an effect duration that is too long to show.")
        public LongDuration longDuration = LongDuration.INFINITY;
        @Config(description = "Top corner to draw effect amplifier in, or none.")
        public EffectAmplifier effectAmplifier = EffectAmplifier.TOP_RIGHT;
        @Config(name = "amplifier_color", description = "Effect amplifier color. Setting this to \"EFFECT\" will use potion color.")
        @Config.AllowedValues(values = {EFFECT_FORMATTING, "BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE", "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN", "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE"})
        String amplifierColorRaw = "WHITE";

        public ChatFormatting amplifierColor;

        public CompactWidgetConfig() {
            this.ambientDuration = false;
        }

        @Override
        public void afterConfigReload() {
            super.afterConfigReload();
            this.amplifierColor = ChatFormatting.getByName(this.amplifierColorRaw);
        }
    }

    public static class InventoryCompactWidgetConfig extends CompactWidgetConfig {
        @Config(description = COMPACT_DURATION_DESCRIPTION)
        public boolean compactDuration = false;
    }

    public static class GuiWidgetConfig extends CompactWidgetConfig {
        @Config(description = "Draw harmful effects on a separate line from beneficial ones. This is turned on in vanilla.")
        public boolean separateEffects = false;
    }

    public static class GuiCompactWidgetConfig extends GuiWidgetConfig {
        @Config(description = COMPACT_DURATION_DESCRIPTION)
        public boolean compactDuration = false;
    }
}
