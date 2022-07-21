package fuzs.stylisheffects.client.gui.effects;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.core.ClientModServices;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
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
            if (posY < this.getMaxClampedRows()) {
                effectToPos.add(Pair.of(effect, this.coordsToEffectPosition(posX, posY)));
            }
        }
        return effectToPos;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectInstance) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, EFFECT_BACKGROUND);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, (float) this.config().widgetAlpha);
        GuiComponent.blit(poseStack, posX, posY, 0, StylishEffects.CONFIG.get(ClientConfig.class).vanillaWidget().ambientBorder && effectInstance.isAmbient() ? this.getHeight() : 0, this.getWidth(), this.getHeight(), 256, 256);
        this.drawEffectSprite(poseStack, posX, posY, minecraft, effectInstance);
        this.drawEffectText(poseStack, posX, posY, minecraft, effectInstance);
    }

    private void drawEffectSprite(PoseStack poseStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectinstance) {
        if (this.drawCustomEffect(poseStack, posX, posY, effectinstance)) return;
        MobEffectTextureManager potionspriteuploader = minecraft.getMobEffectTextures();
        TextureAtlasSprite textureatlassprite = potionspriteuploader.get(effectinstance.getEffect());
        RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
        final float blinkingAlpha = StylishEffects.CONFIG.get(ClientConfig.class).vanillaWidget().blinkingAlpha ? this.getBlinkingAlpha(effectinstance) : 1.0F;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, blinkingAlpha * (float) this.config().widgetAlpha);
        GuiComponent.blit(poseStack, posX + 6, posY + 7, 0, 18, 18, textureatlassprite);
    }

    private void drawEffectText(PoseStack poseStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectinstance) {
        if (!(this.screen instanceof EffectRenderingInventoryScreen effectInventoryScreen) || !ClientModServices.ABSTRACTIONS.renderInventoryText(effectinstance, effectInventoryScreen, poseStack, posX, posY, effectInventoryScreen.getBlitOffset())) {
            MutableComponent component = Component.translatable(effectinstance.getEffect().getDescriptionId());
            if (effectinstance.getAmplifier() >= 1 && effectinstance.getAmplifier() <= 9) {
                component.append(" ").append(Component.translatable("enchantment.level." + (effectinstance.getAmplifier() + 1)));
            }
            int nameColor = ColorUtil.getEffectColor(StylishEffects.CONFIG.get(ClientConfig.class).vanillaWidget().nameColor, effectinstance);
            minecraft.font.drawShadow(poseStack, component, posX + 10 + 18, posY + 7 + (!StylishEffects.CONFIG.get(ClientConfig.class).vanillaWidget().ambientDuration && effectinstance.isAmbient() ? 4 : 0), (int) (this.config().widgetAlpha * 255.0F) << 24 | nameColor);
            if (StylishEffects.CONFIG.get(ClientConfig.class).vanillaWidget().ambientDuration || !effectinstance.isAmbient()) {
                this.getEffectDuration(effectinstance, StylishEffects.CONFIG.get(ClientConfig.class).vanillaWidget().longDurationString).ifPresent(duration -> {
                    int durationColor = ColorUtil.getEffectColor(StylishEffects.CONFIG.get(ClientConfig.class).vanillaWidget().durationColor, effectinstance);
                    minecraft.font.drawShadow(poseStack, duration, posX + 10 + 18, posY + 7 + 10, (int) (this.config().widgetAlpha * 255.0F) << 24 | durationColor);
                });
            }
        }
    }
}
