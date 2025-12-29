package fuzs.stylisheffects.client.handler;

import fuzs.stylisheffects.client.gui.effects.AbstractMobEffectRenderer;

import java.util.function.Function;

public enum EffectRendererEnvironment {
    INVENTORY, GUI;

    @FunctionalInterface
    public interface Factory extends Function<EffectRendererEnvironment, AbstractMobEffectRenderer> {

    }
}
