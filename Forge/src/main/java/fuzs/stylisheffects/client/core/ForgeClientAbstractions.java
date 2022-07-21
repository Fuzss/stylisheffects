package fuzs.stylisheffects.client.core;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.stylisheffects.client.gui.effects.AbstractEffectRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

public class ForgeClientAbstractions implements ClientAbstractions {

    @Override
    public boolean isMobEffectVisibleIn(AbstractEffectRenderer.EffectRendererType effectRendererType, MobEffectInstance effectInstance) {
        return switch (effectRendererType) {
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
}
