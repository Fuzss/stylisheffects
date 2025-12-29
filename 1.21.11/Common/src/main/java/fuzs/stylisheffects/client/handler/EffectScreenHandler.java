package fuzs.stylisheffects.client.handler;

import com.mojang.datafixers.util.Either;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import fuzs.puzzleslib.api.event.v1.data.MutableBoolean;
import fuzs.puzzleslib.api.event.v1.data.MutableInt;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.gui.screens.inventory.effects.AbstractMobEffectRenderer;
import fuzs.stylisheffects.config.ClientConfig;
import fuzs.stylisheffects.config.WidgetType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.MenuType;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class EffectScreenHandler {
    public static final String KEY_DEBUG_MENU_TYPE = StylishEffects.id("menu_opening").toLanguageKey("screen", "debug");

    @Nullable
    private static AbstractMobEffectRenderer guiMobEffectRenderer;
    @Nullable
    private static AbstractMobEffectRenderer inventoryMobEffectRenderer;

    private EffectScreenHandler() {
        // NO-OP
    }

    public static void rebuildGuiRenderer(Minecraft minecraft) {
        // This can be null when the Minecraft class is not yet fully initialized.
        if (minecraft.gui != null) {
            WidgetType widgetType = StylishEffects.CONFIG.get(ClientConfig.class).guiWidgets.widgetType;
            if (widgetType != WidgetType.NONE) {
                guiMobEffectRenderer = widgetType.factory.apply(Either.left(minecraft.gui));
            } else {
                guiMobEffectRenderer = null;
            }
        }
    }

    public static void renderStatusEffects(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (guiMobEffectRenderer != null && !isScreenWithEffectsInInventory(minecraft.screen)) {
            List<MobEffectInstance> mobEffects = guiMobEffectRenderer.getMobEffects(minecraft.player);
            if (!mobEffects.isEmpty()) {
                guiMobEffectRenderer.init();
                guiMobEffectRenderer.renderEffectWidgets(guiGraphics, mobEffects);
            }
        }
    }

    public static void onAfterInit(Minecraft minecraft, AbstractContainerScreen<?> screen, int screenWidth, int screenHeight, List<AbstractWidget> widgets, UnaryOperator<AbstractWidget> addWidget, Consumer<AbstractWidget> removeWidget) {
        // This ensures the init method was called for the current screen via Minecraft::setScreen.
        // When opening the creative mode inventory, there always is a trailing init call for the survival inventory that messes this up otherwise.
        if (screen == screen.minecraft.screen) {
            inventoryMobEffectRenderer = createInventoryRenderer(screen);
        }
    }

    public static void onRemove(AbstractContainerScreen<?> screen) {
        if (screen == screen.minecraft.screen) {
            inventoryMobEffectRenderer = null;
        }
    }

    @Nullable
    private static AbstractMobEffectRenderer createInventoryRenderer(AbstractContainerScreen<?> screen) {
        WidgetType widgetType = StylishEffects.CONFIG.get(ClientConfig.class).inventoryWidgets.widgetType;
        if (widgetType != WidgetType.NONE && isScreenWithEffectsInInventory(screen)) {
            AbstractMobEffectRenderer mobEffectRenderer = widgetType.factory.apply(Either.right(screen));
            if (StylishEffects.CONFIG.get(ClientConfig.class).inventoryWidgets.supportUsingSmallerWidgets) {
                while (!mobEffectRenderer.hasEnoughSpace()) {
                    WidgetType.Factory widgetTypeFactory = mobEffectRenderer.getFallbackRenderer();
                    if (widgetTypeFactory == null) {
                        return null;
                    }

                    mobEffectRenderer = widgetTypeFactory.apply(Either.right(screen));
                }
            }

            return mobEffectRenderer;
        }

        return null;
    }

    private static boolean isScreenWithEffectsInInventory(@Nullable Screen screen) {
        if (screen instanceof AbstractContainerScreen<?> abstractContainerScreen) {
            // don't use vanilla getter as it throws an UnsupportedOperationException for the player inventory
            MenuType<?> menuType = abstractContainerScreen.getMenu().menuType;
            if (menuType != null
                    && StylishEffects.CONFIG.get(ClientConfig.class).inventoryWidgets.effectMenus.menuBlacklist.contains(
                    menuType)) {
                return false;
            } else if (abstractContainerScreen.showsActiveEffects()
                    || StylishEffects.CONFIG.get(ClientConfig.class).inventoryWidgets.effectMenus.effectsEverywhere) {
                return !StylishEffects.CONFIG.get(ClientConfig.class).inventoryWidgets.effectPositions.screenSide.isLeft()
                        || !(abstractContainerScreen instanceof AbstractRecipeBookScreen<?> abstractRecipeBookScreen)
                        || !abstractRecipeBookScreen.recipeBookComponent.isVisible();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static void onAfterBackground(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (inventoryMobEffectRenderer != null) {
            List<MobEffectInstance> mobEffects = inventoryMobEffectRenderer.getMobEffects(screen.minecraft.player);
            if (!mobEffects.isEmpty()) {
                inventoryMobEffectRenderer.init();
                inventoryMobEffectRenderer.renderEffectWidgets(guiGraphics, mobEffects);
                if (screen.getMenu().getCarried().isEmpty()) {
                    inventoryMobEffectRenderer.getHoveredEffectTooltip(mouseX, mouseY, mobEffects)
                            .ifPresent((List<Component> tooltip) -> {
                                guiGraphics.setComponentTooltipForNextFrame(screen.getFont(), tooltip, mouseX, mouseY);
                            });
                }
            }
        }
    }

    public static List<Rect2i> getGuiExtraAreas(AbstractContainerScreen<?> screen) {
        if (inventoryMobEffectRenderer != null) {
            List<MobEffectInstance> mobEffects = inventoryMobEffectRenderer.getMobEffects(screen.minecraft.player);
            if (!mobEffects.isEmpty()) {
                inventoryMobEffectRenderer.init();
                return inventoryMobEffectRenderer.getGuiExtraAreas(mobEffects);
            }
        }

        return Collections.emptyList();
    }

    public static EventResult onPrepareInventoryMobEffects(Screen screen, int maxWidth, MutableBoolean smallWidgets, MutableInt horizontalPosition) {
        return EventResult.INTERRUPT;
    }

    public static EventResultHolder<@Nullable Screen> onScreenOpening(@Nullable Screen oldScreen, @Nullable Screen newScreen) {
        if (newScreen instanceof AbstractContainerScreen<?> abstractContainerScreen && StylishEffects.CONFIG.get(
                ClientConfig.class).inventoryWidgets.effectMenus.debugContainerTypes) {
            // don't use vanilla getter as it throws an UnsupportedOperationException for the player inventory
            MenuType<?> menuType = abstractContainerScreen.getMenu().menuType;
            if (menuType != null) {
                Component component = Component.literal(BuiltInRegistries.MENU.getKey(menuType).toString());
                Minecraft.getInstance().gui.getChat()
                        .addMessage(Component.translatable(KEY_DEBUG_MENU_TYPE,
                                ComponentUtils.wrapInSquareBrackets(component)));
            }
        }

        return EventResultHolder.pass();
    }
}
