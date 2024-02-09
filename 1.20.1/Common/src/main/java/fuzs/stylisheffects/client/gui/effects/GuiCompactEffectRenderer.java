package fuzs.stylisheffects.client.gui.effects;

import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.api.client.stylisheffects.v1.MobEffectWidgetContext;
import fuzs.stylisheffects.client.handler.EffectRendererEnvironment;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.Nullable;

public class GuiCompactEffectRenderer extends GuiEffectRenderer {

    public GuiCompactEffectRenderer(EffectRendererEnvironment environment) {
        super(environment);
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
    protected int getBackgroundTextureX() {
        return 152;
    }

    @Override
    public MobEffectWidgetContext.Renderer getEffectRenderer() {
        return MobEffectWidgetContext.Renderer.GUI_COMPACT;
    }

    @Override
    protected int getSpriteOffsetX() {
        return 5;
    }

    @Override
    public @Nullable EffectRendererEnvironment.Factory getFallbackRenderer() {
        return GuiSmallEffectRenderer::new;
    }

    @Override
    protected ClientConfig.GuiCompactWidgetConfig widgetConfig() {
        return StylishEffects.CONFIG.get(ClientConfig.class).guiCompactWidget();
    }

    @Override
    protected String formatEffectDuration(MobEffectInstance mobEffectInstance) {
        return this.widgetConfig().compactDuration ? formatCompactTickDuration(mobEffectInstance.getDuration()) : super.formatEffectDuration(mobEffectInstance);
    }
}
