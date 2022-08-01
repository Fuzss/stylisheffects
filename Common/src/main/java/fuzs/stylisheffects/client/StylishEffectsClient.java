package fuzs.stylisheffects.client;

import fuzs.puzzleslib.client.core.ClientModConstructor;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.api.client.StylishEffectsClientApi;
import fuzs.stylisheffects.client.handler.EffectScreenHandlerImpl;
import fuzs.stylisheffects.config.ClientConfig;

public class StylishEffectsClient implements ClientModConstructor {

    @Override
    public void onClientSetup() {
        StylishEffectsClientApi.setEffectScreenHandler(EffectScreenHandlerImpl.INSTANCE);
        // can't do this during construct as configs won't be loaded then
        StylishEffectsClientApi.getEffectScreenHandler().rebuildEffectRenderers();
        StylishEffects.CONFIG.getHolder(ClientConfig.class).accept(StylishEffectsClientApi.getEffectScreenHandler()::rebuildEffectRenderers);
    }
}
