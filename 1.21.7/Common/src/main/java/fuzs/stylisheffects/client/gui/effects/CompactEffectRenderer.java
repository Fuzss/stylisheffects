package fuzs.stylisheffects.client.gui.effects;

import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.handler.EffectRendererEnvironment;
import fuzs.stylisheffects.client.util.ColorUtil;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.Nullable;

public abstract class CompactEffectRenderer extends AbstractEffectRenderer {
    private static final ResourceLocation TINY_NUMBERS_TEXTURE = StylishEffects.id("textures/font/tiny_numbers.png");

    public CompactEffectRenderer(EffectRendererEnvironment environment) {
        super(environment);
    }

    @Override
    protected int getBackgroundTextureY() {
        return 0;
    }

    protected abstract int getAmplifierOffsetX();

    protected abstract int getAmplifierOffsetY();

    @Override
    protected abstract ClientConfig.CompactWidgetConfig widgetConfig();

    @Nullable
    protected String getInfiniteDurationString() {
        return this.widgetConfig().hideInfiniteDuration ? null : super.getInfiniteDurationString();
    }

    @Override
    protected void drawEffectAmplifier(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance mobEffectInstance) {
        ClientConfig.EffectAmplifier effectAmplifier = this.widgetConfig().effectAmplifier;
        if (effectAmplifier == ClientConfig.EffectAmplifier.NONE || mobEffectInstance.getAmplifier() < 1
                || mobEffectInstance.getAmplifier() > 9) {
            return;
        }
        int potionColor = ColorUtil.getEffectColor(this.widgetConfig().amplifierColor, mobEffectInstance);
        // subtract amplifier width of 3
        final int offsetX = effectAmplifier == ClientConfig.EffectAmplifier.TOP_LEFT ? this.getAmplifierOffsetX() :
                this.getWidth() - this.getAmplifierOffsetX() - 3;
        final int offsetY = this.getAmplifierOffsetY();
        // drop shadow on all sides
        int colorValue = ARGB.color(ARGB.as8BitChannel((float) this.rendererConfig().widgetAlpha), 0);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                TINY_NUMBERS_TEXTURE,
                posX + offsetX - 1,
                posY + offsetY,
                5 * (mobEffectInstance.getAmplifier() + 1),
                0,
                3,
                5,
                256,
                256,
                colorValue);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                TINY_NUMBERS_TEXTURE,
                posX + offsetX + 1,
                posY + offsetY,
                5 * (mobEffectInstance.getAmplifier() + 1),
                0,
                3,
                5,
                256,
                256,
                colorValue);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                TINY_NUMBERS_TEXTURE,
                posX + offsetX,
                posY + offsetY - 1,
                5 * (mobEffectInstance.getAmplifier() + 1),
                0,
                3,
                5,
                256,
                256,
                colorValue);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                TINY_NUMBERS_TEXTURE,
                posX + offsetX,
                posY + offsetY + 1,
                5 * (mobEffectInstance.getAmplifier() + 1),
                0,
                3,
                5,
                256,
                256,
                colorValue);
        // actual number
        colorValue = ARGB.color(ARGB.as8BitChannel((float) this.rendererConfig().widgetAlpha), potionColor);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                TINY_NUMBERS_TEXTURE,
                posX + offsetX,
                posY + offsetY,
                5 * (mobEffectInstance.getAmplifier() + 1),
                0,
                3,
                5,
                256,
                256,
                colorValue);
    }
}
