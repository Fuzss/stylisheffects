package fuzs.stylisheffects.client;

import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.handler.EffectScreenHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = StylishEffects.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class StylishEffectsClient {
    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        EffectScreenHandler handler = new EffectScreenHandler();
        MinecraftForge.EVENT_BUS.addListener(handler::onPotionShift);
        MinecraftForge.EVENT_BUS.addListener(handler::onGuiOpen);
        MinecraftForge.EVENT_BUS.addListener(handler::onInitGuiPost);
        MinecraftForge.EVENT_BUS.addListener(handler::onDrawScreenPost);
        MinecraftForge.EVENT_BUS.addListener(handler::onRenderGameOverlayPre);
        MinecraftForge.EVENT_BUS.addListener(handler::onRenderGameOverlayText);
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent evt) {
        EffectScreenHandler.createHudRenderer();
        StylishEffects.CONFIG.addClientCallback(EffectScreenHandler::createHudRenderer);
    }
}
