package fuzs.stylisheffects.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.puzzleslib.client.core.ClientCoreServices;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.api.client.EffectScreenHandler;
import fuzs.stylisheffects.api.client.MobEffectWidgetContext;
import fuzs.stylisheffects.client.core.ClientModServices;
import fuzs.stylisheffects.client.gui.effects.AbstractEffectRenderer;
import fuzs.stylisheffects.client.gui.effects.CompactEffectRenderer;
import fuzs.stylisheffects.client.gui.effects.VanillaEffectRenderer;
import fuzs.stylisheffects.config.ClientConfig;
import fuzs.stylisheffects.mixin.client.accessor.AbstractContainerMenuAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class EffectScreenHandlerImpl implements EffectScreenHandler {
    public static final EffectScreenHandlerImpl INSTANCE = new EffectScreenHandlerImpl();

    @Nullable
    private AbstractEffectRenderer guiRenderer;
    @Nullable
    private AbstractEffectRenderer inventoryRenderer;

    private EffectScreenHandlerImpl() {

    }

    @Override
    public void rebuildEffectRenderers() {
        MobEffectWidgetContext.Renderer rendererType = StylishEffects.CONFIG.get(ClientConfig.class).guiRenderer().rendererType;
        if (rendererType != MobEffectWidgetContext.Renderer.NONE) {
            this.guiRenderer = createRenderer(rendererType, EffectRendererEnvironment.GUI);
        }
        // we don't rebuild the inventory screen handler here, it is rebuilt when reopening the screen, should be enough
        // also needs access to the current screen
    }

    @Override
    public Optional<MobEffectWidgetContext> getInventoryHoveredEffect(Screen screen, double mouseX, double mouseY) {
        return getEffectRenderer(screen, this.inventoryRenderer, null)
                .flatMap(renderer -> renderer.getHoveredEffect((int) mouseX, (int) mouseY)
                        .map(renderer::buildContext));
    }

    @Override
    public List<Rect2i> getInventoryRenderAreas(Screen screen) {
        return getEffectRenderer(screen, this.inventoryRenderer, null)
                .map(AbstractEffectRenderer::getRenderAreas)
                .orElse(List.of());
    }

    public void onRenderMobEffectIconsOverlay(PoseStack poseStack, int screenWidth, int screenHeight) {
        final Minecraft minecraft = Minecraft.getInstance();
        getEffectRenderer(minecraft.screen, this.guiRenderer, minecraft.player.getActiveEffects()).ifPresent(renderer -> {
            MobEffectWidgetContext.ScreenSide screenSide = StylishEffects.CONFIG.get(ClientConfig.class).guiRenderer().screenSide;
            renderer.setScreenDimensions(minecraft.gui, screenWidth, screenHeight, screenSide.right() ? screenWidth : 0, 0, screenSide);
            renderer.renderEffects(poseStack, minecraft);
        });
    }

    public void onScreenInit(Screen screen) {
        this.inventoryRenderer = createInventoryRendererOrFallback(screen);
    }

    public void onDrawBackground(AbstractContainerScreen<?> screen, PoseStack poseStack, int mouseX, int mouseY) {
        // recreating this during init to adjust for screen size changes should be enough, but doesn't work for some reason for creative mode inventory,
        // therefore needs to happen every tick (since more screens might show unexpected behavior)
        Minecraft minecraft = ClientCoreServices.FACTORIES.screens().getMinecraft(screen);
        getEffectRenderer(screen, this.inventoryRenderer, minecraft.player.getActiveEffects()).ifPresent(renderer -> {
            renderer.renderEffects(poseStack, minecraft);
            TooltipFlag tooltipFlag = minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
            renderer.getHoveredEffectTooltip(mouseX, mouseY, tooltipFlag).ifPresent(tooltip -> {
                screen.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
            });
        });
    }

    public void onMouseClicked(Screen screen, double mouseX, double mouseY, int button) {
        getEffectRenderer(screen, this.inventoryRenderer, null).ifPresent(renderer -> {
            renderer.getHoveredEffect((int) mouseX, (int) mouseY).ifPresent(effectInstance -> {
                // this can be cancelled by returning false, but since we don't have any mouse clicked action by default the returned result is ignored
                ClientModServices.ABSTRACTIONS.onEffectMouseClicked(renderer.buildContext(effectInstance), screen, mouseX, mouseY, button);
            });
        });
    }

    public void onScreenOpen(Screen screen) {
        if (screen instanceof AbstractContainerScreen containerScreen && StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().debugContainerTypes) {
            // don't use vanilla getter as it throws an UnsupportedOperationException for the player inventory
            MenuType<?> type = ((AbstractContainerMenuAccessor) ((AbstractContainerScreen<?>) containerScreen).getMenu()).getMenuType();
            if (type != null) {
                Component component = Component.literal(Registry.MENU.getKey(type).toString());
                Minecraft.getInstance().gui.getChat().addMessage(Component.translatable("debug.menu.opening", ComponentUtils.wrapInSquareBrackets(component)));
            }
        }
    }

    private static Optional<AbstractEffectRenderer> getEffectRenderer(@Nullable Screen screen, @Nullable AbstractEffectRenderer effectRenderer, @Nullable Collection<MobEffectInstance> activeEffects) {
        if (supportsEffectsDisplay(screen)) {
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
        // TODO implement remaining types
        return switch (renderer) {
            case GUI_SMALL -> new CompactEffectRenderer(environment);
            case GUI_COMPACT -> new CompactEffectRenderer(environment);
            case INVENTORY_COMPACT -> new CompactEffectRenderer(environment);
            case INVENTORY_FULL_SIZE -> new VanillaEffectRenderer(environment);
            default -> throw new IllegalArgumentException(String.format("Cannot create effect renderer for type %s", renderer));
        };
    }

    @Nullable
    public static AbstractEffectRenderer createInventoryRendererOrFallback(Screen screen) {
        MobEffectWidgetContext.Renderer rendererType = StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().rendererType;
        if (rendererType != MobEffectWidgetContext.Renderer.NONE && supportsEffectsDisplay(screen)) {
            AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) screen;
            MobEffectWidgetContext.ScreenSide screenSide = StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().screenSide;
            Consumer<AbstractEffectRenderer> setScreenDimensions = renderer -> {
                int leftPos = ClientCoreServices.FACTORIES.screens().getLeftPos(containerScreen);
                renderer.setScreenDimensions(containerScreen, !screenSide.right() ? leftPos : containerScreen.width - (leftPos + ClientCoreServices.FACTORIES.screens().getImageWidth(containerScreen)), ClientCoreServices.FACTORIES.screens().getImageHeight(containerScreen), !screenSide.right() ? leftPos : leftPos + ClientCoreServices.FACTORIES.screens().getImageWidth(containerScreen), ClientCoreServices.FACTORIES.screens().getTopPos(containerScreen), screenSide);
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
        if (screen instanceof AbstractContainerScreen containerScreen) {
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
        return screen == null;
    }
}
