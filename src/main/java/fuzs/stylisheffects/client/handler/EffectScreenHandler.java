package fuzs.stylisheffects.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.gui.effects.AbstractEffectRenderer;
import fuzs.stylisheffects.client.gui.effects.CompactEffectRenderer;
import fuzs.stylisheffects.client.gui.effects.VanillaEffectRenderer;
import fuzs.stylisheffects.config.ClientConfig;
import fuzs.stylisheffects.mixin.client.accessor.EffectRenderingInventoryScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;

public class EffectScreenHandler {
    public static final EffectScreenHandler INSTANCE = new EffectScreenHandler();

    @Nullable
    public AbstractEffectRenderer inventoryRenderer;
    @Nullable
    public AbstractEffectRenderer hudRenderer;

    private EffectScreenHandler() {

    }

    public void createEffectRenderers() {
        this.inventoryRenderer = createEffectRenderer(StylishEffects.CONFIG.client().inventoryRenderer().rendererType, AbstractEffectRenderer.EffectRendererType.INVENTORY);
        this.hudRenderer = createEffectRenderer(StylishEffects.CONFIG.client().hudRenderer().rendererType, AbstractEffectRenderer.EffectRendererType.HUD);
    }

    @Nullable
    private static AbstractEffectRenderer createEffectRenderer(ClientConfig.EffectRenderer rendererType, AbstractEffectRenderer.EffectRendererType effectRendererType) {
        return switch (rendererType) {
            case VANILLA -> new VanillaEffectRenderer(effectRendererType);
            case COMPACT -> new CompactEffectRenderer(effectRendererType);
            default -> null;
        };
    }

    @SubscribeEvent
    public void onPotionShift(final GuiScreenEvent.PotionShiftEvent evt) {
        evt.setCanceled(true);
    }

    @SubscribeEvent
    public void onInitGuiPost(final GuiScreenEvent.InitGuiEvent.Post evt) {
        if (evt.getGui() instanceof EffectRenderingInventoryScreen) {
            // disable vanilla rendering in creative mode inventory, survival inventory has to be disabled separately
            // this is not needed by us, we just check before rendering as survival inventory does
            ((EffectRenderingInventoryScreenAccessor) evt.getGui()).setDoRenderEffects(false);
        }
    }

    @SubscribeEvent
    public void onDrawBackground(final GuiContainerEvent.DrawBackground evt) {
        if (evt.getGuiContainer() instanceof InventoryScreen) {
            ((EffectRenderingInventoryScreenAccessor) evt.getGuiContainer()).setDoRenderEffects(false);
        }
    }

    public void onRenderGameOverlayText(PoseStack poseStack, int screenWidth, int screenHeight) {
        // use this event so potion icons are drawn behind debug menu as in vanilla
        // field may get changed during config reload from different thread
        final AbstractEffectRenderer hudRenderer = this.hudRenderer;
        if (hudRenderer != null) {
            final Minecraft minecraft = Minecraft.getInstance();
            if (!minecraft.player.getActiveEffects().isEmpty()) {
                final ClientConfig.ScreenSide screenSide = StylishEffects.CONFIG.client().hudRenderer().screenSide;
                hudRenderer.setScreenDimensions(minecraft.gui, screenWidth, screenHeight, screenSide.right() ? screenWidth : 0, 0, screenSide);
                hudRenderer.setActiveEffects(minecraft.player.getActiveEffects());
                hudRenderer.renderEffects(poseStack, minecraft);
            }
        }
    }

    @SubscribeEvent
    public void onDrawScreenPost(final GuiScreenEvent.DrawScreenEvent.Post evt) {
        // field may get changed during config reload from different thread
        final AbstractEffectRenderer inventoryRenderer = this.inventoryRenderer;
        if (inventoryRenderer != null && this.supportsEffectsDisplay(evt.getGui())) {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) evt.getGui();
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

    public boolean supportsEffectsDisplay(Screen screen) {
        if (screen instanceof EffectRenderingInventoryScreen) {
            return true;
        }
        if (StylishEffects.CONFIG.client().inventoryRenderer().effectsEverywhere && screen instanceof AbstractContainerScreen) {
            if (screen instanceof RecipeUpdateListener) {
                if (((RecipeUpdateListener) screen).getRecipeBookComponent().isVisible()) {
                    return StylishEffects.CONFIG.client().inventoryRenderer().screenSide == ClientConfig.ScreenSide.RIGHT;
                }
            }
            return true;
        }
        return false;
    }
}
