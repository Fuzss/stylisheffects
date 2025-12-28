package fuzs.stylisheffects.fabric;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.stylisheffects.StylishEffects;
import net.fabricmc.api.ModInitializer;

public class StylishEffectsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstructor.construct(StylishEffects.MOD_ID, StylishEffects::new);
    }
}
