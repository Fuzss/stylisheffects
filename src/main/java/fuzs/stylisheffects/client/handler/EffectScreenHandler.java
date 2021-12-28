package fuzs.stylisheffects.client.handler;

import com.mojang.blaze3d.matrix.MatrixStack;
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
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class EffectScreenHandler {
    @Nullable
    private static AbstractEffectRenderer hudRenderer;

    public static void createHudRenderer() {
        hudRenderer = createEffectRenderer(StylishEffects.CONFIG.client().hudRenderer().rendererType, AbstractEffectRenderer.EffectRendererType.HUD);
    }

    @Nullable
    private static AbstractEffectRenderer createEffectRenderer(ClientConfig.EffectRenderer rendererType, AbstractEffectRenderer.EffectRendererType effectRendererType) {
        switch (rendererType) {
            case VANILLA:
                return new VanillaEffectRenderer(effectRendererType);
            case COMPACT:
                return new CompactEffectRenderer(effectRendererType);
            default:
                return null;
        }
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
    public void onPotionShift(final GuiScreenEvent.PotionShiftEvent evt) {
        evt.setCanceled(true);
    }

    @SubscribeEvent
    public void onRenderGameOverlayText(final RenderGameOverlayEvent.Text evt) {
        // use this event so potion icons are drawn behind debug menu as in vanilla
        // field may get changed during config reload from different thread
        final AbstractEffectRenderer hudRenderer = EffectScreenHandler.hudRenderer;
        if (hudRenderer == null) return;
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen == null || !supportsEffectsDisplay(minecraft.screen)) {
            hudRenderer.setActiveEffects(minecraft.player.getActiveEffects());
            if (hudRenderer.isActive()) {
                final ClientConfig.ScreenSide screenSide = StylishEffects.CONFIG.client().hudRenderer().screenSide;
                hudRenderer.setScreenDimensions(minecraft.gui, evt.getWindow().getGuiScaledWidth(), evt.getWindow().getGuiScaledHeight(), screenSide.right() ? evt.getWindow().getGuiScaledWidth() : 0, 0, screenSide);
                hudRenderer.renderEffects(evt.getMatrixStack(), minecraft);
            }
        }
    }

    @SubscribeEvent
    public void onDrawBackground(final GuiContainerEvent.DrawBackground evt) {
        final ContainerScreen<?> screen = evt.getGuiContainer();
        // field may get changed during config reload from different thread
        final AbstractEffectRenderer inventoryRenderer = createRendererOrFallback(screen);
        if (inventoryRenderer == null) return;
        final Minecraft minecraft = screen.getMinecraft();
        if (inventoryRenderer.isActive()) {
            final MatrixStack poseStack = evt.getMatrixStack();
            inventoryRenderer.renderEffects(poseStack, minecraft);
            inventoryRenderer.getHoveredEffectTooltip(evt.getMouseX(), evt.getMouseY()).ifPresent(tooltip -> {
                screen.renderComponentTooltip(poseStack, tooltip, evt.getMouseX(), evt.getMouseY());
            });
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

    public static AbstractEffectRenderer createRendererOrFallback(Screen screen) {
        final ClientConfig.EffectRenderer rendererType = StylishEffects.CONFIG.client().inventoryRenderer().rendererType;
        if (rendererType != ClientConfig.EffectRenderer.NONE && supportsEffectsDisplay(screen)) {
            final ContainerScreen<?> containerScreen = (ContainerScreen<?>) screen;
            final ClientConfig.ScreenSide screenSide = StylishEffects.CONFIG.client().inventoryRenderer().screenSide;
            Consumer<AbstractEffectRenderer> setScreenDimensions = renderer -> {
                renderer.setScreenDimensions(containerScreen, !screenSide.right() ? containerScreen.getGuiLeft() : containerScreen.width - (containerScreen.getGuiLeft() + containerScreen.getXSize()), containerScreen.getYSize(), !screenSide.right() ? containerScreen.getGuiLeft() : containerScreen.getGuiLeft() + containerScreen.getXSize(), containerScreen.getGuiTop(), screenSide);
            };
            AbstractEffectRenderer renderer = createEffectRenderer(rendererType, AbstractEffectRenderer.EffectRendererType.INVENTORY);
            setScreenDimensions.accept(renderer);
            while (!renderer.isValid()) {
                renderer = renderer.getFallbackRenderer().apply(AbstractEffectRenderer.EffectRendererType.INVENTORY);
                if (renderer == null) return null;
                setScreenDimensions.accept(renderer);
            }
            renderer.setActiveEffects(screen.getMinecraft().player.getActiveEffects());
            return renderer;
        }
        return null;
    }
}
