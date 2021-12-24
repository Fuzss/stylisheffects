package fuzs.stylisheffects.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.puzzleslib.client.gui.screens.Screens;
import fuzs.stylisheffects.StylishEffects;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

public class EffectScreenHandler {
    public static final EffectScreenHandler INSTANCE = new EffectScreenHandler();

    @Nullable
    private AbstractEffectRenderer hudRenderer;

    private EffectScreenHandler() {
    }

    public void createHudRenderer() {
        this.hudRenderer = StylishEffects.CONFIG.client().hudRenderer().rendererType.create(AbstractEffectRenderer.EffectRendererType.HUD);
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
                final ClientConfig.ScreenSide screenSide = StylishEffects.CONFIG.client().hudRenderer().screenSide;
                hudRenderer.setScreenDimensions(minecraft.gui, screenWidth, screenHeight, screenSide.right() ? screenWidth : 0, 0, screenSide);
                hudRenderer.renderEffects(poseStack, minecraft);
            }
        }
    }

    @SubscribeEvent
    public void onScreenOpen(final ScreenOpenEvent evt) {
        if (evt.getScreen() instanceof AbstractContainerScreen && StylishEffects.CONFIG.client().inventoryRenderer().debugContainerTypes) {
            // don't use vanilla getter as it throws an UnsupportedOperationException for the player inventory
            final MenuType<?> type = ((AbstractContainerMenuAccessor) ((AbstractContainerScreen<?>) evt.getScreen()).getMenu()).getMenuType();
            if (type != null) {
                final Component component = new TextComponent(ForgeRegistries.CONTAINERS.getKey(type).toString());
                Minecraft.getInstance().gui.getChat().addMessage(new TranslatableComponent("debug.menu.opening", ComponentUtils.wrapInSquareBrackets(component)));
            }
        }
    }

    @SubscribeEvent
    public void onDrawBackground(final ContainerScreenEvent.DrawBackground evt) {
        final AbstractContainerScreen<?> screen = evt.getContainerScreen();
        // recreating this during init to adjust for screen size changes should  be enough, but doesn't work for some reason for creative mode inventory,
        // therefore needs to happen every tick (since more screens might show unexpected behavior)
        final AbstractEffectRenderer inventoryRenderer = createRendererOrFallback(screen);
        if (inventoryRenderer == null) return;
        if (inventoryRenderer.isActive()) {
            final PoseStack poseStack = evt.getPoseStack();
            inventoryRenderer.renderEffects(poseStack, Screens.getMinecraft(screen));
            inventoryRenderer.getHoveredEffectTooltip(evt.getMouseX(), evt.getMouseY()).ifPresent(tooltip -> {
                screen.renderComponentTooltip(poseStack, tooltip, evt.getMouseX(), evt.getMouseY());
            });
        }
    }

    private static boolean supportsEffectsDisplay(Screen screen) {
        if (screen instanceof AbstractContainerScreen) {
            // don't use vanilla getter as it throws an UnsupportedOperationException for the player inventory
            final MenuType<?> type = ((AbstractContainerMenuAccessor) ((AbstractContainerScreen<?>) screen).getMenu()).getMenuType();
            if (type != null && StylishEffects.CONFIG.client().inventoryRenderer().menuBlacklist.contains(type)) {
                return false;
            }
        }
        if (screen instanceof EffectRenderingInventoryScreen || StylishEffects.CONFIG.client().inventoryRenderer().effectsEverywhere && screen instanceof AbstractContainerScreen) {
            if (screen instanceof RecipeUpdateListener listener) {
                if (listener.getRecipeBookComponent().isVisible()) {
                    return StylishEffects.CONFIG.client().inventoryRenderer().screenSide == ClientConfig.ScreenSide.RIGHT;
                }
            }
            return true;
        }
        return false;
    }

    @Nullable
    public static AbstractEffectRenderer createRendererOrFallback(Screen screen) {
        final EffectRenderer rendererType = StylishEffects.CONFIG.client().inventoryRenderer().rendererType;
        if (rendererType.isEnabled() && supportsEffectsDisplay(screen)) {
            final AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) screen;
            final ClientConfig.ScreenSide screenSide = StylishEffects.CONFIG.client().inventoryRenderer().screenSide;
            Consumer<AbstractEffectRenderer> setScreenDimensions = renderer -> {
                final int leftPos = Screens.getLeftPos(containerScreen);
                renderer.setScreenDimensions(containerScreen, !screenSide.right() ? leftPos : containerScreen.width - (leftPos + Screens.getImageWidth(containerScreen)), Screens.getImageHeight(containerScreen), !screenSide.right() ? leftPos : leftPos + Screens.getImageWidth(containerScreen), Screens.getTopPos(containerScreen), screenSide);
            };
            AbstractEffectRenderer renderer = rendererType.create(AbstractEffectRenderer.EffectRendererType.INVENTORY);
            setScreenDimensions.accept(renderer);
            while (!renderer.isValid()) {
                renderer = renderer.getFallbackRenderer().apply(AbstractEffectRenderer.EffectRendererType.INVENTORY);
                setScreenDimensions.accept(renderer);
            }
            renderer.setActiveEffects(Screens.getMinecraft(screen).player.getActiveEffects());
            return renderer;
        }
        return null;
    }

    public enum EffectRenderer {
        NONE(type -> null),
        COMPACT(CompactEffectRenderer::new),
        VANILLA(VanillaEffectRenderer::new);

        private final Function<AbstractEffectRenderer.EffectRendererType, AbstractEffectRenderer> factory;

        EffectRenderer(Function<AbstractEffectRenderer.EffectRendererType, AbstractEffectRenderer> factory) {
            this.factory = factory;
        }

        @Nullable
        public AbstractEffectRenderer create(AbstractEffectRenderer.EffectRendererType type) {
            return this.factory.apply(type);
        }

        public boolean isEnabled() {
            return this != NONE;
        }
    }
}
