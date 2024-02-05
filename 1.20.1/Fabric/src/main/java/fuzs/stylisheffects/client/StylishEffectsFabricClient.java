package fuzs.stylisheffects.client;

import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.stylisheffects.StylishEffects;
import net.fabricmc.api.ClientModInitializer;

public class StylishEffectsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(StylishEffects.MOD_ID, StylishEffectsClient::new);
    }
}
