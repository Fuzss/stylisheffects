package fuzs.stylisheffects.client.handler;

import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.DefaultedValue;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.api.v1.client.EffectScreenHandler;
import fuzs.stylisheffects.api.v1.client.MobEffectWidgetContext;
import fuzs.stylisheffects.services.ClientAbstractions;
import fuzs.stylisheffects.client.gui.effects.*;
import fuzs.stylisheffects.config.ClientConfig;
import fuzs.stylisheffects.mixin.client.accessor.AbstractContainerMenuAccessor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class EffectScreenHandlerImpl implements EffectScreenHandler {
    public static final EffectScreenHandlerImpl INSTANCE = new EffectScreenHandlerImpl();
    public static final String KEY_DEBUG_MENU_TYPE = "debug.menu.opening";

    @Nullable
    private AbstractEffectRenderer guiRenderer;
    @Nullable
    private AbstractEffectRenderer inventoryRenderer;

    private EffectScreenHandlerImpl() {
        // NO-OP
    }

    @Override
    public void rebuildEffectRenderers() {
        MobEffectWidgetContext.Renderer rendererType = StylishEffects.CONFIG.get(ClientConfig.class).guiRenderer().rendererType;
        if (rendererType != MobEffectWidgetContext.Renderer.NONE) {
            this.guiRenderer = createRenderer(rendererType, EffectRendererEnvironment.GUI);
        }
    }

    @Override
    public Optional<MobEffectWidgetContext> getInventoryHoveredEffect(Screen screen, double mouseX, double mouseY) {
        return getEffectRenderer(screen, this.inventoryRenderer)
                .flatMap(renderer -> renderer.getHoveredEffect((int) mouseX, (int) mouseY)
                        .map(renderer::buildContext));
    }

    @Override
    public List<Rect2i> getInventoryRenderAreas(Screen screen) {
        return getEffectRenderer(screen, this.inventoryRenderer)
                .map(AbstractEffectRenderer::getRenderAreas)
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

    public EventResult onBeforeRenderGuiLayer(Minecraft minecraft, GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        // Forge messes up the gui overlay order and renders potion icons on top of the debug screen, so make a special case for that
        if (ModLoaderEnvironment.INSTANCE.getModLoader().isForgeLike() && minecraft.getDebugOverlay().showDebugScreen()) return EventResult.INTERRUPT;
        getEffectRenderer(minecraft.screen, true, this.guiRenderer, minecraft.player.getActiveEffects()).ifPresent(renderer -> {
            MobEffectWidgetContext.ScreenSide screenSide = StylishEffects.CONFIG.get(ClientConfig.class).guiRenderer().screenSide;
            renderer.setScreenDimensions(minecraft.gui, guiGraphics.guiWidth(), guiGraphics.guiHeight(), screenSide.right() ? guiGraphics.guiWidth() : 0, 0, screenSide);
            renderer.renderEffects(guiGraphics, minecraft);
        });
        return EventResult.INTERRUPT;
    }

    public void onDrawBackground(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        getEffectRenderer(screen, this.inventoryRenderer).ifPresent(renderer -> {
            renderer.renderEffects(guiGraphics, screen.minecraft);
        });
    }

    public void onDrawForeground(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        getEffectRenderer(screen, this.inventoryRenderer).ifPresent(renderer -> {
            TooltipFlag tooltipFlag = screen.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
            renderer.getHoveredEffectTooltip(mouseX, mouseY, tooltipFlag).ifPresent(tooltip -> {
                if (!screen.getMenu().getCarried().isEmpty()) return;
                // this is necessary as the foreground event runs after the container renderer has been translated to leftPos and topPos (to render slots and so on)
                // we cannot modify mouseX and mouseY that are passed to Screen::renderComponentTooltip as that will mess with tooltip text wrapping at the screen border
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(-screen.leftPos, -screen.topPos, 0.0);
                guiGraphics.renderComponentTooltip(screen.font, tooltip, mouseX, mouseY);
                guiGraphics.pose().popPose();
            });
        });
    }

    public EventResult onMouseClicked(Screen screen, double mouseX, double mouseY, int button) {
        getEffectRenderer(screen, this.inventoryRenderer).ifPresent(renderer -> {
            renderer.getHoveredEffect((int) mouseX, (int) mouseY).ifPresent(effectInstance -> {
                // this can be cancelled by returning false, but since we don't have any mouse clicked action by default the returned result is ignored
                ClientAbstractions.INSTANCE.onEffectMouseClicked(renderer.buildContext(effectInstance), screen, mouseX, mouseY, button);
            });
        });
        return EventResult.PASS;
    }

    public EventResult onScreenOpen(@Nullable Screen oldScreen, DefaultedValue<Screen> newScreen) {
        if (newScreen.get() instanceof AbstractContainerScreen<?> containerScreen && StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().debugContainerTypes) {
            // don't use vanilla getter as it throws an UnsupportedOperationException for the player inventory
            MenuType<?> type = ((AbstractContainerMenuAccessor) ((AbstractContainerScreen<?>) containerScreen).getMenu()).getMenuType();
            if (type != null) {
                Component component = Component.literal(BuiltInRegistries.MENU.getKey(type).toString());
                Minecraft.getInstance().gui.getChat().addMessage(Component.translatable(KEY_DEBUG_MENU_TYPE, ComponentUtils.wrapInSquareBrackets(component)));
            }
        }
        return EventResult.PASS;
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

    public static AbstractEffectRenderer createRenderer(MobEffectWidgetContext.Renderer renderer, EffectRendererEnvironment environment) {
        return switch (renderer) {
            case GUI_SMALL -> new GuiSmallEffectRenderer(environment);
            case GUI_COMPACT -> new GuiCompactEffectRenderer(environment);
            case INVENTORY_COMPACT -> new InventoryCompactEffectRenderer(environment);
            case INVENTORY_FULL_SIZE -> new InventoryFullSizeEffectRenderer(environment);
            default -> throw new IllegalArgumentException(String.format("Cannot create effect renderer for type %s", renderer));
        };
    }

    @Nullable
    private static AbstractEffectRenderer createInventoryRendererOrFallback(Screen screen) {
        MobEffectWidgetContext.Renderer rendererType = StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().rendererType;
        if (rendererType != MobEffectWidgetContext.Renderer.NONE && supportsEffectsDisplay(screen)) {
            AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) screen;
            MobEffectWidgetContext.ScreenSide screenSide = StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().screenSide;
            Consumer<AbstractEffectRenderer> setScreenDimensions = renderer -> {
                int leftPos = containerScreen.leftPos;
                int availableWidth =
                        !screenSide.right() ? leftPos : containerScreen.width - (leftPos + containerScreen.imageWidth);
                int startX = !screenSide.right() ? leftPos : leftPos + containerScreen.imageWidth;
                renderer.setScreenDimensions(containerScreen,
                        availableWidth, containerScreen.imageHeight, startX, containerScreen.topPos, screenSide);
            };
            AbstractEffectRenderer renderer = createRenderer(rendererType, EffectRendererEnvironment.INVENTORY);
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
            MenuType<?> type = ((AbstractContainerMenuAccessor) ((AbstractContainerScreen<?>) containerScreen).getMenu()).getMenuType();
            if (type != null && StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().menuBlacklist.contains(type)) {
                return false;
            }
        }
        if (screen instanceof EffectRenderingInventoryScreen || StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().effectsEverywhere && screen instanceof AbstractContainerScreen) {
            if (screen instanceof RecipeUpdateListener listener) {
                if (listener.getRecipeBookComponent().isVisible()) {
                    return StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().screenSide.right();
                }
            }
            return true;
        }
        return false;
    }
}
