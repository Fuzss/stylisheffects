package fuzs.stylisheffects.services;

import fuzs.puzzleslib.api.core.v1.ServiceProviderHelper;
import fuzs.stylisheffects.api.v1.client.MobEffectWidgetContext;
import fuzs.stylisheffects.client.handler.EffectRendererEnvironment;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public interface ClientAbstractions {
    ClientAbstractions INSTANCE = ServiceProviderHelper.load(ClientAbstractions.class);

    boolean isMobEffectVisibleIn(EffectRendererEnvironment effectRendererEnvironment, MobEffectInstance effectInstance);

    boolean renderInventoryText(MobEffectInstance effectInstance, AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int x, int y, int blitOffset);

    boolean renderInventoryIcon(MobEffectInstance effectInstance, AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int x, int y, int blitOffset);

    boolean renderGuiIcon(MobEffectInstance effectInstance, Gui gui, GuiGraphics guiGraphics, int x, int y, float z, float alpha);

    boolean onEffectMouseClicked(MobEffectWidgetContext context, Screen screen, double mouseX, double mouseY, int button);

    void onGatherEffectTooltipLines(MobEffectWidgetContext context, List<Component> tooltipLines, TooltipFlag tooltipFlag);
}
