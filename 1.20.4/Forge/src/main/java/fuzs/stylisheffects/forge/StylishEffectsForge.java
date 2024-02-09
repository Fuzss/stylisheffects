package fuzs.stylisheffects.forge;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.stylisheffects.StylishEffects;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod(StylishEffects.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class StylishEffectsForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(StylishEffects.MOD_ID, StylishEffects::new);
    }
}
