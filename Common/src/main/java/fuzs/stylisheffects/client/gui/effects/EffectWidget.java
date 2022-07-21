package fuzs.stylisheffects.client.gui.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;

public interface EffectWidget {

    int getWidth();

    int getHeight();

    void renderWidget(PoseStack poseStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectinstance);
}
