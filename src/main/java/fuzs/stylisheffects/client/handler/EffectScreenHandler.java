package fuzs.stylisheffects.client.handler;

import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.gui.effects.AbstractEffectRenderer;
import fuzs.stylisheffects.client.gui.effects.CompactEffectRenderer;
import fuzs.stylisheffects.client.gui.effects.VanillaEffectRenderer;
import fuzs.stylisheffects.config.ClientConfig;
import fuzs.stylisheffects.mixin.client.accessor.DisplayEffectsScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;

public class EffectScreenHandler {
    @Nullable
    public static AbstractEffectRenderer inventoryRenderer;
    @Nullable
    public static AbstractEffectRenderer hudRenderer;

    public static void createEffectRenderers() {
        inventoryRenderer = createEffectRenderer(StylishEffects.CONFIG.client().inventoryRenderer().rendererType, AbstractEffectRenderer.EffectRendererType.INVENTORY);
        hudRenderer = createEffectRenderer(StylishEffects.CONFIG.client().hudRenderer().rendererType, AbstractEffectRenderer.EffectRendererType.HUD);
    }

    @Nullable
    private static AbstractEffectRenderer createEffectRenderer(ClientConfig.EffectRenderer rendererType, AbstractEffectRenderer.EffectRendererType effectRendererType) {
        switch (rendererType) {
            case VANILLA:
                return new VanillaEffectRenderer(effectRendererType);
            case COMPACT:
                return new CompactEffectRenderer(effectRendererType);
        }
        return null;
    }

    @SubscribeEvent
    public void onRenderGameOverlayPre(final RenderGameOverlayEvent.Pre evt) {
        if (evt.getType() == ElementType.POTION_ICONS) evt.setCanceled(true);
    }

    @SubscribeEvent
    public void onPotionShift(final GuiScreenEvent.PotionShiftEvent evt) {
        evt.setCanceled(true);
    }

    @SubscribeEvent
    public void onInitGuiPost(final GuiScreenEvent.InitGuiEvent.Post evt) {
        if (evt.getGui() instanceof DisplayEffectsScreen) {
            // disable vanilla rendering in creative mode inventory, survival inventory has to be disabled separately
            // this is not needed by us, we just check before rendering as survival inventory does
            ((DisplayEffectsScreenAccessor) evt.getGui()).setDoRenderEffects(false);
        }
    }

    @SubscribeEvent
    public void onDrawBackground(final GuiContainerEvent.DrawBackground evt) {
        if (evt.getGuiContainer() instanceof InventoryScreen) {
            ((DisplayEffectsScreenAccessor) evt.getGuiContainer()).setDoRenderEffects(false);
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlayText(final RenderGameOverlayEvent.Text evt) {
        // use this event so potion icons are drawn behind debug menu as in vanilla
        // field may get changed during config reload from different thread
        final AbstractEffectRenderer hudRenderer = EffectScreenHandler.hudRenderer;
        if (hudRenderer != null) {
            final Minecraft minecraft = Minecraft.getInstance();
            if (!minecraft.player.getActiveEffects().isEmpty()) {
                final ClientConfig.ScreenSide screenSide = StylishEffects.CONFIG.client().hudRenderer().screenSide;
                hudRenderer.setScreenDimensions(minecraft.gui, evt.getWindow().getGuiScaledWidth(), evt.getWindow().getGuiScaledHeight(), screenSide.right() ? evt.getWindow().getGuiScaledWidth() : 0, 0, screenSide);
                hudRenderer.setActiveEffects(minecraft.player.getActiveEffects());
                hudRenderer.renderEffects(evt.getMatrixStack(), minecraft);
            }
        }
    }

    @SubscribeEvent
    public void onDrawScreenPost(final GuiScreenEvent.DrawScreenEvent.Post evt) {
        // field may get changed during config reload from different thread
        final AbstractEffectRenderer inventoryRenderer = EffectScreenHandler.inventoryRenderer;
        if (inventoryRenderer != null && supportsEffectsDisplay(evt.getGui())) {
            ContainerScreen<?> screen = (ContainerScreen<?>) evt.getGui();
            final Minecraft minecraft = screen.getMinecraft();
            if (!minecraft.player.getActiveEffects().isEmpty()) {
                final ClientConfig.ScreenSide screenSide = StylishEffects.CONFIG.client().inventoryRenderer().screenSide;
                inventoryRenderer.setScreenDimensions(screen, !screenSide.right() ? screen.getGuiLeft() : screen.width - (screen.getGuiLeft() + screen.getXSize()), screen.getYSize(), !screenSide.right() ? screen.getGuiLeft() : screen.getGuiLeft() + screen.getXSize(), screen.getGuiTop(), screenSide);
                inventoryRenderer.setActiveEffects(minecraft.player.getActiveEffects());
                inventoryRenderer.renderEffects(evt.getMatrixStack(), minecraft);
                inventoryRenderer.getHoveredEffectTooltip(evt.getMouseX(), evt.getMouseY()).ifPresent(tooltip -> evt.getGui().renderComponentTooltip(evt.getMatrixStack(), tooltip, evt.getMouseX(), evt.getMouseY()));
            }
        }
    }

    public static boolean supportsEffectsDisplay(Screen screen) {
        if (screen instanceof DisplayEffectsScreen) {
            return true;
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
