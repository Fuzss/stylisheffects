package fuzs.stylisheffects.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;
import fuzs.puzzleslib.api.config.v3.serialization.ConfigDataSet;
import fuzs.stylisheffects.client.gui.effects.AbstractMobEffectRenderer;
import fuzs.stylisheffects.client.util.ColorUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientConfig implements ConfigCore {
    @Config
    public final InventoryRendererConfig inventoryRendering = new InventoryRendererConfig();
    @Config
    public final GuiRendererConfig guiRendering = new GuiRendererConfig();
    @Config
    public final EffectWidgetConfig inventoryWidgets = new EffectWidgetConfig();
    @Config
    public final EffectWidgetConfig guiWidgets = new EffectWidgetConfig();

    public ClientConfig() {
        this.inventoryWidgets.ambientDuration = true;
        this.guiWidgets.ambientDuration = false;
    }

    public static abstract class EffectRendererConfig implements ConfigCore {
        @Config(description = {
                "The effect renderer to use.",
                "This setting might not be respected when not enough screen space is available. To force this setting disable \"allow_fallback\"."
        })
        public WidgetType widgetType = WidgetType.GUI_RECTANGLE;
        @Config(description = "Maximum amount of status effects rendered in a single row.")
        @Config.IntRange(min = 1)
        public int maxColumns = 5;
        @Config(description = "Maximum amount of status effects rendered in a single column.")
        @Config.IntRange(min = 1)
        public int maxRows = 255;
        @Config(description = "Screen side to render status effects on.")
        public ScreenSide screenSide = ScreenSide.RIGHT;
        @Config(description = "Transparency value for effect widgets.")
        @Config.DoubleRange(min = 0.0, max = 1.0)
        public double alpha = 1.0;
        @Config(description = "Empty space between individual effect widgets on the x-axis.")
        @Config.IntRange(min = 0)
        public int horizontalSpacing = 1;
        @Config(description = "Empty space between individual effect widgets on the y-axis.")
        @Config.IntRange(min = 0)
        public int verticalSpacing = 1;
        @Config(description = "Bypass vanilla's \"hideParticles\" flag which prevents a status effect from showing when set via commands.")
        public boolean ignoreHideParticles = false;
        @Config(description = "Prevent status effects with infinite duration from showing.")
        public boolean skipInfiniteEffects = false;
        @Config(description = "Allow effect widgets to use a smaller variant if not enough screen space exists (when available). Otherwise effect widgets might run off-screen.")
        public boolean allowFallback = true;
        @Config(description = "Custom scale for the effect widgets.")
        @Config.DoubleRange(min = 1.0, max = 16.0)
        public double scale = AbstractMobEffectRenderer.DEFAULT_WIDGET_SCALE;

        public boolean hoveringTooltip() {
            return false;
        }

        public boolean tooltipDuration() {
            return false;
        }

        public boolean separateEffects() {
            return false;
        }
    }

    public static class InventoryRendererConfig extends EffectRendererConfig {
        @Config
        public final EffectMenusConfig effectMenus = new EffectMenusConfig();
        @Config(description = "Show a tooltip when hovering over an effect widget.")
        boolean hoveringTooltip = true;
        @Config(description = "Show remaining status effect duration on tooltip.")
        boolean tooltipDuration = true;
        @Config(description = "Minimum screen border distance for effect widgets.")
        @Config.IntRange(min = 0)
        public int screenBorderDistance = 3;

        public InventoryRendererConfig() {
            this.screenSide = ScreenSide.LEFT;
            this.alpha = 1.0;
            this.ignoreHideParticles = true;
            this.allowFallback = true;
        }

        @Override
        public void afterConfigReload() {
            this.effectMenus.afterConfigReload();
        }

        @Override
        public boolean hoveringTooltip() {
            return this.hoveringTooltip;
        }

        @Override
        public boolean tooltipDuration() {
            return this.tooltipDuration;
        }
    }

    public static class EffectMenusConfig implements ConfigCore {
        @Config(description = "Render active status effects in every menu screen, not just in the player inventory.")
        public boolean effectsEverywhere = false;
        @Config(name = "menus_never_with_effects",
                description = "Exclude certain menus from showing active status effects. Useful when effect icons overlap with other screen elements.")
        List<String> menuBlacklistRaw = new ArrayList<>(Arrays.asList("curios:curios_container",
                "curios:curios_container_v2",
                "tconstruct:*",
                "mekanism:*"));
        @Config(description = "Print menu type to game chat whenever a new menu screen is opened. Only intended to find menu types to be added to \"menu_blacklist\".")
        public boolean debugContainerTypes = false;

        public ConfigDataSet<MenuType<?>> menuBlacklist;

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
        @Config(description = "Draw harmful effects on a separate line from beneficial ones. This is turned on in vanilla.")
        public boolean separateEffects = false;

        public GuiRendererConfig() {
            this.screenSide = ScreenSide.RIGHT;
            this.alpha = 0.85;
            this.ignoreHideParticles = false;
            this.allowFallback = false;
        }

        @Override
        public boolean separateEffects() {
            return this.separateEffects;
        }
    }

    public static class EffectWidgetConfig implements ConfigCore {
        @Config(description = "Should the effect icon start to blink when the effect is running out.")
        public boolean blinkingAlpha = true;
        @Config(description = "The effect duration color (when available).")
        public EffectColorConfig durationColor = new EffectColorConfig(DyeColor.GRAY);
        @Config(description = "Should ambient effect widgets have a cyan colored border.")
        public boolean ambientBorder = true;
        @Config(description = "Show duration for ambient effects.")
        public boolean ambientDuration = true;
        @Config(description = "Show infinity symbol for effects with infinite duration.")
        public boolean infiniteDuration = false;
        @Config(description = "Display effect duration in a more compact way. This is used automatically when the default duration formatting is too long.")
        public boolean shortenEffectDuration = false;
        @Config(description = "Top corner to draw effect amplifier in, or none.")
        public EffectAmplifier effectAmplifier = EffectAmplifier.TOP_RIGHT;
        @Config(description = "The effect amplifier color (when available).")
        public EffectColorConfig amplifierColor = new EffectColorConfig(DyeColor.WHITE);
        @Config(description = "The effect name color (when available).")
        public EffectColorConfig nameColor = new EffectColorConfig(DyeColor.WHITE);
    }

    public static class EffectColorConfig implements ConfigCore {
        @Config(description = "The text color.")
        DyeColor color;
        @Config(description = "Override the color to use the potion color from the mob effect.")
        boolean applyMobEffectColor = false;

        public EffectColorConfig(DyeColor color) {
            this.color = color;
        }

        public int getMobEffectColor(MobEffectInstance mobEffect) {
            return this.getMobEffectStyle(mobEffect).getColor().getValue();
        }

        public Style getMobEffectStyle(MobEffectInstance mobEffect) {
            return ColorUtil.getMobEffectStyle(mobEffect, this.applyMobEffectColor ? null : this.color);
        }
    }
}
