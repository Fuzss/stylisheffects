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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.RenderProperties;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Function;

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
    protected int getTopOffset() {
        return 0;
    }

    @Override
    public Function<EffectRendererType, AbstractEffectRenderer> getFallbackRenderer() {
        return CompactEffectRenderer::new;
    }

    @Override
    public List<Pair<MobEffectInstance, int[]>> getEffectPositions(List<MobEffectInstance> activeEffects) {
        int counter = 0;
        List<Pair<MobEffectInstance, int[]>> effectToPos = Lists.newArrayList();
        for (MobEffectInstance effect : activeEffects) {
            int posX = counter % this.getMaxClampedColumns();
            int posY = counter / this.getMaxClampedColumns();
            counter++;
            if (this.config().overflowMode != ClientConfig.OverflowMode.SKIP || posY < this.getMaxClampedRows()) {
                effectToPos.add(Pair.of(effect, this.coordsToEffectPosition(posX, posY)));
            }
        }
        return effectToPos;
    }

    @Override
    public void renderWidget(PoseStack matrixStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectinstance) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, EFFECT_BACKGROUND);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, (float) this.config().widgetAlpha);
        GuiComponent.blit(matrixStack, posX, posY, 0, StylishEffects.CONFIG.client().vanillaWidget().ambientBorder && effectinstance.isAmbient() ? this.getHeight() : 0, this.getWidth(), this.getHeight(), 256, 256);
        this.drawEffectSprite(matrixStack, posX, posY, minecraft, effectinstance);
        this.drawCustomEffect(matrixStack, posX, posY, effectinstance);
        this.drawEffectText(matrixStack, posX, posY, minecraft, effectinstance);
    }

    private void drawEffectSprite(PoseStack matrixStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectinstance) {
        MobEffectTextureManager potionspriteuploader = minecraft.getMobEffectTextures();
        TextureAtlasSprite textureatlassprite = potionspriteuploader.get(effectinstance.getEffect());
        RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
        final float blinkingAlpha = StylishEffects.CONFIG.client().vanillaWidget().blinkingAlpha ? this.getBlinkingAlpha(effectinstance) : 1.0F;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, blinkingAlpha * (float) this.config().widgetAlpha);
        GuiComponent.blit(matrixStack, posX + 6, posY + 7, 0, 18, 18, textureatlassprite);
    }

    private void drawEffectText(PoseStack matrixStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectinstance) {
        if (RenderProperties.getEffectRenderer(effectinstance).shouldRenderInvText(effectinstance)) {
            MutableComponent component = new TranslatableComponent(effectinstance.getEffect().getDescriptionId());
            if (effectinstance.getAmplifier() >= 1 && effectinstance.getAmplifier() <= 9) {
                component.append(" ").append(new TranslatableComponent("enchantment.level." + (effectinstance.getAmplifier() + 1)));
            }
            int nameColor = ColorUtil.getEffectColor(StylishEffects.CONFIG.client().vanillaWidget().nameColor, effectinstance);
            minecraft.font.drawShadow(matrixStack, component, posX + 10 + 18, posY + 7 + (!StylishEffects.CONFIG.client().vanillaWidget().ambientDuration && effectinstance.isAmbient() ? 4 : 0), (int) (this.config().widgetAlpha * 255.0F) << 24 | nameColor);
            if (StylishEffects.CONFIG.client().vanillaWidget().ambientDuration || !effectinstance.isAmbient()) {
                this.getEffectDuration(effectinstance, StylishEffects.CONFIG.client().vanillaWidget().longDurationString).ifPresent(duration -> {
                    int durationColor = ColorUtil.getEffectColor(StylishEffects.CONFIG.client().vanillaWidget().durationColor, effectinstance);
                    minecraft.font.drawShadow(matrixStack, duration, posX + 10 + 18, posY + 7 + 10, (int) (this.config().widgetAlpha * 255.0F) << 24 | durationColor);
                });
            }
        }
    }
}
