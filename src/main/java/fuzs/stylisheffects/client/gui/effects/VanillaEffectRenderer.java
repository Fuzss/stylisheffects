package fuzs.stylisheffects.client.gui.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.potion.EffectInstance;

import java.util.Collection;

public class VanillaEffectRenderer extends AbstractEffectRenderer {
    private int totalEffects;
    private int effectCounter;

    public VanillaEffectRenderer(ClientConfig.EffectConfig config) {
        super(config);
    }

    @Override
    public int getPlaqueWidth() {
        return 120;
    }

    @Override
    public int getPlaqueHeight() {
        return 32;
    }

    @Override
    public int getMaxHorizontalEffects() {
        return 1;
    }

    @Override
    public void setActiveEffects(Collection<EffectInstance> activeEffects) {
        super.setActiveEffects(activeEffects);
        this.totalEffects = activeEffects.size();
        this.effectCounter = 0;
    }

    @Override
    public int[] getEffectPosition(boolean isBeneficial) {
        return new int[]{0, this.effectCounter++};
    }

    @Override
    public void renderPlaque(MatrixStack matrixStack, int posX, int posY, EffectInstance effectinstance) {
        Minecraft.getInstance().getTextureManager().bind(EFFECT_BACKGROUND);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.config().widgetAlpha);
        // background
        AbstractGui.blit(matrixStack, posX, posY, 0, effectinstance.isAmbient() ? 32 : 0, 120, 32, 256, 256);
    }
}
