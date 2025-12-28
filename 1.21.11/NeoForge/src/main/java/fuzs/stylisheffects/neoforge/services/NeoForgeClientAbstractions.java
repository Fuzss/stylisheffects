package fuzs.stylisheffects.neoforge.services;

import fuzs.stylisheffects.services.ClientAbstractions;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.client.event.GatherEffectScreenTooltipsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

public final class NeoForgeClientAbstractions implements ClientAbstractions {

    @Override
    public boolean renderInventoryText(MobEffectInstance effectInstance, AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int x, int y, int blitOffset) {
        return IClientMobEffectExtensions.of(effectInstance)
                .renderInventoryText(effectInstance, screen, guiGraphics, x, y, blitOffset);
    }

    @Override
    public boolean renderInventoryIcon(MobEffectInstance effectInstance, AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int x, int y, int blitOffset) {
        return IClientMobEffectExtensions.of(effectInstance)
                .renderInventoryIcon(effectInstance, screen, guiGraphics, x, y, blitOffset);
    }

    @Override
    public boolean renderGuiIcon(MobEffectInstance effectInstance, Gui gui, GuiGraphics guiGraphics, int x, int y, float z, float alpha) {
        return IClientMobEffectExtensions.of(effectInstance)
                .renderGuiIcon(effectInstance, gui, guiGraphics, x, y, z, alpha);
    }

    @Override
    public void onGatherEffectScreenTooltip(AbstractContainerScreen<?> screen, MobEffectInstance mobEffect, List<Component> tooltipLines) {
        NeoForge.EVENT_BUS.post(new GatherEffectScreenTooltipsEvent(screen, mobEffect, tooltipLines));
    }
}
