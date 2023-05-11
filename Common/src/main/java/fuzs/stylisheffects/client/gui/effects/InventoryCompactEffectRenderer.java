package fuzs.stylisheffects.client.gui.effects;

import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.api.client.MobEffectWidgetContext;
import fuzs.stylisheffects.client.handler.EffectRendererEnvironment;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Optional;

public class InventoryCompactEffectRenderer extends CompactEffectRenderer {

    public InventoryCompactEffectRenderer(EffectRendererEnvironment environment) {
        super(environment);
    }

    @Override
    public int getWidth() {
        return 32;
    }

    @Override
    public int getHeight() {
        return 32;
    }

    @Override
    protected int getBackgroundTextureX() {
        return 120;
    }

    @Override
    protected int getSpriteOffsetX() {
        return 7;
    }

    @Override
    protected int getSpriteOffsetY(boolean withoutDuration) {
        return withoutDuration ? 7 : 6;
    }

    @Override
    protected int getAmplifierOffsetX() {
        return 3;
    }

    @Override
    protected int getAmplifierOffsetY() {
        return 3;
    }

    @Override
    protected int getDurationOffsetY() {
        return this.getHeight() - 13;
    }

    @Override
    public MobEffectWidgetContext.Renderer getEffectRenderer() {
        return MobEffectWidgetContext.Renderer.INVENTORY_COMPACT;
    }

    @Override
    public EffectRendererEnvironment.Factory getFallbackRenderer() {
        return GuiCompactEffectRenderer::new;
    }

    @Override
    protected ClientConfig.InventoryCompactWidgetConfig widgetConfig() {
        return StylishEffects.CONFIG.get(ClientConfig.class).inventoryCompactWidget();
    }

    @Override
    protected Optional<Component> getEffectDuration(MobEffectInstance effectInstance) {
        if (this.widgetConfig().compactDuration) {
            return Optional.of(Component.literal(formatCompactTickDuration(effectInstance.getDuration())));
        }
        return super.getEffectDuration(effectInstance);
    }
}
