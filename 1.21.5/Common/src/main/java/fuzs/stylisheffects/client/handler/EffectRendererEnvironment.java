package fuzs.stylisheffects.client.handler;

import fuzs.stylisheffects.client.gui.effects.AbstractEffectRenderer;

import java.util.function.Function;

public enum EffectRendererEnvironment {
    INVENTORY, GUI;

    public interface Factory extends Function<EffectRendererEnvironment, AbstractEffectRenderer> {

    }
}
