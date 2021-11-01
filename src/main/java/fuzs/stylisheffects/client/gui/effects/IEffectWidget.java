package fuzs.stylisheffects.client.gui.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.EffectInstance;

public interface IEffectWidget {

    int getWidth();

    int getHeight();

    void renderWidget(MatrixStack matrixStack, int posX, int posY, Minecraft minecraft, EffectInstance effectinstance);
}
