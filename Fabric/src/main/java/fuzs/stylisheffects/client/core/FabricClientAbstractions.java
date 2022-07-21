package fuzs.stylisheffects.client.core;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.stylisheffects.client.gui.effects.AbstractEffectRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;

public class FabricClientAbstractions implements ClientAbstractions {

    @Override
    public boolean isMobEffectVisibleIn(AbstractEffectRenderer.EffectRendererType effectRendererType, MobEffectInstance effectInstance) {
        return true;
    }

    @Override
    public boolean renderInventoryText(MobEffectInstance effectInstance, EffectRenderingInventoryScreen<?> screen, PoseStack poseStack, int x, int y, int blitOffset) {
        return false;
    }

    @Override
    public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, PoseStack poseStack, int x, int y, int blitOffset) {
        return false;
    }

    @Override
    public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, PoseStack poseStack, int x, int y, float z, float alpha) {
        return false;
    }
}
