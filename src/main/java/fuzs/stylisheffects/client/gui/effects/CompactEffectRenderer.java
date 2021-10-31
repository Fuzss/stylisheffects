package fuzs.stylisheffects.client.gui.effects;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;

public class CompactEffectRenderer extends AbstractEffectRenderer {
    private static final ResourceLocation TINY_NUMBERS_TEXTURE = new ResourceLocation(StylishEffects.MODID,"textures/font/tiny_numbers.png");

    protected int beneficialEffects;
    protected int harmfulEffects;
    private int beneficialEffectsCounter;
    private int harmfulEffectsCounter;

    public CompactEffectRenderer(ClientConfig.EffectConfig config) {
        super(config);
    }

    @Override
    public int getPlaqueWidth() {
        return 29;
    }

    @Override
    public int getPlaqueHeight() {
        return 24;
    }

    @Override
    public int getMaxHorizontalEffects() {
        return this.screenWidth / (this.getPlaqueWidth() + this.plaqueGap);
    }

    @Override
    public void setActiveEffects(Collection<EffectInstance> activeEffects) {
        super.setActiveEffects(activeEffects);
        this.beneficialEffects = (int) activeEffects.stream()
                .map(EffectInstance::getEffect)
                .filter(Effect::isBeneficial)
                .count();
        this.harmfulEffects = activeEffects.size() - this.beneficialEffects;
        this.beneficialEffectsCounter = this.harmfulEffectsCounter = 0;
    }

    @Override
    public int[] getEffectPosition(boolean isBeneficial) {
        int[] pos = new int[2];
        pos[0] = (isBeneficial ? this.beneficialEffectsCounter : this.harmfulEffectsCounter) % this.getMaxHorizontalEffects();
        if (isBeneficial) {
            pos[1] = this.beneficialEffectsCounter / this.getMaxHorizontalEffects();
        } else {
            pos[1] = (int) Math.ceil(this.beneficialEffects / (float) this.getMaxHorizontalEffects()) + this.harmfulEffectsCounter / this.getMaxHorizontalEffects();
        }
        if (isBeneficial) this.beneficialEffectsCounter++; else this.harmfulEffectsCounter++;
        return pos;
    }

    @Override
    public void renderPlaque(MatrixStack matrixStack, int posX, int posY, EffectInstance effectinstance) {
        Minecraft.getInstance().getTextureManager().bind(EFFECT_BACKGROUND);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.config().widgetAlpha);
        // background
        AbstractGui.blit(matrixStack, posX, posY, effectinstance.isAmbient() ? 29 : 0, 64, 29, 24, 256, 256);

    }
}
