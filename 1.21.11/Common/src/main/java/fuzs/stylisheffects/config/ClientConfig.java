package fuzs.stylisheffects.config;

import fuzs.puzzleslib.api.client.gui.v2.AnchorPoint;
import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;
import fuzs.puzzleslib.api.config.v3.serialization.ConfigDataSet;
import fuzs.stylisheffects.client.gui.screens.inventory.effects.AbstractMobEffectRenderer;
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
    public final InventoryWidgetsConfig inventoryWidgets = new InventoryWidgetsConfig();
    @Config
    public final GuiWidgetsConfig guiWidgets = new GuiWidgetsConfig();

    public static abstract class EffectWidgetsConfig implements ConfigCore {
        @Config
        public final EffectPositionsConfig effectPositions = new EffectPositionsConfig();
        @Config
        public final EffectDurationConfig effectDuration = new EffectDurationConfig();
        @Config
        public final EffectAmplifierConfig effectAmplifier = new EffectAmplifierConfig();
        @Config
        public final EffectBarConfig effectBar = new EffectBarConfig();
        @Config(description = {
                "The effect renderer to use.",
                "This setting might not be respected when not enough screen space is available."
        })
        public WidgetType widgetType = WidgetType.GUI_RECTANGLE;
        @Config(description = "Bypass vanilla's \"hideParticles\" flag which prevents a status effect from showing when set via commands.")
        public boolean ignoreHideParticles = false;
        @Config(description = "Prevent status effects with infinite duration from showing.")
        public boolean skipInfiniteEffects = false;
        @Config(description = "Custom scale for the effect widgets.")
        @Config.DoubleRange(min = 1.0, max = 16.0)
        public double widgetScale = AbstractMobEffectRenderer.DEFAULT_WIDGET_SCALE;
        @Config(description = "The transparency value for effect widgets with one making it opaque, and zero making it fully invisible.")
        @Config.DoubleRange(min = 0.0, max = 1.0)
        public double widgetTransparency = 1.0;
        @Config(description = "Should the effect icon start to blink when the effect is running out.")
        public boolean blinkingSprite = true;
        @Config(description = "Should ambient effect widgets have a cyan colored border.")
        public boolean ambientBorder = true;
        @Config(description = "The effect name color (when available).")
        public EffectColorConfig nameColor = new EffectColorConfig(DyeColor.WHITE);

        public abstract int horizontalOffset();

        public abstract int verticalOffset();

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

    public static class InventoryWidgetsConfig extends EffectWidgetsConfig {
        @Config
        public final EffectMenusConfig effectMenus = new EffectMenusConfig();
        @Config
        public final EffectTooltipsConfig effectTooltips = new EffectTooltipsConfig();
        @Config(description = "Allow effect widgets to use a smaller variant (when available) if not enough screen space is available. Otherwise widgets might run off-screen.")
        public boolean supportUsingSmallerWidgets = true;
        @Config(description = "Minimum screen border distance for effect widgets.")
        @Config.IntRange(min = 0)
        public int screenBorderDistance = 3;

        public InventoryWidgetsConfig() {
            this.effectDuration.ambientDuration = true;
            this.effectPositions.screenSide = ScreenSide.LEFT;
            this.widgetTransparency = 1.0;
            this.ignoreHideParticles = true;
        }

        @Override
        public void afterConfigReload() {
            this.effectMenus.afterConfigReload();
        }

        @Override
        public int horizontalOffset() {
            return this.screenBorderDistance;
        }

        @Override
        public int verticalOffset() {
            return this.screenBorderDistance;
        }

        @Override
        public boolean hoveringTooltip() {
            return this.effectTooltips.hoveringTooltip;
        }

        @Override
        public boolean tooltipDuration() {
            return this.effectTooltips.tooltipDuration;
        }
    }

    public static class GuiWidgetsConfig extends EffectWidgetsConfig {
        @Config(description = "Horizontal offset on x-axis.")
        @Config.IntRange(min = 0)
        public int horizontalOffset = 3;
        @Config(description = "Vertical offset on y-axis.")
        @Config.IntRange(min = 0)
        public int verticalOffset = 3;
        @Config(description = "Draw harmful effects on a separate line from beneficial ones. This is turned on in vanilla.")
        public boolean separateEffects = false;

        public GuiWidgetsConfig() {
            this.effectDuration.ambientDuration = false;
            this.effectPositions.screenSide = ScreenSide.RIGHT;
            this.widgetTransparency = 0.85;
            this.ignoreHideParticles = false;
        }

        @Override
        public int horizontalOffset() {
            return this.horizontalOffset;
        }

        @Override
        public int verticalOffset() {
            return this.verticalOffset;
        }

        @Override
        public boolean separateEffects() {
            return this.separateEffects;
        }
    }

    public static class EffectPositionsConfig implements ConfigCore {
        @Config(description = "Maximum amount of status effects rendered in a single row.")
        @Config.IntRange(min = 1)
        public int maxColumns = 5;
        @Config(description = "Maximum amount of status effects rendered in a single column.")
        @Config.IntRange(min = 1)
        public int maxRows = 255;
        @Config(description = "Screen side to render status effects on.")
        public ScreenSide screenSide = ScreenSide.RIGHT;
        @Config(description = "Empty space between individual effect widgets on the x-axis.")
        @Config.IntRange(min = 0)
        public int horizontalSpacing = 1;
        @Config(description = "Empty space between individual effect widgets on the y-axis.")
        @Config.IntRange(min = 0)
        public int verticalSpacing = 1;
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
        @Config(description = "Print menu type to the game chat whenever a new menu screen is opened. Only intended for finding menu types to prevent effect widget rendering in.")
        public boolean debugContainerTypes = false;

        public ConfigDataSet<MenuType<?>> menuBlacklist;

        @Override
        public void afterConfigReload() {
            this.menuBlacklist = ConfigDataSet.from(Registries.MENU, this.menuBlacklistRaw);
        }
    }

    public static class EffectTooltipsConfig implements ConfigCore {
        @Config(description = "Show a tooltip when hovering over an effect widget.")
        boolean hoveringTooltip = true;
        @Config(description = "Show remaining status effect duration on tooltip.")
        boolean tooltipDuration = true;
    }

    public static class EffectDurationConfig implements ConfigCore {
        @Config(description = "The effect duration color (when available).")
        public EffectColorConfig durationColor = new EffectColorConfig(DyeColor.GRAY, true);
        @Config(description = "Show duration for ambient effects.")
        public boolean ambientDuration = true;
        @Config(description = "Show infinity symbol for effects with infinite duration.")
        public boolean infiniteDuration = false;
        @Config(description = "Display effect duration in a more compact way. This is used automatically when the default duration formatting is too long.")
        public boolean shortenEffectDuration = false;
    }

    public static class EffectAmplifierConfig implements ConfigCore {
        @Config(description = "Show a tiny effect effect amplifier on effect widgets (when available).")
        public boolean effectAmplifier = true;
        @Config(description = "The position on the effect widget to draw the tiny effect amplifier at.")
        public AnchorPoint amplifierPosition = AnchorPoint.TOP_RIGHT;
        @Config(description = "The effect amplifier color.")
        public EffectColorConfig amplifierColor = new EffectColorConfig(DyeColor.WHITE);
    }

    public static class EffectBarConfig implements ConfigCore {
        @Config(description = "Show a slim bar on effect widgets representing the passing duration of the effect.")
        public boolean effectBar = true;
        @Config(description = "The effect bar color.")
        public EffectColorConfig barColor = new EffectColorConfig();
        @Config(description = "The position of the bar on the effect widget.")
        public BarPosition barPosition = BarPosition.RIGHT;
        @Config(description = "The transparency value for the bar with one making it opaque, and zero making it fully invisible.")
        @Config.DoubleRange(min = 0.0, max = 1.0)
        public double barTransparency = 0.5;
        @Config(description = "Switch the direction in which the effect bar is shrinking to.")
        public boolean flipAxis = false;
        @Config(description = {
                "Include bars for effects that were already on the player before the starting duration could be stored.",
                "This is usually the case when logging in."
        })
        public boolean unknownStartingDuration = false;
        @Config(description = "Show bar for ambient effects.")
        public boolean ambientBar = false;
    }

    public static class EffectColorConfig implements ConfigCore {
        @Config(description = "The text color.")
        DyeColor color;
        @Config(description = "Override the color to use the potion color from the mob effect.")
        boolean applyMobEffectColor;

        public EffectColorConfig() {
            this(DyeColor.WHITE, true);
        }

        public EffectColorConfig(DyeColor color) {
            this(color, false);
        }

        public EffectColorConfig(DyeColor color, boolean applyMobEffectColor) {
            this.color = color;
            this.applyMobEffectColor = applyMobEffectColor;
        }

        public int getMobEffectColor(MobEffectInstance mobEffect) {
            return this.getMobEffectStyle(mobEffect).getColor().getValue();
        }

        public Style getMobEffectStyle(MobEffectInstance mobEffect) {
            return ColorUtil.getMobEffectStyle(mobEffect, this.applyMobEffectColor ? null : this.color);
        }
    }
}
