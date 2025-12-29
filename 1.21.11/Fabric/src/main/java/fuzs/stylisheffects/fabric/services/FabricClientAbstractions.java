package fuzs.stylisheffects.fabric.services;

import fuzs.puzzleslib.fabric.api.client.event.v1.FabricGuiEvents;
import fuzs.stylisheffects.services.ClientAbstractions;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.List;

public final class FabricClientAbstractions implements ClientAbstractions {

    @Override
    public boolean renderInventoryText(MobEffectInstance mobEffect, AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int x, int y, int blitOffset) {
        return false;
    }

    @Override
    public boolean renderInventoryIcon(MobEffectInstance mobEffect, AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int x, int y, int blitOffset) {
        return false;
    }

    @Override
    public boolean renderGuiIcon(MobEffectInstance mobEffect, Gui gui, GuiGraphics guiGraphics, int x, int y, float z, float alpha) {
        return false;
    }

    @Override
    public void onGatherEffectScreenTooltip(AbstractContainerScreen<?> screen, MobEffectInstance mobEffect, List<Component> tooltipLines) {
        FabricGuiEvents.GATHER_EFFECT_SCREEN_TOOLTIP.invoker()
                .onGatherEffectScreenTooltip(screen, mobEffect, tooltipLines);
    }
}
