package fuzs.stylisheffects.client;

import fuzs.puzzleslib.client.core.ClientModConstructor;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.handler.EffectScreenHandler;
import fuzs.stylisheffects.config.ClientConfig;

public class StylishEffectsClient implements ClientModConstructor {

    @Override
    public void onClientSetup() {
        // can't do this during construct as configs won't be loaded then
        EffectScreenHandler.INSTANCE.createHudRenderer();
        StylishEffects.CONFIG.getHolder(ClientConfig.class).addCallback(EffectScreenHandler.INSTANCE::createHudRenderer);
    }
}
