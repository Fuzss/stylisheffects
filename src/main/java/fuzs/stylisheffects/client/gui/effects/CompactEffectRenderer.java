package fuzs.stylisheffects.client.gui.effects;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.stylisheffects.StylishEffects;
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
    public int getRows() {
        if (StylishEffects.CONFIG.client().compactWidget().separateEffects) {
            final int beneficialEffects = this.getBeneficialAmount(this.activeEffects);
            return this.splitByColumns(beneficialEffects) + this.splitByColumns(this.activeEffects.size() - beneficialEffects);
        }
        return super.getRows();
    }

    @Override
    protected int getTopOffset() {
        return 1;
    }

    private int getBeneficialAmount(List<MobEffectInstance> activeEffects) {
        return  (int) activeEffects.stream()
                .map(MobEffectInstance::getEffect)
                .filter(MobEffect::isBeneficial)
                .count();
    }

    @Override
    public List<Pair<MobEffectInstance, int[]>> getEffectPositions(List<MobEffectInstance> activeEffects) {
        final int beneficialRows = this.splitByColumns(this.getBeneficialAmount(activeEffects));
        int beneficialCounter = 0, harmfulCounter = 0;
        List<Pair<MobEffectInstance, int[]>> effectToPos = Lists.newArrayList();
        for (MobEffectInstance effect : activeEffects) {
            int counter;
            final boolean beneficial = !StylishEffects.CONFIG.client().compactWidget().separateEffects || effect.getEffect().isBeneficial();
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
            if (this.config().overflowMode != ClientConfig.OverflowMode.SKIP || posY < this.getMaxClampedRows()) {
                effectToPos.add(Pair.of(effect, this.coordsToEffectPosition(posX, posY)));
            }
        }
        // sorting is need for rendering in condensed mode (when too many effects are active and the widget overlap) so that widget overlap in the right order
        if (StylishEffects.CONFIG.client().compactWidget().separateEffects) {
            effectToPos.sort(Comparator.<Pair<MobEffectInstance, int[]>, Boolean>comparing(o -> o.getLeft().getEffect().isBeneficial()).reversed());
        }
        if (beneficialCounter + harmfulCounter != activeEffects.size()) throw new RuntimeException("effects amount mismatch");
        return effectToPos;
    }

    @Override
    public void renderWidget(PoseStack matrixStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectinstance) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, EFFECT_BACKGROUND);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, (float) this.config().widgetAlpha);
        GuiComponent.blit(matrixStack, posX, posY, StylishEffects.CONFIG.client().compactWidget().ambientBorder && effectinstance.isAmbient() ? this.getWidth() : 0, 64, this.getWidth(), this.getHeight(), 256, 256);
        this.drawEffectAmplifier(matrixStack, posX, posY, minecraft, effectinstance);
        this.drawEffectSprite(matrixStack, posX, posY, minecraft, effectinstance);
        this.drawCustomEffect(matrixStack, posX, posY, effectinstance);
        this.drawEffectText(matrixStack, posX, posY, minecraft, effectinstance);
    }

    private void drawEffectAmplifier(PoseStack matrixStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectinstance) {
        final ClientConfig.EffectAmplifier amplifier = StylishEffects.CONFIG.client().compactWidget().effectAmplifier;
        if (amplifier != ClientConfig.EffectAmplifier.NONE && effectinstance.getAmplifier() >= 1 && effectinstance.getAmplifier() <= 9) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TINY_NUMBERS_TEXTURE);
            int potionColor = ColorUtil.getEffectColor(StylishEffects.CONFIG.client().compactWidget().amplifierColor, effectinstance);
            float red = (potionColor >> 16 & 255) / 255.0F;
            float green = (potionColor >> 8 & 255) / 255.0F;
            float blue = (potionColor >> 0 & 255) / 255.0F;
            final int offsetX = amplifier == ClientConfig.EffectAmplifier.TOP_LEFT ? 3 : 23;
            final int offsetY = 2;
            // drop shadow
            RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, (float) this.config().widgetAlpha);
            GuiComponent.blit(matrixStack, posX + offsetX - 1, posY + offsetY, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
            GuiComponent.blit(matrixStack, posX + offsetX + 1, posY + offsetY, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
            GuiComponent.blit(matrixStack, posX + offsetX, posY + offsetY - 1, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
            GuiComponent.blit(matrixStack, posX + offsetX, posY + offsetY + 1, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
            // actual number
            RenderSystem.setShaderColor(red, green, blue, (float) this.config().widgetAlpha);
            GuiComponent.blit(matrixStack, posX + offsetX, posY + offsetY, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
        }
    }

    private void drawEffectSprite(PoseStack matrixStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectinstance) {
        MobEffectTextureManager potionspriteuploader = minecraft.getMobEffectTextures();
        TextureAtlasSprite textureatlassprite = potionspriteuploader.get(effectinstance.getEffect());
        RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
        final float blinkingAlpha = StylishEffects.CONFIG.client().compactWidget().blinkingAlpha ? this.getBlinkingAlpha(effectinstance) : 1.0F;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, blinkingAlpha * (float) this.config().widgetAlpha);
        // draw icon a bit further down when no time is displayed to trim empty space
        GuiComponent.blit(matrixStack, posX + 5, posY + (!StylishEffects.CONFIG.client().compactWidget().ambientDuration && effectinstance.isAmbient() ? 3 : 2), 0, 18, 18, textureatlassprite);
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private void drawEffectText(PoseStack matrixStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectinstance) {
        if (StylishEffects.CONFIG.client().compactWidget().ambientDuration || !effectinstance.isAmbient()) {
            this.getEffectDuration(effectinstance, StylishEffects.CONFIG.client().compactWidget().longDurationString).ifPresent(durationComponent -> {
                int potionColor = ColorUtil.getEffectColor(StylishEffects.CONFIG.client().compactWidget().durationColor, effectinstance);
                final int alpha = (int) (this.config().widgetAlpha * 255.0F) << 24;
                FormattedCharSequence ireorderingprocessor = durationComponent.getVisualOrderText();
                // render shadow on every side due avoid clashing with colorful background
                minecraft.font.draw(matrixStack, ireorderingprocessor, posX + 14 - minecraft.font.width(ireorderingprocessor) / 2, posY + 14, alpha);
                minecraft.font.draw(matrixStack, ireorderingprocessor, posX + 16 - minecraft.font.width(ireorderingprocessor) / 2, posY + 14, alpha);
                minecraft.font.draw(matrixStack, ireorderingprocessor, posX + 15 - minecraft.font.width(ireorderingprocessor) / 2, posY + 13, alpha);
                minecraft.font.draw(matrixStack, ireorderingprocessor, posX + 15 - minecraft.font.width(ireorderingprocessor) / 2, posY + 15, alpha);
                minecraft.font.draw(matrixStack, ireorderingprocessor, posX + 15 - minecraft.font.width(ireorderingprocessor) / 2, posY + 14, alpha | potionColor);
            });
        }
    }
}
