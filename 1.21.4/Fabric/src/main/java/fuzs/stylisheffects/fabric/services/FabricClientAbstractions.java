package fuzs.stylisheffects.fabric.services;

import fuzs.stylisheffects.api.v1.client.MobEffectWidgetContext;
import fuzs.stylisheffects.client.handler.EffectRendererEnvironment;
import fuzs.stylisheffects.fabric.api.v1.client.FabricMobEffectWidgetEvents;
import fuzs.stylisheffects.services.ClientAbstractions;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public final class FabricClientAbstractions implements ClientAbstractions {

    @Override
    public boolean isMobEffectVisibleIn(EffectRendererEnvironment effectRendererEnvironment, MobEffectInstance effectInstance) {
        return true;
    }

    @Override
    public boolean renderInventoryText(MobEffectInstance effectInstance, AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int x, int y, int blitOffset) {
        return false;
    }

    @Override
    public boolean renderInventoryIcon(MobEffectInstance effectInstance, AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int x, int y, int blitOffset) {
        return false;
    }

    @Override
    public boolean renderGuiIcon(MobEffectInstance effectInstance, Gui gui, GuiGraphics guiGraphics, int x, int y, float z, float alpha) {
        return false;
    }

    @Override
    public boolean onEffectMouseClicked(MobEffectWidgetContext context, Screen screen, double mouseX, double mouseY, int button) {
        return FabricMobEffectWidgetEvents.CLICKED.invoker()
                .onEffectMouseClicked(context, screen, mouseX, mouseY, button)
                .isInterrupt();
    }

    @Override
    public void onGatherEffectTooltipLines(MobEffectWidgetContext context, List<Component> tooltipLines, TooltipFlag tooltipFlag) {
        FabricMobEffectWidgetEvents.TOOLTIP.invoker().onGatherEffectTooltipLines(context, tooltipLines, tooltipFlag);
    }
}
