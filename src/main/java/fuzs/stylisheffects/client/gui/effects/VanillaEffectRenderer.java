package fuzs.stylisheffects.client.gui.effects;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.stylisheffects.StylishEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VanillaEffectRenderer extends AbstractEffectRenderer {
    public VanillaEffectRenderer(EffectRendererType type) {
        super(type);
    }

    @Override
    public int getWidth() {
        return 120;
    }

    @Override
    public int getHeight() {
        return 32;
    }

    @Override
    public int getMaxHorizontalEffects() {
        return 1;
    }

    @Override
    public Map<EffectInstance, int[]> getEffectPositions(List<EffectInstance> activeEffects) {
        Map<EffectInstance, int[]> effectToPos = Maps.newHashMap();
        for (int i = 0, size = Math.min(activeEffects.size(), this.getMaxVerticalEffects()); i < size; i++) {
            effectToPos.put(activeEffects.get(i), this.coordsToEffectPosition(0, i));
        }
        return effectToPos;
    }

    @Override
    public void renderWidget(MatrixStack matrixStack, int posX, int posY, Minecraft minecraft, EffectInstance effectinstance) {
        RenderSystem.enableBlend();
        minecraft.getTextureManager().bind(EFFECT_BACKGROUND);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.config().widgetAlpha);
        AbstractGui.blit(matrixStack, posX, posY, 0, effectinstance.isAmbient() ? this.getHeight() : 0, this.getWidth(), this.getHeight(), 256, 256);
        this.drawEffectSprite(matrixStack, posX, posY, minecraft, effectinstance);
        this.drawCustomEffect(matrixStack, posX, posY, effectinstance);
        this.drawEffectText(matrixStack, posX, posY, minecraft, effectinstance);
    }

    private void drawEffectSprite(MatrixStack matrixStack, int posX, int posY, Minecraft minecraft, EffectInstance effectinstance) {
        PotionSpriteUploader potionspriteuploader = minecraft.getMobEffectTextures();
        TextureAtlasSprite textureatlassprite = potionspriteuploader.get(effectinstance.getEffect());
        minecraft.getTextureManager().bind(textureatlassprite.atlas().location());
        final float blinkingAlpha = StylishEffects.CONFIG.client().vanillaWidget().blinkingAlpha ? this.getBlinkingAlpha(effectinstance) : 1.0F;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, blinkingAlpha * this.config().widgetAlpha);
        AbstractGui.blit(matrixStack, posX + 6, posY + 7, 0, 18, 18, textureatlassprite);
    }

    private void drawEffectText(MatrixStack matrixStack, int posX, int posY, Minecraft minecraft, EffectInstance effectinstance) {
        if (effectinstance.shouldRenderInvText()) {
            IFormattableTextComponent component = new TranslationTextComponent(effectinstance.getEffect().getDescriptionId());
            if (effectinstance.getAmplifier() >= 1 && effectinstance.getAmplifier() <= 9) {
                component.append(" ").append(new TranslationTextComponent("enchantment.level." + (effectinstance.getAmplifier() + 1)));
            }
            int nameColor = ColorUtil.getEffectColor(StylishEffects.CONFIG.client().vanillaWidget().nameColor, effectinstance);
            minecraft.font.drawShadow(matrixStack, component, posX + 10 + 18, posY + 7, (int) (this.config().widgetAlpha * 255.0F) << 24 | nameColor);
            this.getEffectDuration(effectinstance, StylishEffects.CONFIG.client().vanillaWidget().longDurationString).ifPresent(duration -> {
                int durationColor = ColorUtil.getEffectColor(StylishEffects.CONFIG.client().vanillaWidget().durationColor, effectinstance);
                minecraft.font.drawShadow(matrixStack, duration, posX + 10 + 18, posY + 7 + 10, (int) (this.config().widgetAlpha * 255.0F) << 24 | durationColor);
            });
        }
    }
}
