package fuzs.stylisheffects.client.handler;

import com.mojang.datafixers.util.Either;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.gui.effects.AbstractMobEffectRenderer;
import fuzs.stylisheffects.config.ClientConfig;
import fuzs.stylisheffects.config.ScreenSide;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class EffectScreenHandler {
    public static final EffectScreenHandler INSTANCE = new EffectScreenHandler();
    public static final String KEY_DEBUG_MENU_TYPE = StylishEffects.id("menu_opening").toLanguageKey("screen", "debug");

    @Nullable
    private AbstractMobEffectRenderer guiRenderer;
    @Nullable
    private AbstractMobEffectRenderer inventoryRenderer;

    private EffectScreenHandler() {
        // NO-OP
    }

    public void rebuildEffectRenderers() {
        WidgetType widgetType = StylishEffects.CONFIG.get(ClientConfig.class).guiWidgets.widgetType;
        if (widgetType != WidgetType.NONE) {
            this.guiRenderer = widgetType.factory.apply(Either.left(Minecraft.getInstance().gui));
        } else {
            this.guiRenderer = null;
        }
    }

    public List<Rect2i> getInventoryRenderAreas(Screen screen) {
        return getEffectRenderer(screen, this.inventoryRenderer).map(AbstractMobEffectRenderer::getRenderAreas)
                .orElse(List.of());
    }

    public void onClientTick(Minecraft minecraft) {
        this.createInventoryRenderer(minecraft.screen, minecraft.player);
    }

    public void onAfterInit(Minecraft minecraft, Screen screen, int screenWidth, int screenHeight, List<AbstractWidget> widgets, UnaryOperator<AbstractWidget> addWidget, Consumer<AbstractWidget> removeWidget) {
        this.createInventoryRenderer(screen, minecraft.player);
    }

    private void createInventoryRenderer(@Nullable Screen screen, @Nullable Player player) {
        // recreating this during init to adjust for screen size changes should be enough, but doesn't work for some reason for creative mode inventory,
        // therefore needs to happen every tick (since more screens might show unexpected behavior)
        // recipe book also has issues
        AbstractMobEffectRenderer renderer = screen != null ? createInventoryRendererOrFallback(screen) : null;
        if (renderer != null && player != null) {
            renderer.setActiveEffects(player.getActiveEffects());
        }

        this.inventoryRenderer = renderer;
    }

    public void renderStatusEffects(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        getEffectRenderer(minecraft.screen,
                true,
                this.guiRenderer,
                minecraft.player.getActiveEffects()).ifPresent((AbstractMobEffectRenderer renderer) -> {
            ScreenSide screenSide = StylishEffects.CONFIG.get(ClientConfig.class).guiWidgets.effectPositions.screenSide;
            renderer.setScreenDimensions(guiGraphics.guiWidth(),
                    guiGraphics.guiHeight(),
                    screenSide.isRight() ? guiGraphics.guiWidth() : 0,
                    0);
            renderer.renderEffects(guiGraphics);
        });
    }

    public void onAfterBackground(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        getEffectRenderer(screen, this.inventoryRenderer).ifPresent((AbstractMobEffectRenderer renderer) -> {
            renderer.renderEffects(guiGraphics);
            renderer.getHoveredEffectTooltip(screen, mouseX, mouseY).ifPresent((List<Component> tooltip) -> {
                if (screen.getMenu().getCarried().isEmpty()) {
                    guiGraphics.setComponentTooltipForNextFrame(screen.getFont(), tooltip, mouseX, mouseY);
                }
            });
        });
    }

    public EventResultHolder<@Nullable Screen> onScreenOpening(@Nullable Screen oldScreen, @Nullable Screen newScreen) {
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

    private static Optional<AbstractMobEffectRenderer> getEffectRenderer(Screen screen, @Nullable AbstractMobEffectRenderer effectRenderer) {
        return getEffectRenderer(screen, false, effectRenderer, null);
    }

    private static Optional<AbstractMobEffectRenderer> getEffectRenderer(@Nullable Screen screen, boolean invertSupport, @Nullable AbstractMobEffectRenderer effectRenderer, @Nullable Collection<MobEffectInstance> activeEffects) {
        AbstractContainerScreen<?> abstractContainerScreen = getScreenWithEffectsInInventory(screen);
        if (!invertSupport && abstractContainerScreen != null || invertSupport && abstractContainerScreen == null) {
            // effect renderer field may get changed during config reload from different thread, so we are extra careful when dealing with the renderer
            if (effectRenderer != null) {
                if (activeEffects != null) {
                    effectRenderer.setActiveEffects(activeEffects);
                }

                if (effectRenderer.isActive()) {
                    return Optional.of(effectRenderer);
                }
            }
        }

        return Optional.empty();
    }

    @Nullable
    private static AbstractMobEffectRenderer createInventoryRendererOrFallback(Screen screen) {
        WidgetType widgetType = StylishEffects.CONFIG.get(ClientConfig.class).inventoryWidgets.widgetType;
        AbstractContainerScreen<?> abstractContainerScreen = getScreenWithEffectsInInventory(screen);
        if (widgetType != WidgetType.NONE && abstractContainerScreen != null) {
            Consumer<AbstractMobEffectRenderer> setScreenDimensions = (AbstractMobEffectRenderer renderer) -> {
                ScreenSide screenSide = StylishEffects.CONFIG.get(ClientConfig.class).inventoryWidgets.effectPositions.screenSide;
                int leftPos = abstractContainerScreen.leftPos;
                int availableWidth = screenSide.isLeft() ? leftPos :
                        abstractContainerScreen.width - (leftPos + abstractContainerScreen.imageWidth);
                int startX = screenSide.isLeft() ? leftPos : leftPos + abstractContainerScreen.imageWidth;
                renderer.setScreenDimensions(availableWidth,
                        abstractContainerScreen.imageHeight,
                        startX,
                        abstractContainerScreen.topPos);
            };
            AbstractMobEffectRenderer renderer = widgetType.factory.apply(Either.right(abstractContainerScreen));
            setScreenDimensions.accept(renderer);
            while (!renderer.isValid()) {
                WidgetType.Factory rendererFactory = renderer.getFallbackRenderer();
                if (rendererFactory == null) {
                    return null;
                }

                renderer = rendererFactory.apply(Either.right(abstractContainerScreen));
                setScreenDimensions.accept(renderer);
            }

            return renderer;
        }

        return null;
    }

    private static @Nullable AbstractContainerScreen<?> getScreenWithEffectsInInventory(@Nullable Screen screen) {
        if (screen instanceof AbstractContainerScreen<?> abstractContainerScreen) {
            // don't use vanilla getter as it throws an UnsupportedOperationException for the player inventory
            MenuType<?> menuType = abstractContainerScreen.getMenu().menuType;
            if (menuType != null
                    && StylishEffects.CONFIG.get(ClientConfig.class).inventoryWidgets.effectMenus.menuBlacklist.contains(
                    menuType)) {
                return null;
            } else if (abstractContainerScreen.showsActiveEffects()
                    || StylishEffects.CONFIG.get(ClientConfig.class).inventoryWidgets.effectMenus.effectsEverywhere) {
                if (StylishEffects.CONFIG.get(ClientConfig.class).inventoryWidgets.effectPositions.screenSide.isLeft()
                        && abstractContainerScreen instanceof AbstractRecipeBookScreen<?> abstractRecipeBookScreen
                        && abstractRecipeBookScreen.recipeBookComponent.isVisible()) {
                    return null;
                } else {
                    return abstractContainerScreen;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
