package fuzs.stylisheffects.services;

import fuzs.puzzleslib.api.core.v1.ServiceProviderHelper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.List;

public interface ClientAbstractions {
    ClientAbstractions INSTANCE = ServiceProviderHelper.load(ClientAbstractions.class);

    boolean renderInventoryText(MobEffectInstance effectInstance, AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int x, int y, int blitOffset);

    boolean renderInventoryIcon(MobEffectInstance effectInstance, AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int x, int y, int blitOffset);

    boolean renderGuiIcon(MobEffectInstance effectInstance, Gui gui, GuiGraphics guiGraphics, int x, int y, float z, float alpha);

    void onGatherEffectScreenTooltip(AbstractContainerScreen<?> screen, MobEffectInstance mobEffect, List<Component> tooltipLines);
}
