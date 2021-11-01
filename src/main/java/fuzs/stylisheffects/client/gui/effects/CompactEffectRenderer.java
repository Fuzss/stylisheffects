package fuzs.stylisheffects.client.gui.effects;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Map;

public class CompactEffectRenderer extends AbstractEffectRenderer {
    private static final ResourceLocation TINY_NUMBERS_TEXTURE = new ResourceLocation(StylishEffects.MODID,"textures/font/tiny_numbers.png");

    public CompactEffectRenderer(EffectRendererType type) {
        super(type);
    }

    @Override
    public int getWidth() {
        return 29;
    }

    @Override
    public int getHeight() {
        return 24;
    }

    @Override
    public int getMaxHorizontalEffects() {
        return MathHelper.clamp(this.availableWidth / (this.getWidth() + this.inBetweenSpace), 1, this.config().maxWidth);
    }

    @Override
    public Map<EffectInstance, int[]> getEffectPositions(List<EffectInstance> activeEffects) {
        final int beneficialEffects = (int) activeEffects.stream()
                .map(EffectInstance::getEffect)
                .filter(Effect::isBeneficial)
                .count();
        int beneficialRows = (int) Math.ceil(beneficialEffects / (float) this.getMaxHorizontalEffects());
        int beneficialCounter = 0, harmfulCounter = 0;
        int maxHeight = this.getMaxVerticalEffects();
        Map<EffectInstance, int[]> effectToPos = Maps.newHashMap();
        for (EffectInstance effect : activeEffects) {
            int counter;
            final boolean beneficial = !StylishEffects.CONFIG.client().compactWidget().separateEffects || effect.getEffect().isBeneficial();
            if (beneficial) {
                counter = beneficialCounter++;
            } else {
                counter = harmfulCounter++;
            }
            int posX = counter % this.getMaxHorizontalEffects();
            int posY = counter / this.getMaxHorizontalEffects();
            if (!beneficial) {
                posY += beneficialRows;
            }
            if (posY < maxHeight) {
                effectToPos.put(effect, this.coordsToEffectPosition(posX, posY));
            }
        }
        if (beneficialCounter + harmfulCounter != activeEffects.size()) throw new RuntimeException("effects amount mismatch");
        return effectToPos;
    }

    @Override
    public void renderWidget(MatrixStack matrixStack, int posX, int posY, Minecraft minecraft, EffectInstance effectinstance) {
        RenderSystem.enableBlend();
        minecraft.getTextureManager().bind(EFFECT_BACKGROUND);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.config().widgetAlpha);
        AbstractGui.blit(matrixStack, posX, posY, effectinstance.isAmbient() ? this.getWidth() : 0, 64, this.getWidth(), this.getHeight(), 256, 256);
        this.drawEffectAmplifier(matrixStack, posX, posY, minecraft, effectinstance);
        this.drawEffectSprite(matrixStack, posX, posY, minecraft, effectinstance);
        this.drawCustomEffect(matrixStack, posX, posY, effectinstance);
        this.drawEffectText(matrixStack, posX, posY, minecraft, effectinstance);
    }

    private void drawEffectAmplifier(MatrixStack matrixStack, int posX, int posY, Minecraft minecraft, EffectInstance effectinstance) {
        final ClientConfig.EffectAmplifier amplifier = StylishEffects.CONFIG.client().compactWidget().effectAmplifier;
        if (amplifier != ClientConfig.EffectAmplifier.NONE && effectinstance.getAmplifier() >= 1 && effectinstance.getAmplifier() <= 9) {
            minecraft.getTextureManager().bind(TINY_NUMBERS_TEXTURE);
            int potionColor = ColorUtil.getEffectColor(StylishEffects.CONFIG.client().compactWidget().amplifierColor, effectinstance);
            float red = (potionColor >> 16 & 255) / 255.0F;
            float green = (potionColor >> 8 & 255) / 255.0F;
            float blue = (potionColor >> 0 & 255) / 255.0F;
            // drop shadow
            RenderSystem.color4f(red * 0.25F, green * 0.25F, blue * 0.25F, this.config().widgetAlpha);
            AbstractGui.blit(matrixStack, posX + (amplifier == ClientConfig.EffectAmplifier.TOP_LEFT ? 4 : 24), posY + 3, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
            // actual number
            RenderSystem.color4f(red, green, blue, this.config().widgetAlpha);
            AbstractGui.blit(matrixStack, posX + (amplifier == ClientConfig.EffectAmplifier.TOP_LEFT ? 3 : 23), posY + 2, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
        }
    }

    private void drawEffectSprite(MatrixStack matrixStack, int posX, int posY, Minecraft minecraft, EffectInstance effectinstance) {
        PotionSpriteUploader potionspriteuploader = minecraft.getMobEffectTextures();
        TextureAtlasSprite textureatlassprite = potionspriteuploader.get(effectinstance.getEffect());
        minecraft.getTextureManager().bind(textureatlassprite.atlas().location());
        final float blinkingAlpha = StylishEffects.CONFIG.client().compactWidget().blinkingAlpha ? this.getBlinkingAlpha(effectinstance) : 1.0F;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, blinkingAlpha * this.config().widgetAlpha);
        AbstractGui.blit(matrixStack, posX + 5, posY + (effectinstance.isAmbient() ? 3 : 2), 0, 18, 18, textureatlassprite);
    }

    private void drawEffectText(MatrixStack matrixStack, int posX, int posY, Minecraft minecraft, EffectInstance effectinstance) {
        if (!effectinstance.isAmbient()) {
            this.getEffectDuration(effectinstance, StylishEffects.CONFIG.client().compactWidget().longDurationString).ifPresent(durationComponent -> {
                int potionColor = ColorUtil.getEffectColor(StylishEffects.CONFIG.client().compactWidget().durationColor, effectinstance);
                AbstractGui.drawCenteredString(matrixStack, minecraft.font, durationComponent, posX + 15, posY + 14, (int) (this.config().widgetAlpha * 255.0F) << 24 | potionColor);
            });
        }
    }
}
