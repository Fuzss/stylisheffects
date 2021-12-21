package fuzs.stylisheffects.client.handler;

import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.gui.effects.AbstractEffectRenderer;
import fuzs.stylisheffects.client.gui.effects.CompactEffectRenderer;
import fuzs.stylisheffects.client.gui.effects.VanillaEffectRenderer;
import fuzs.stylisheffects.config.ClientConfig;
import fuzs.stylisheffects.mixin.client.accessor.ContainerAccessor;
import fuzs.stylisheffects.mixin.client.accessor.DisplayEffectsScreenAccessor;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

public class EffectScreenHandler {
    @Nullable
    public static AbstractEffectRenderer inventoryRenderer;
    @Nullable
    public static AbstractEffectRenderer hudRenderer;

    public static void createHudRenderer() {
        Consumer<AbstractEffectRenderer> setScreenDimensions = renderer -> {
            Minecraft minecraft = Minecraft.getInstance();
            final ClientConfig.ScreenSide screenSide = StylishEffects.CONFIG.client().hudRenderer().screenSide;
            final MainWindow window = minecraft.getWindow();
            renderer.setScreenDimensions(minecraft.gui, window.getGuiScaledWidth(), window.getGuiScaledHeight(), screenSide.right() ? window.getGuiScaledWidth() : 0, 0, screenSide);
        };
        hudRenderer = createEffectRenderer(StylishEffects.CONFIG.client().hudRenderer().rendererType, AbstractEffectRenderer.EffectRendererType.HUD, setScreenDimensions);
    }

    @Nullable
    private static AbstractEffectRenderer createEffectRenderer(ClientConfig.EffectRenderer rendererType, AbstractEffectRenderer.EffectRendererType effectRendererType, Consumer<AbstractEffectRenderer> setScreenDimensions) {
        switch (rendererType) {
            case VANILLA:
                return createRendererOrFallback(effectRendererType, VanillaEffectRenderer::new, setScreenDimensions);
            case COMPACT:
                return createRendererOrFallback(effectRendererType, CompactEffectRenderer::new, setScreenDimensions);
        }
        return null;
    }

    private static AbstractEffectRenderer createRendererOrFallback(AbstractEffectRenderer.EffectRendererType type, Function<AbstractEffectRenderer.EffectRendererType, AbstractEffectRenderer> effectRendererFactory, Consumer<AbstractEffectRenderer> setScreenDimensions) {
        AbstractEffectRenderer renderer = effectRendererFactory.apply(type);
        setScreenDimensions.accept(renderer);
        while (!renderer.isValid()) {
            renderer = renderer.getFallbackRenderer().apply(type);
            setScreenDimensions.accept(renderer);
        }
        return renderer;
    }

    @SubscribeEvent
    public void onRenderGameOverlayPre(final RenderGameOverlayEvent.Pre evt) {
        if (evt.getType() == ElementType.POTION_ICONS) evt.setCanceled(true);
    }

    @SubscribeEvent
    public void onGuiOpen(final GuiOpenEvent evt) {
        if (evt.getGui() instanceof ContainerScreen && StylishEffects.CONFIG.client().inventoryRenderer().debugContainerTypes) {
            // don't use vanilla getter as it throws an UnsupportedOperationException for the player inventory
            final ContainerType<?> type = ((ContainerAccessor) ((ContainerScreen<?>) evt.getGui()).getMenu()).getMenuType();
            if (type != null) {
                final ITextComponent component = new StringTextComponent(ForgeRegistries.CONTAINERS.getKey(type).toString());
                Minecraft.getInstance().gui.getChat().addMessage(new TranslationTextComponent("debug.menu.opening", TextComponentUtils.wrapInSquareBrackets(component)));
            }
        }
    }

    @SubscribeEvent
    public void onInitGuiPost(final GuiScreenEvent.InitGuiEvent.Post evt) {
        inventoryRenderer = null;
    }

    private void extracted(Screen screen) {
        if (inventoryRenderer != null) return;
        if (supportsEffectsDisplay(screen)) {
            Consumer<AbstractEffectRenderer> setScreenDimensions = renderer -> {
                ContainerScreen<?> containerScreen = (ContainerScreen<?>) screen;
                final ClientConfig.ScreenSide screenSide = StylishEffects.CONFIG.client().inventoryRenderer().screenSide;
                renderer.setScreenDimensions(containerScreen, !screenSide.right() ? containerScreen.getGuiLeft() : containerScreen.width - (containerScreen.getGuiLeft() + containerScreen.getXSize()), containerScreen.getYSize(), !screenSide.right() ? containerScreen.getGuiLeft() : containerScreen.getGuiLeft() + containerScreen.getXSize(), containerScreen.getGuiTop(), screenSide);
            };
            inventoryRenderer = createEffectRenderer(StylishEffects.CONFIG.client().inventoryRenderer().rendererType, AbstractEffectRenderer.EffectRendererType.INVENTORY, setScreenDimensions);
        }
        inventoryRenderer = AbstractEffectRenderer.EMPTY;
    }

    @SubscribeEvent
    public void onPotionShift(final GuiScreenEvent.PotionShiftEvent evt) {
        evt.setCanceled(true);
    }

    @SubscribeEvent
    public void onRenderGameOverlayText(final RenderGameOverlayEvent.Text evt) {
        // use this event so potion icons are drawn behind debug menu as in vanilla
        // field may get changed during config reload from different thread
        final AbstractEffectRenderer hudRenderer = EffectScreenHandler.hudRenderer;
        if (hudRenderer != null) {
            final Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen == null || !supportsEffectsDisplay(minecraft.screen)) {
                hudRenderer.setActiveEffects(minecraft.player.getActiveEffects());
                if (hudRenderer.isActive()) {
                    hudRenderer.renderEffects(evt.getMatrixStack(), minecraft);
                }
            }
        }
    }

    @SubscribeEvent
    public void onDrawScreenPost(final GuiScreenEvent.DrawScreenEvent.Post evt) {
        // field may get changed during config reload from different thread
        final AbstractEffectRenderer inventoryRenderer = EffectScreenHandler.inventoryRenderer;
        if (inventoryRenderer != null) {
            final Minecraft minecraft = evt.getGui().getMinecraft();
            inventoryRenderer.setActiveEffects(minecraft.player.getActiveEffects());
            if (inventoryRenderer.isActive()) {
                inventoryRenderer.renderEffects(evt.getMatrixStack(), minecraft);
                inventoryRenderer.getHoveredEffectTooltip(evt.getMouseX(), evt.getMouseY()).ifPresent(tooltip -> {
                    evt.getGui().renderComponentTooltip(evt.getMatrixStack(), tooltip, evt.getMouseX(), evt.getMouseY());
                });
            }
        }
    }

    private static boolean supportsEffectsDisplay(Screen screen) {
        if (screen instanceof ContainerScreen) {
            // don't use vanilla getter as it throws an UnsupportedOperationException for the player inventory
            final ContainerType<?> type = ((ContainerAccessor) ((ContainerScreen<?>) screen).getMenu()).getMenuType();
            if (type != null && StylishEffects.CONFIG.client().inventoryRenderer().menuBlacklist.contains(type)) {
                return false;
            }
        }
        if (screen instanceof DisplayEffectsScreen) {
            return ((DisplayEffectsScreenAccessor) screen).getDoRenderEffects() || StylishEffects.CONFIG.client().inventoryRenderer().screenSide == ClientConfig.ScreenSide.RIGHT;
        }
        if (StylishEffects.CONFIG.client().inventoryRenderer().effectsEverywhere && screen instanceof ContainerScreen) {
            if (screen instanceof IRecipeShownListener) {
                if (((IRecipeShownListener) screen).getRecipeBookComponent().isVisible()) {
                    return StylishEffects.CONFIG.client().inventoryRenderer().screenSide == ClientConfig.ScreenSide.RIGHT;
                }
            }
            return true;
        }
        return false;
    }
}
