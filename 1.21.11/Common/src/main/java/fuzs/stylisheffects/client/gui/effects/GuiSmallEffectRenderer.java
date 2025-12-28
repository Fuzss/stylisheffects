package fuzs.stylisheffects.client.gui.effects;

import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.handler.EffectRendererEnvironment;
import fuzs.stylisheffects.config.ClientConfig;
import fuzs.stylisheffects.config.WidgetType;
import net.minecraft.world.effect.MobEffectInstance;
import org.jspecify.annotations.Nullable;

public class GuiSmallEffectRenderer extends GuiEffectRenderer {

    public GuiSmallEffectRenderer(EffectRendererEnvironment environment) {
        super(environment);
    }

    @Override
    public int getWidth() {
        return 24;
    }

    @Override
    public int getHeight() {
        return 24;
    }

    @Override
    protected int getBackgroundTextureX() {
        return 181;
    }

    @Override
    public WidgetType getType() {
        return WidgetType.GUI_COMPACT;
    }

    @Override
    protected int getSpriteOffsetX() {
        return 3;
    }

    @Override
    public EffectRendererEnvironment.@Nullable Factory getFallbackRenderer() {
        return null;
    }

    @Override
    protected ClientConfig.GuiWidgetConfig widgetConfig() {
        return StylishEffects.CONFIG.get(ClientConfig.class).guiSmallWidget();
    }

    @Override
    protected String formatEffectDuration(MobEffectInstance mobEffectInstance) {
        return formatCompactTickDuration(mobEffectInstance.getDuration());
    }
}
