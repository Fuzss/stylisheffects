package fuzs.stylisheffects.client.core;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.stylisheffects.api.client.event.MobEffectWidgetEvent;
import fuzs.stylisheffects.client.handler.EffectRendererEnvironment;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

public class ForgeClientAbstractions implements ClientAbstractions {

    @Override
    public boolean isMobEffectVisibleIn(EffectRendererEnvironment effectRendererEnvironment, MobEffectInstance effectInstance) {
        return switch (effectRendererEnvironment) {
            case GUI -> IClientMobEffectExtensions.of(effectInstance).isVisibleInGui(effectInstance);
            case INVENTORY -> IClientMobEffectExtensions.of(effectInstance).isVisibleInInventory(effectInstance);
        };
    }

    @Override
    public boolean renderInventoryText(MobEffectInstance effectInstance, EffectRenderingInventoryScreen<?> screen, PoseStack poseStack, int x, int y, int blitOffset) {
        return IClientMobEffectExtensions.of(effectInstance).renderInventoryText(effectInstance, screen, poseStack, x, y, blitOffset);
    }

    @Override
    public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, PoseStack poseStack, int x, int y, int blitOffset) {
        return IClientMobEffectExtensions.of(instance).renderInventoryIcon(instance, screen, poseStack, x, y, blitOffset);
    }

    @Override
    public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, PoseStack poseStack, int x, int y, float z, float alpha) {
        return IClientMobEffectExtensions.of(instance).renderGuiIcon(instance, gui, poseStack, x, y, z, alpha);
    }

    @Override
    public void onGatherEffectTooltipLines(MobEffectInstance effectInstance, List<Component> tooltipLines, TooltipFlag tooltipFlag) {
        MinecraftForge.EVENT_BUS.post(new MobEffectWidgetEvent.EffectTooltip(effectInstance, tooltipLines, tooltipFlag));
    }
}
