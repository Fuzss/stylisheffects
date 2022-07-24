package fuzs.stylisheffects.config;

import com.google.common.collect.Lists;
import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.annotation.Config;
import fuzs.puzzleslib.config.serialization.EntryCollectionBuilder;
import fuzs.stylisheffects.api.client.MobEffectWidgetContext;
import fuzs.stylisheffects.client.gui.effects.AbstractEffectRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;

import java.util.List;
import java.util.Set;

public class ClientConfig extends AbstractConfig {
    @Config
    private final RenderersConfig renderers = new RenderersConfig();
    @Config
    private final WidgetsConfig widgets = new WidgetsConfig();

    @Override
    protected void afterConfigReload() {
        this.guiSmallWidget().afterConfigReload();
        this.guiCompactWidget().afterConfigReload();
        this.inventoryCompactWidget().afterConfigReload();
        this.inventoryFullSizeWidget().afterConfigReload();
        this.inventoryRenderer().afterConfigReload();
    }

    public InventoryRendererConfig inventoryRenderer() {
        return this.renderers.inventoryRenderer;
    }

    public GuiRendererConfig guiRenderer() {
        return this.renderers.guiRenderer;
    }

    public InventoryCompactWidgetConfig inventoryCompactWidget() {
        return this.widgets.inventoryCompactWidget;
    }

    public InventoryFullSizeWidgetConfig inventoryFullSizeWidget() {
        return this.widgets.inventoryFullSizeWidget;
    }

    public GuiSmallWidgetConfig guiSmallWidget() {
        return this.widgets.guiSmallWiget;
    }

    public GuiCompactWigetConfig guiCompactWidget() {
        return this.widgets.guiCompactWiget;
    }

    public enum LongDuration {
        VANILLA, INFINITY, NONE
    }

    public enum EffectAmplifier {
        NONE, TOP_LEFT, TOP_RIGHT
    }

    public static class RenderersConfig extends AbstractConfig {
        @Config
        final InventoryRendererConfig inventoryRenderer = new InventoryRendererConfig();
        @Config
        final GuiRendererConfig guiRenderer = new GuiRendererConfig();

        public RenderersConfig() {
            super("renderers");
        }
    }

    public static class WidgetsConfig extends AbstractConfig {
        @Config
        final InventoryCompactWidgetConfig inventoryCompactWidget = new InventoryCompactWidgetConfig();
        @Config
        final InventoryFullSizeWidgetConfig inventoryFullSizeWidget = new InventoryFullSizeWidgetConfig();
        @Config
        final GuiSmallWidgetConfig guiSmallWiget = new GuiSmallWidgetConfig();
        @Config
        final GuiCompactWigetConfig guiCompactWiget = new GuiCompactWigetConfig();

        public WidgetsConfig() {
            super("widgets");
        }
    }

    public static abstract class EffectRendererConfig extends AbstractConfig {
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

        public EffectRendererConfig(String name) {
            super(name);
        }
    }

    public static class InventoryRendererConfig extends EffectRendererConfig {
        @Config(description = "Render active status effects in every menu screen, not just in the player inventory.")
        public boolean effectsEverywhere = true;
        @Config(name = "menu_blacklist", description = "Exclude certain menus from showing active status effects. Useful when effect icons overlap with other screen elements.")
        private List<String> menuBlacklistRaw = Lists.newArrayList("curios:curios_container", "tconstruct:part_builder", "tconstruct:tinker_station", "tconstruct:smeltery");
        @Config(description = "Print menu type to game chat whenever a new menu screen is opened. Only intended to find menu types to be added to \"menu_blacklist\".")
        public boolean debugContainerTypes = false;
        @Config(description = "Show a tooltip when hovering over an effect widget.")
        public boolean hoveringTooltip = true;
        @Config(description = "Show remaining status effect duration on tooltip.")
        public boolean tooltipDuration = true;
        @Config(description = "Minimum screen border distance for effect widgets.")
        @Config.IntRange(min = 0)
        public int screenBorderDistance = 3;

        public Set<MenuType<?>> menuBlacklist;

        public InventoryRendererConfig() {
            super("inventory_renderer");
            this.screenSide = MobEffectWidgetContext.ScreenSide.LEFT;
            this.widgetAlpha = 1.0;
            this.respectHideParticles = false;
            this.allowFallback = true;
        }

        @Override
        protected void afterConfigReload() {
            this.menuBlacklist = EntryCollectionBuilder.of(Registry.MENU_REGISTRY).buildSet(this.menuBlacklistRaw);
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
            super("gui_renderer");
            this.screenSide = MobEffectWidgetContext.ScreenSide.RIGHT;
            this.widgetAlpha = 0.85;
            this.respectHideParticles = true;
            this.allowFallback = false;
        }
    }

    public static abstract class EffectWidgetConfig extends AbstractConfig {
        public static final String EFFECT_FORMATTING = "EFFECT";
        static final String COMPACT_DURATION_DESCRIPTION = "Display effect duration more compact, allows for always showing duration, even when it is very long (vanilla will not show durations that are longer than ~30 minutes).";

        @Config(description = "Should the effect icon start to blink when the effect is running out.")
        public boolean blinkingAlpha = true;
        @Config(description = "Display string to be used for an effect duration that is too long to show.")
        public LongDuration longDuration = LongDuration.INFINITY;
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

        public EffectWidgetConfig(String name) {
            super(name);
        }

        @Override
        protected void afterConfigReload() {
            this.durationColor = ChatFormatting.getByName(this.durationColorRaw);
        }
    }

    public static class InventoryFullSizeWidgetConfig extends EffectWidgetConfig {
        @Config(name = "name_color", description = "Effect name color. Setting this to \"EFFECT\" will use potion color.")
        @Config.AllowedValues(values = {EFFECT_FORMATTING, "BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE", "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN", "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE"})
        private String nameColorRaw = "WHITE";

        public ChatFormatting nameColor;

        public InventoryFullSizeWidgetConfig() {
            super(MobEffectWidgetContext.Renderer.INVENTORY_FULL_SIZE.toString());
            this.longDuration = LongDuration.VANILLA;
            this.ambientDuration = true;
        }

        @Override
        protected void afterConfigReload() {
            super.afterConfigReload();
            this.nameColor = ChatFormatting.getByName(this.nameColorRaw);
        }
    }

    public static abstract class CompactWidgetConfig extends EffectWidgetConfig {
        @Config(description = "Top corner to draw effect amplifier in, or none.")
        public EffectAmplifier effectAmplifier = EffectAmplifier.TOP_RIGHT;
        @Config(name = "amplifier_color", description = "Effect amplifier color. Setting this to \"EFFECT\" will use potion color.")
        @Config.AllowedValues(values = {EFFECT_FORMATTING, "BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE", "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN", "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE"})
        private String amplifierColorRaw = "WHITE";

        public ChatFormatting amplifierColor;

        public CompactWidgetConfig(String name) {
            super(name);
            this.longDuration = LongDuration.INFINITY;
            this.ambientDuration = false;
        }

        @Override
        protected void afterConfigReload() {
            super.afterConfigReload();
            this.amplifierColor = ChatFormatting.getByName(this.amplifierColorRaw);
        }
    }

    public static class InventoryCompactWidgetConfig extends CompactWidgetConfig {
        @Config(description = COMPACT_DURATION_DESCRIPTION)
        public boolean compactDuration = false;

        public InventoryCompactWidgetConfig() {
            super(MobEffectWidgetContext.Renderer.INVENTORY_COMPACT.toString());
        }
    }

    public static abstract class GuiWidgetConfig extends CompactWidgetConfig {
        @Config(description = "Draw harmful effects on a separate line from beneficial ones. This is turned on in vanilla.")
        public boolean separateEffects = false;

        public GuiWidgetConfig(String name) {
            super(name);
        }
    }

    public static class GuiSmallWidgetConfig extends GuiWidgetConfig {

        public GuiSmallWidgetConfig() {
            super(MobEffectWidgetContext.Renderer.GUI_SMALL.toString());
        }
    }

    public static class GuiCompactWigetConfig extends GuiWidgetConfig {
        @Config(description = COMPACT_DURATION_DESCRIPTION)
        public boolean compactDuration = false;

        public GuiCompactWigetConfig() {
            super(MobEffectWidgetContext.Renderer.GUI_COMPACT.toString());
        }
    }
}
