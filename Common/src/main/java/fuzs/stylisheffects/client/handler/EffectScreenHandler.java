package fuzs.stylisheffects.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.puzzleslib.client.core.ClientCoreServices;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.gui.effects.AbstractEffectRenderer;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class EffectScreenHandler {
    public static final EffectScreenHandler INSTANCE = new EffectScreenHandler();

    @Nullable
    private AbstractEffectRenderer hudRenderer;

    private EffectScreenHandler() {
    }

    public void createHudRenderer() {
        this.hudRenderer = StylishEffects.CONFIG.get(ClientConfig.class).hudRenderer().rendererType.create(EffectRendererEnvironment.GUI);
    }

    public void onRenderGameOverlayText(PoseStack poseStack, int screenWidth, int screenHeight) {
        // use this event so potion icons are drawn behind debug menu as in vanilla
        // field may get changed during config reload from different thread
        final AbstractEffectRenderer hudRenderer = this.hudRenderer;
        if (hudRenderer == null) return;
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen == null || !supportsEffectsDisplay(minecraft.screen)) {
            hudRenderer.setActiveEffects(minecraft.player.getActiveEffects());
            if (hudRenderer.isActive()) {
                final ClientConfig.ScreenSide screenSide = StylishEffects.CONFIG.get(ClientConfig.class).hudRenderer().screenSide;
                hudRenderer.setScreenDimensions(minecraft.gui, screenWidth, screenHeight, screenSide.right() ? screenWidth : 0, 0, screenSide);
                hudRenderer.renderEffects(poseStack, minecraft);
            }
        }
    }

    public void onScreenOpen(Screen screen) {
        if (screen instanceof AbstractContainerScreen containerScreen && StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().debugContainerTypes) {
            MenuType<?> type = getScreenMenuType(containerScreen);
            if (type != null) {
                Component component = Component.literal(Registry.MENU.getKey(type).toString());
                Minecraft.getInstance().gui.getChat().addMessage(Component.translatable("debug.menu.opening", ComponentUtils.wrapInSquareBrackets(component)));
            }
        }
    }

    public void onDrawBackground(AbstractContainerScreen<?> screen, PoseStack poseStack, int mouseX, int mouseY) {
        // recreating this during init to adjust for screen size changes should be enough, but doesn't work for some reason for creative mode inventory,
        // therefore needs to happen every tick (since more screens might show unexpected behavior)
        final AbstractEffectRenderer inventoryRenderer = createRendererOrFallback(screen);
        if (inventoryRenderer == null) return;
        if (inventoryRenderer.isActive()) {
            inventoryRenderer.renderEffects(poseStack, ClientCoreServices.FACTORIES.screens().getMinecraft(screen));
            inventoryRenderer.getHoveredEffectTooltip(mouseX, mouseY).ifPresent(tooltip -> {
                screen.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
            });
        }
    }

    private static boolean supportsEffectsDisplay(Screen screen) {
        if (screen instanceof AbstractContainerScreen containerScreen) {
            MenuType<?> type = getScreenMenuType(containerScreen);
            if (type != null && StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().menuBlacklist.contains(type)) {
                return false;
            }
        }
        if (screen instanceof EffectRenderingInventoryScreen || StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().effectsEverywhere && screen instanceof AbstractContainerScreen) {
            if (screen instanceof RecipeUpdateListener listener) {
                if (listener.getRecipeBookComponent().isVisible()) {
                    return StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().screenSide == ClientConfig.ScreenSide.RIGHT;
                }
            }
            return true;
        }
        return false;
    }

    @Nullable
    private static MenuType<?> getScreenMenuType(AbstractContainerScreen<?> screen) {
        try {
            return screen.getMenu().getType();
        } catch (UnsupportedOperationException ignored) {

        }
        return null;
    }

    @Nullable
    public static AbstractEffectRenderer createRendererOrFallback(Screen screen) {
        final EffectRenderer rendererType = StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().rendererType;
        if (rendererType.isEnabled() && supportsEffectsDisplay(screen)) {
            final AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) screen;
            final ClientConfig.ScreenSide screenSide = StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().screenSide;
            Consumer<AbstractEffectRenderer> setScreenDimensions = renderer -> {
                final int leftPos = ClientCoreServices.FACTORIES.screens().getLeftPos(containerScreen);
                renderer.setScreenDimensions(containerScreen, !screenSide.right() ? leftPos : containerScreen.width - (leftPos + ClientCoreServices.FACTORIES.screens().getImageWidth(containerScreen)), ClientCoreServices.FACTORIES.screens().getImageHeight(containerScreen), !screenSide.right() ? leftPos : leftPos + ClientCoreServices.FACTORIES.screens().getImageWidth(containerScreen), ClientCoreServices.FACTORIES.screens().getTopPos(containerScreen), screenSide);
            };
            AbstractEffectRenderer renderer = rendererType.create(EffectRendererEnvironment.INVENTORY);
            setScreenDimensions.accept(renderer);
            while (!renderer.isValid()) {
                EffectRendererEnvironment.Factory rendererFactory = renderer.getFallbackRenderer();
                if (rendererFactory == null) return null;
                renderer = rendererFactory.apply(EffectRendererEnvironment.INVENTORY);
                setScreenDimensions.accept(renderer);
            }
            renderer.setActiveEffects(ClientCoreServices.FACTORIES.screens().getMinecraft(screen).player.getActiveEffects());
            return renderer;
        }
        return null;
    }
}
