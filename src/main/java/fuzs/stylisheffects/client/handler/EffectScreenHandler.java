package fuzs.stylisheffects.client.handler;

import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.gui.effects.AbstractEffectRenderer;
import fuzs.stylisheffects.client.gui.effects.CompactEffectRenderer;
import fuzs.stylisheffects.client.gui.effects.VanillaEffectRenderer;
import fuzs.stylisheffects.config.ClientConfig;
import fuzs.stylisheffects.mixin.client.accessor.ContainerAccessor;
import fuzs.stylisheffects.mixin.client.accessor.DisplayEffectsScreenAccessor;
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
    public void onGuiOpen(final GuiOpenEvent evt) {
        if (evt.getGui() instanceof ContainerScreen)
            if (StylishEffects.CONFIG.client().inventoryRenderer().debugContainerTypes) {
                // don't use vanilla getter as it throws an UnsupportedOperationException for the player inventory
                final ContainerType<?> type = ((ContainerAccessor) ((ContainerScreen<?>) evt.getGui()).getMenu()).getMenuType();
                if (type != null) {
                    final ITextComponent component = new StringTextComponent(ForgeRegistries.CONTAINERS.getKey(type).toString());
                    Minecraft.getInstance().gui.getChat().addMessage(new TranslationTextComponent("debug.menu.opening", TextComponentUtils.wrapInSquareBrackets(component)));
                }
            }
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
            hudRenderer.setActiveEffects(minecraft.player.getActiveEffects());
            if (hudRenderer.isActive()) {
                final ClientConfig.ScreenSide screenSide = StylishEffects.CONFIG.client().hudRenderer().screenSide;
                hudRenderer.setScreenDimensions(minecraft.gui, evt.getWindow().getGuiScaledWidth(), evt.getWindow().getGuiScaledHeight(), screenSide.right() ? evt.getWindow().getGuiScaledWidth() : 0, 0, screenSide);
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
            inventoryRenderer.setActiveEffects(minecraft.player.getActiveEffects());
            if (inventoryRenderer.isActive()) {
                final ClientConfig.ScreenSide screenSide = StylishEffects.CONFIG.client().inventoryRenderer().screenSide;
                inventoryRenderer.setScreenDimensions(screen, !screenSide.right() ? screen.getGuiLeft() : screen.width - (screen.getGuiLeft() + screen.getXSize()), screen.getYSize(), !screenSide.right() ? screen.getGuiLeft() : screen.getGuiLeft() + screen.getXSize(), screen.getGuiTop(), screenSide);
                inventoryRenderer.renderEffects(evt.getMatrixStack(), minecraft);
                inventoryRenderer.getHoveredEffectTooltip(evt.getMouseX(), evt.getMouseY()).ifPresent(tooltip -> {
                    evt.getGui().renderComponentTooltip(evt.getMatrixStack(), tooltip, evt.getMouseX(), evt.getMouseY());
                });
            }
        }
    }

    public static boolean supportsEffectsDisplay(Screen screen) {
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
