package fuzs.stylisheffects.client.gui.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.potion.EffectInstance;

public interface IEffectPlaque {

    int getPlaqueWidth();

    int getPlaqueHeight();

    void renderPlaque(MatrixStack matrixStack, int posX, int posY, EffectInstance effectinstance);
}
