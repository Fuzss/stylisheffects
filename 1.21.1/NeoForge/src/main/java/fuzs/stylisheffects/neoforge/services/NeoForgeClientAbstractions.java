package fuzs.stylisheffects.neoforge.services;

import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.neoforge.api.event.v1.core.NeoForgeEventInvokerRegistry;
import fuzs.stylisheffects.api.v1.client.MobEffectWidgetContext;
import fuzs.stylisheffects.api.v1.client.MobEffectWidgetEvents;
import fuzs.stylisheffects.neoforge.api.v1.client.NeoForgeMobEffectWidgetEvent;
import fuzs.stylisheffects.services.ClientAbstractions;
import fuzs.stylisheffects.client.handler.EffectRendererEnvironment;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

public final class NeoForgeClientAbstractions implements ClientAbstractions {

    @Override
    public boolean isMobEffectVisibleIn(EffectRendererEnvironment effectRendererEnvironment, MobEffectInstance effectInstance) {
        return switch (effectRendererEnvironment) {
            case GUI -> IClientMobEffectExtensions.of(effectInstance).isVisibleInGui(effectInstance);
            case INVENTORY -> IClientMobEffectExtensions.of(effectInstance).isVisibleInInventory(effectInstance);
        };
    }

    @Override
    public boolean renderInventoryText(MobEffectInstance effectInstance, EffectRenderingInventoryScreen<?> screen, GuiGraphics guiGraphics, int x, int y, int blitOffset) {
        return IClientMobEffectExtensions.of(effectInstance).renderInventoryText(effectInstance, screen, guiGraphics, x, y, blitOffset);
    }

    @Override
    public boolean renderInventoryIcon(MobEffectInstance effectInstance, EffectRenderingInventoryScreen<?> screen, GuiGraphics guiGraphics, int x, int y, int blitOffset) {
        return IClientMobEffectExtensions.of(effectInstance).renderInventoryIcon(effectInstance, screen, guiGraphics, x, y, blitOffset);
    }

    @Override
    public boolean renderGuiIcon(MobEffectInstance effectInstance, Gui gui, GuiGraphics guiGraphics, int x, int y, float z, float alpha) {
        return IClientMobEffectExtensions.of(effectInstance).renderGuiIcon(effectInstance, gui, guiGraphics, x, y, z, alpha);
    }

    @Override
    public boolean onEffectMouseClicked(MobEffectWidgetContext context, Screen screen, double mouseX, double mouseY, int button) {
        return NeoForge.EVENT_BUS.post(new NeoForgeMobEffectWidgetEvent.MouseClicked(context, screen, mouseX, mouseY, button)).isCanceled();
    }

    @Override
    public void onGatherEffectTooltipLines(MobEffectWidgetContext context, List<Component> tooltipLines, TooltipFlag tooltipFlag) {
        NeoForge.EVENT_BUS.post(new NeoForgeMobEffectWidgetEvent.EffectTooltip(context, tooltipLines, tooltipFlag));
    }

    @Override
    public void registerEventHandlers() {
        NeoForgeEventInvokerRegistry.INSTANCE.register(MobEffectWidgetEvents.MouseClicked.class,
                NeoForgeMobEffectWidgetEvent.MouseClicked.class,
                (MobEffectWidgetEvents.MouseClicked callback, NeoForgeMobEffectWidgetEvent.MouseClicked evt) -> {
                    EventResult result = callback.onEffectMouseClicked(evt.getContext(),
                            evt.getScreen(),
                            evt.getMouseX(),
                            evt.getMouseY(),
                            evt.getButton());
                    if (result.isInterrupt()) evt.setCanceled(true);
                });
        NeoForgeEventInvokerRegistry.INSTANCE.register(MobEffectWidgetEvents.EffectTooltip.class,
                NeoForgeMobEffectWidgetEvent.EffectTooltip.class,
                (MobEffectWidgetEvents.EffectTooltip callback, NeoForgeMobEffectWidgetEvent.EffectTooltip evt) -> {
                    callback.onGatherEffectTooltipLines(evt.getContext(), evt.getTooltipLines(), evt.getTooltipFlag());
                });
    }
}
