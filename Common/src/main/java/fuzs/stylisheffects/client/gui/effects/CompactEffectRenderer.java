package fuzs.stylisheffects.client.gui.effects;

import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.handler.EffectRendererEnvironment;
import fuzs.stylisheffects.client.util.ColorUtil;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

public abstract class CompactEffectRenderer extends AbstractEffectRenderer {
    private static final ResourceLocation TINY_NUMBERS_TEXTURE = new ResourceLocation(StylishEffects.MOD_ID,"textures/font/tiny_numbers.png");

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

    @Override
    protected void drawEffectAmplifier(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance effectinstance) {
        ClientConfig.EffectAmplifier amplifier = this.widgetConfig().effectAmplifier;
        if (amplifier == ClientConfig.EffectAmplifier.NONE || effectinstance.getAmplifier() < 1 || effectinstance.getAmplifier() > 9) return;
        int potionColor = ColorUtil.getEffectColor(this.widgetConfig().amplifierColor, effectinstance);
        float red = (potionColor >> 16 & 255) / 255.0F;
        float green = (potionColor >> 8 & 255) / 255.0F;
        float blue = (potionColor >> 0 & 255) / 255.0F;
        // subtract amplifier width of 3
        final int offsetX = amplifier == ClientConfig.EffectAmplifier.TOP_LEFT ? this.getAmplifierOffsetX() : this.getWidth() - this.getAmplifierOffsetX() - 3;
        final int offsetY = this.getAmplifierOffsetY();
        // drop shadow on all sides
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, (float) this.rendererConfig().widgetAlpha);
        guiGraphics.blit(TINY_NUMBERS_TEXTURE, posX + offsetX - 1, posY + offsetY, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
        guiGraphics.blit(TINY_NUMBERS_TEXTURE, posX + offsetX + 1, posY + offsetY, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
        guiGraphics.blit(TINY_NUMBERS_TEXTURE, posX + offsetX, posY + offsetY - 1, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
        guiGraphics.blit(TINY_NUMBERS_TEXTURE, posX + offsetX, posY + offsetY + 1, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
        // actual number
        RenderSystem.setShaderColor(red, green, blue, (float) this.rendererConfig().widgetAlpha);
        guiGraphics.blit(TINY_NUMBERS_TEXTURE, posX + offsetX, posY + offsetY, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
