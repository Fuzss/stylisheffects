package fuzs.stylisheffects.client.gui.effects;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.handler.EffectRendererEnvironment;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;
import java.util.List;

public class CompactEffectRenderer extends AbstractEffectRenderer {
    private static final ResourceLocation TINY_NUMBERS_TEXTURE = new ResourceLocation(StylishEffects.MOD_ID,"textures/font/tiny_numbers.png");

    public CompactEffectRenderer(EffectRendererEnvironment type) {
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
    public int getRows() {
        if (StylishEffects.CONFIG.get(ClientConfig.class).compactWidget().separateEffects) {
            final int beneficialEffects = this.countBeneficialEffects(this.activeEffects);
            return this.splitByColumns(beneficialEffects) + this.splitByColumns(this.activeEffects.size() - beneficialEffects);
        }
        return super.getRows();
    }

    @Override
    protected int getTopOffset() {
        return 1;
    }

    private int countBeneficialEffects(List<MobEffectInstance> activeEffects) {
        return (int) activeEffects.stream()
                .map(MobEffectInstance::getEffect)
                .filter(MobEffect::isBeneficial)
                .count();
    }

    @Override
    public List<Pair<MobEffectInstance, int[]>> getEffectPositions(List<MobEffectInstance> activeEffects) {
        final int beneficialRows = this.splitByColumns(this.countBeneficialEffects(activeEffects));
        int beneficialCounter = 0, harmfulCounter = 0;
        List<Pair<MobEffectInstance, int[]>> effectToPos = Lists.newArrayList();
        for (MobEffectInstance effect : activeEffects) {
            int counter;
            final boolean beneficial = !StylishEffects.CONFIG.get(ClientConfig.class).compactWidget().separateEffects || effect.getEffect().isBeneficial();
            if (beneficial) {
                counter = beneficialCounter++;
            } else {
                counter = harmfulCounter++;
            }
            int posX = counter % this.getMaxClampedColumns();
            int posY = counter / this.getMaxClampedColumns();
            if (!beneficial) {
                posY += beneficialRows;
            }
            if (posY < this.getMaxClampedRows()) {
                effectToPos.add(Pair.of(effect, this.coordsToEffectPosition(posX, posY)));
            }
        }
        // sorting is need for rendering in condensed mode (when too many effects are active and the widget overlap) so that widget overlap in the right order
        if (StylishEffects.CONFIG.get(ClientConfig.class).compactWidget().separateEffects) {
            effectToPos.sort(Comparator.<Pair<MobEffectInstance, int[]>, Boolean>comparing(o -> o.getLeft().getEffect().isBeneficial()).reversed());
        }
        if (beneficialCounter + harmfulCounter != activeEffects.size()) throw new RuntimeException("effects amount mismatch");
        return effectToPos;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectInstance) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, EFFECT_BACKGROUND);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, (float) this.config().widgetAlpha);
        GuiComponent.blit(poseStack, posX, posY, 152, StylishEffects.CONFIG.get(ClientConfig.class).compactWidget().ambientBorder && effectInstance.isAmbient() ? this.getHeight() : 0, this.getWidth(), this.getHeight(), 256, 256);
        this.drawEffectAmplifier(poseStack, posX, posY, minecraft, effectInstance);
        this.drawEffectSprite(poseStack, posX, posY, minecraft, effectInstance);
        this.drawEffectText(poseStack, posX, posY, minecraft, effectInstance);
    }

    private void drawEffectAmplifier(PoseStack poseStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectinstance) {
        final ClientConfig.EffectAmplifier amplifier = StylishEffects.CONFIG.get(ClientConfig.class).compactWidget().effectAmplifier;
        if (amplifier != ClientConfig.EffectAmplifier.NONE && effectinstance.getAmplifier() >= 1 && effectinstance.getAmplifier() <= 9) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TINY_NUMBERS_TEXTURE);
            int potionColor = ColorUtil.getEffectColor(StylishEffects.CONFIG.get(ClientConfig.class).compactWidget().amplifierColor, effectinstance);
            float red = (potionColor >> 16 & 255) / 255.0F;
            float green = (potionColor >> 8 & 255) / 255.0F;
            float blue = (potionColor >> 0 & 255) / 255.0F;
            final int offsetX = amplifier == ClientConfig.EffectAmplifier.TOP_LEFT ? 3 : 23;
            final int offsetY = 2;
            // drop shadow
            RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, (float) this.config().widgetAlpha);
            GuiComponent.blit(poseStack, posX + offsetX - 1, posY + offsetY, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
            GuiComponent.blit(poseStack, posX + offsetX + 1, posY + offsetY, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
            GuiComponent.blit(poseStack, posX + offsetX, posY + offsetY - 1, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
            GuiComponent.blit(poseStack, posX + offsetX, posY + offsetY + 1, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
            // actual number
            RenderSystem.setShaderColor(red, green, blue, (float) this.config().widgetAlpha);
            GuiComponent.blit(poseStack, posX + offsetX, posY + offsetY, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
        }
    }

    private void drawEffectSprite(PoseStack poseStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectinstance) {
        if (this.drawCustomEffect(poseStack, posX, posY, effectinstance)) return;
        MobEffectTextureManager potionspriteuploader = minecraft.getMobEffectTextures();
        TextureAtlasSprite textureatlassprite = potionspriteuploader.get(effectinstance.getEffect());
        RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
        final float blinkingAlpha = StylishEffects.CONFIG.get(ClientConfig.class).compactWidget().blinkingAlpha ? this.getBlinkingAlpha(effectinstance) : 1.0F;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, blinkingAlpha * (float) this.config().widgetAlpha);
        // draw icon a bit further down when no time is displayed to trim empty space
        GuiComponent.blit(poseStack, posX + 5, posY + (!StylishEffects.CONFIG.get(ClientConfig.class).compactWidget().ambientDuration && effectinstance.isAmbient() ? 3 : 2), 0, 18, 18, textureatlassprite);
    }

    private void drawEffectText(PoseStack poseStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectinstance) {
        if (StylishEffects.CONFIG.get(ClientConfig.class).compactWidget().ambientDuration || !effectinstance.isAmbient()) {
            this.getEffectDuration(effectinstance, StylishEffects.CONFIG.get(ClientConfig.class).compactWidget().longDurationString).ifPresent(durationComponent -> {
                int potionColor = ColorUtil.getEffectColor(StylishEffects.CONFIG.get(ClientConfig.class).compactWidget().durationColor, effectinstance);
                final int alpha = (int) (this.config().widgetAlpha * 255.0F) << 24;
                FormattedCharSequence ireorderingprocessor = durationComponent.getVisualOrderText();
                // render shadow on every side to avoid clashing with colorful background
                minecraft.font.draw(poseStack, ireorderingprocessor, posX + 14 - minecraft.font.width(ireorderingprocessor) / 2, posY + 14, alpha);
                minecraft.font.draw(poseStack, ireorderingprocessor, posX + 16 - minecraft.font.width(ireorderingprocessor) / 2, posY + 14, alpha);
                minecraft.font.draw(poseStack, ireorderingprocessor, posX + 15 - minecraft.font.width(ireorderingprocessor) / 2, posY + 13, alpha);
                minecraft.font.draw(poseStack, ireorderingprocessor, posX + 15 - minecraft.font.width(ireorderingprocessor) / 2, posY + 15, alpha);
                minecraft.font.draw(poseStack, ireorderingprocessor, posX + 15 - minecraft.font.width(ireorderingprocessor) / 2, posY + 14, alpha | potionColor);
            });
        }
    }
}
