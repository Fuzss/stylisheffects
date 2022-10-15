package fuzs.stylisheffects.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.puzzleslib.client.core.ClientCoreServices;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.api.client.EffectScreenHandler;
import fuzs.stylisheffects.api.client.MobEffectWidgetContext;
import fuzs.stylisheffects.client.core.ClientModServices;
import fuzs.stylisheffects.client.gui.effects.*;
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
import net.minecraft.world.entity.player.Player;
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

    public void onScreenInit(Screen screen) {
        this.createInventoryRenderer(screen, ClientCoreServices.SCREENS.getMinecraft(screen).player);
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

    public void onRenderMobEffectIconsOverlay(PoseStack poseStack, int screenWidth, int screenHeight) {
        final Minecraft minecraft = Minecraft.getInstance();
        getEffectRenderer(minecraft.screen, true, this.guiRenderer, minecraft.player.getActiveEffects()).ifPresent(renderer -> {
            MobEffectWidgetContext.ScreenSide screenSide = StylishEffects.CONFIG.get(ClientConfig.class).guiRenderer().screenSide;
            renderer.setScreenDimensions(minecraft.gui, screenWidth, screenHeight, screenSide.right() ? screenWidth : 0, 0, screenSide);
            renderer.renderEffects(poseStack, minecraft);
        });
    }

    public void onDrawBackground(AbstractContainerScreen<?> screen, PoseStack poseStack, int mouseX, int mouseY) {
        Minecraft minecraft = ClientCoreServices.SCREENS.getMinecraft(screen);
        getEffectRenderer(screen, this.inventoryRenderer).ifPresent(renderer -> {
            renderer.renderEffects(poseStack, minecraft);
        });
    }

    public void onDrawForeground(AbstractContainerScreen<?> screen, PoseStack poseStack, int mouseX, int mouseY) {
        Minecraft minecraft = ClientCoreServices.SCREENS.getMinecraft(screen);
        getEffectRenderer(screen, this.inventoryRenderer).ifPresent(renderer -> {
            TooltipFlag tooltipFlag = minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
            renderer.getHoveredEffectTooltip(mouseX, mouseY, tooltipFlag).ifPresent(tooltip -> {
                if (!screen.getMenu().getCarried().isEmpty()) return;
                // this is necessary as the foreground event runs after the container renderer has been translated to leftPos and topPos (to render slots and so on)
                // we cannot modify mouseX and mouseY that are passed to Screen::renderComponentTooltip as that will mess with tooltip text wrapping at the screen border
                poseStack.pushPose();
                poseStack.translate(-ClientCoreServices.SCREENS.getLeftPos(screen), -ClientCoreServices.SCREENS.getTopPos(screen), 0.0);
                screen.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
                poseStack.popPose();
            });
        });
    }

    public void onMouseClicked(Screen screen, double mouseX, double mouseY, int button) {
        getEffectRenderer(screen, this.inventoryRenderer).ifPresent(renderer -> {
            renderer.getHoveredEffect((int) mouseX, (int) mouseY).ifPresent(effectInstance -> {
                // this can be cancelled by returning false, but since we don't have any mouse clicked action by default the returned result is ignored
                ClientModServices.ABSTRACTIONS.onEffectMouseClicked(renderer.buildContext(effectInstance), screen, mouseX, mouseY, button);
            });
        });
    }

    public Optional<Screen> onScreenOpen(@Nullable Screen oldScreen, @Nullable Screen newScreen) {
        if (newScreen instanceof AbstractContainerScreen<?> containerScreen && StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().debugContainerTypes) {
            // don't use vanilla getter as it throws an UnsupportedOperationException for the player inventory
            MenuType<?> type = ((AbstractContainerMenuAccessor) ((AbstractContainerScreen<?>) containerScreen).getMenu()).getMenuType();
            if (type != null) {
                Component component = Component.literal(Registry.MENU.getKey(type).toString());
                Minecraft.getInstance().gui.getChat().addMessage(Component.translatable("debug.menu.opening", ComponentUtils.wrapInSquareBrackets(component)));
            }
        }
        return Optional.empty();
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
                int leftPos = ClientCoreServices.SCREENS.getLeftPos(containerScreen);
                renderer.setScreenDimensions(containerScreen, !screenSide.right() ? leftPos : containerScreen.width - (leftPos + ClientCoreServices.SCREENS.getImageWidth(containerScreen)), ClientCoreServices.SCREENS.getImageHeight(containerScreen), !screenSide.right() ? leftPos : leftPos + ClientCoreServices.SCREENS.getImageWidth(containerScreen), ClientCoreServices.SCREENS.getTopPos(containerScreen), screenSide);
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

    private static boolean supportsEffectsDisplay(Screen screen) {
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
