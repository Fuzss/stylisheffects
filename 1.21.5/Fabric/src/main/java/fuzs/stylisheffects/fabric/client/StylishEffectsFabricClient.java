package fuzs.stylisheffects.fabric.client;

import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.StylishEffectsClient;
import net.fabricmc.api.ClientModInitializer;

public class StylishEffectsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(StylishEffects.MOD_ID, StylishEffectsClient::new);
    }
}
