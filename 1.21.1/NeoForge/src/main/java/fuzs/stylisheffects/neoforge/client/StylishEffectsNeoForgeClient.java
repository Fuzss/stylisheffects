package fuzs.stylisheffects.neoforge.client;

import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.StylishEffectsClient;
import fuzs.stylisheffects.data.client.ModLanguageProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = StylishEffects.MOD_ID, dist = Dist.CLIENT)
public class StylishEffectsNeoForgeClient {

    public StylishEffectsNeoForgeClient() {
        ClientModConstructor.construct(StylishEffects.MOD_ID, StylishEffectsClient::new);
        DataProviderHelper.registerDataProviders(StylishEffects.MOD_ID, ModLanguageProvider::new);
    }
}
