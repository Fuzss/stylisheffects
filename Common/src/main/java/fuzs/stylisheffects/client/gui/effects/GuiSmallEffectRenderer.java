package fuzs.stylisheffects.client.gui.effects;

import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.api.client.MobEffectWidgetContext;
import fuzs.stylisheffects.client.handler.EffectRendererEnvironment;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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
    public MobEffectWidgetContext.Renderer getEffectRenderer() {
        return MobEffectWidgetContext.Renderer.GUI_COMPACT;
    }

    @Override
    protected int getSpriteOffsetX() {
        return 3;
    }

    @Override
    public @Nullable EffectRendererEnvironment.Factory getFallbackRenderer() {
        return null;
    }

    @Override
    protected ClientConfig.GuiSmallWidgetConfig widgetConfig() {
        return StylishEffects.CONFIG.get(ClientConfig.class).guiSmallWidget();
    }

    @Override
    protected Optional<Component> getEffectDuration(MobEffectInstance effectInstance, ClientConfig.LongDuration longDuration) {
        return Optional.of(Component.literal(formatTickDuration(effectInstance.getDuration())));
    }
}
