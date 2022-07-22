package fuzs.stylisheffects.client.handler;

import fuzs.stylisheffects.client.gui.effects.AbstractEffectRenderer;
import fuzs.stylisheffects.client.gui.effects.CompactEffectRenderer;
import fuzs.stylisheffects.client.gui.effects.VanillaEffectRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum EffectRenderer {
    NONE(null),
//    GUI_SMALL(null),
    GUI_COMPACT(CompactEffectRenderer::new),
//    INVENTORY_COMPACT(null),
    INVENTORY_FULL_SIZE(VanillaEffectRenderer::new);

    @Nullable
    private final EffectRendererEnvironment.Factory factory;

    EffectRenderer(EffectRendererEnvironment.Factory factory) {
        this.factory = factory;
    }

    @SuppressWarnings("ConstantConditions")
    @NotNull
    public AbstractEffectRenderer create(EffectRendererEnvironment type) {
        if (this.factory != null) {
            return this.factory.apply(type);
        }
        return null;
    }

    public boolean isEnabled() {
        return this != NONE;
    }
}
