package fuzs.stylisheffects.client.core;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.stylisheffects.api.client.MobEffectWidgetContext;
import fuzs.stylisheffects.api.client.event.MobEffectWidgetEvents;
import fuzs.stylisheffects.client.handler.EffectRendererEnvironment;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class FabricClientAbstractions implements ClientAbstractions {

    @Override
    public boolean isMobEffectVisibleIn(EffectRendererEnvironment effectRendererEnvironment, MobEffectInstance effectInstance) {
        return true;
    }

    @Override
    public boolean renderInventoryText(MobEffectInstance effectInstance, EffectRenderingInventoryScreen<?> screen, PoseStack poseStack, int x, int y, int blitOffset) {
        return false;
    }

    @Override
    public boolean renderInventoryIcon(MobEffectInstance effectInstance, EffectRenderingInventoryScreen<?> screen, PoseStack poseStack, int x, int y, int blitOffset) {
        return false;
    }

    @Override
    public boolean renderGuiIcon(MobEffectInstance effectInstance, Gui gui, PoseStack poseStack, int x, int y, float z, float alpha) {
        return false;
    }

    @Override
    public boolean onEffectMouseClicked(MobEffectWidgetContext context, Screen screen, double mouseX, double mouseY, int button) {
        return MobEffectWidgetEvents.CLICKED.invoker().onEffectMouseClicked(context, screen, mouseX, mouseY, button);
    }

    @Override
    public void onGatherEffectTooltipLines(MobEffectWidgetContext context, List<Component> tooltipLines, TooltipFlag tooltipFlag) {
        MobEffectWidgetEvents.TOOLTIP.invoker().onGatherEffectTooltipLines(context, tooltipLines, tooltipFlag);
    }
}
