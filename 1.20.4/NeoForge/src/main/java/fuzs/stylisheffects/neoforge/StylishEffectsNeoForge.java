package fuzs.stylisheffects.neoforge;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.stylisheffects.StylishEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

@Mod(StylishEffects.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class StylishEffectsNeoForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(StylishEffects.MOD_ID, StylishEffects::new);
    }
}
