package fuzs.stylisheffects;

import fuzs.puzzleslib.core.CoreServices;
import net.fabricmc.api.ModInitializer;

public class StylishEffectsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CoreServices.FACTORIES.modConstructor(StylishEffects.MOD_ID).accept(new StylishEffects());
    }
}
