package fuzs.stylisheffects.client.handler;

import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.gui.effects.*;
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
    private AbstractEffectRenderer guiRenderer;
    @Nullable
    private AbstractEffectRenderer inventoryRenderer;

    private EffectScreenHandler() {
        // NO-OP
    }

    public void rebuildEffectRenderers() {
        WidgetType widgetTypeType = StylishEffects.CONFIG.get(ClientConfig.class).guiRenderer().widgetTypeType;
        if (widgetTypeType != WidgetType.NONE) {
            this.guiRenderer = createRenderer(widgetTypeType, EffectRendererEnvironment.GUI);
        }
    }

    public List<Rect2i> getInventoryRenderAreas(Screen screen) {
        return getEffectRenderer(screen, this.inventoryRenderer).map(AbstractEffectRenderer::getRenderAreas)
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
        AbstractEffectRenderer renderer = screen != null ? createInventoryRendererOrFallback(screen) : null;
        if (renderer != null && player != null) {
            renderer.setActiveEffects(player.getActiveEffects());
        }

        this.inventoryRenderer = renderer;
    }

    public void renderStatusEffects(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        getEffectRenderer(minecraft.screen, true, this.guiRenderer, minecraft.player.getActiveEffects()).ifPresent(
                renderer -> {
                    ScreenSide screenSide = StylishEffects.CONFIG.get(ClientConfig.class).guiRenderer().screenSide;
                    renderer.setScreenDimensions(minecraft.gui,
                            guiGraphics.guiWidth(),
                            guiGraphics.guiHeight(),
                            screenSide.isRight() ? guiGraphics.guiWidth() : 0,
                            0,
                            screenSide);
                    renderer.renderEffects(guiGraphics, minecraft);
                });
    }

    public void onAfterBackground(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        getEffectRenderer(screen, this.inventoryRenderer).ifPresent(renderer -> {
            renderer.renderEffects(guiGraphics, screen.minecraft);
            renderer.getHoveredEffectTooltip(screen, mouseX, mouseY).ifPresent((List<Component> tooltip) -> {
                if (screen.getMenu().getCarried().isEmpty()) {
                    guiGraphics.setComponentTooltipForNextFrame(screen.getFont(), tooltip, mouseX, mouseY);
                }
            });
        });
    }

    public EventResultHolder<@Nullable Screen> onScreenOpening(@Nullable Screen oldScreen, @Nullable Screen newScreen) {
        if (newScreen instanceof AbstractContainerScreen<?> abstractContainerScreen && StylishEffects.CONFIG.get(
                ClientConfig.class).inventoryRenderer().debugContainerTypes) {
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

    private static Optional<AbstractEffectRenderer> getEffectRenderer(Screen screen, @Nullable AbstractEffectRenderer effectRenderer) {
        return getEffectRenderer(screen, false, effectRenderer, null);
    }

    private static Optional<AbstractEffectRenderer> getEffectRenderer(@Nullable Screen screen, boolean invertSupport, @Nullable AbstractEffectRenderer effectRenderer, @Nullable Collection<MobEffectInstance> activeEffects) {
        if (!invertSupport && supportsEffectsDisplay(screen) || invertSupport && !supportsEffectsDisplay(screen)) {
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

    public static AbstractEffectRenderer createRenderer(WidgetType widgetType, EffectRendererEnvironment environment) {
        return switch (widgetType) {
            case GUI_SMALL -> new GuiSmallEffectRenderer(environment);
            case GUI_COMPACT -> new GuiCompactEffectRenderer(environment);
            case INVENTORY_COMPACT -> new InventoryCompactEffectRenderer(environment);
            case INVENTORY_FULL_SIZE -> new InventoryFullSizeEffectRenderer(environment);
            default -> throw new IllegalArgumentException(String.format("Cannot create effect renderer for type %s",
                    widgetType));
        };
    }

    @Nullable
    private static AbstractEffectRenderer createInventoryRendererOrFallback(Screen screen) {
        WidgetType widgetTypeType = StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().widgetTypeType;
        if (widgetTypeType != WidgetType.NONE && supportsEffectsDisplay(screen)) {
            AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) screen;
            ScreenSide screenSide = StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().screenSide;
            Consumer<AbstractEffectRenderer> setScreenDimensions = renderer -> {
                int leftPos = containerScreen.leftPos;
                int availableWidth = !screenSide.isRight() ? leftPos :
                        containerScreen.width - (leftPos + containerScreen.imageWidth);
                int startX = !screenSide.isRight() ? leftPos : leftPos + containerScreen.imageWidth;
                renderer.setScreenDimensions(containerScreen,
                        availableWidth,
                        containerScreen.imageHeight,
                        startX,
                        containerScreen.topPos,
                        screenSide);
            };
            AbstractEffectRenderer renderer = createRenderer(widgetTypeType, EffectRendererEnvironment.INVENTORY);
            setScreenDimensions.accept(renderer);
            while (!renderer.isValid()) {
                EffectRendererEnvironment.Factory rendererFactory = renderer.getFallbackRenderer();
                if (rendererFactory == null) return null;
                renderer = rendererFactory.apply(EffectRendererEnvironment.INVENTORY);
                setScreenDimensions.accept(renderer);
            }

            return renderer;
        }

        return null;
    }

    private static boolean supportsEffectsDisplay(@Nullable Screen screen) {
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            // don't use vanilla getter as it throws an UnsupportedOperationException for the player inventory
            MenuType<?> menuType = containerScreen.getMenu().menuType;
            if (menuType != null && StylishEffects.CONFIG.get(ClientConfig.class)
                    .inventoryRenderer().menuBlacklist.contains(menuType)) {
                return false;
            }
        }

        if (screen instanceof AbstractContainerScreen && (screen.showsActiveEffects() || StylishEffects.CONFIG.get(
                ClientConfig.class).inventoryRenderer().effectsEverywhere)) {
            if (screen instanceof AbstractRecipeBookScreen<?> abstractRecipeBookScreen) {
                if (abstractRecipeBookScreen.recipeBookComponent.isVisible()) {
                    return StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().screenSide.isRight();
                }
            }

            return true;
        }

        return false;
    }
}
