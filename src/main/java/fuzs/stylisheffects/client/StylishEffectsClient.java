package fuzs.stylisheffects.client;

import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.handler.EffectScreenHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = StylishEffects.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class StylishEffectsClient {
    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        MinecraftForge.EVENT_BUS.addListener(EffectScreenHandler.INSTANCE::onScreenOpen);
        MinecraftForge.EVENT_BUS.addListener(EffectScreenHandler.INSTANCE::onDrawBackground);
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent evt) {
        OverlayRegistry.enableOverlay(ForgeIngameGui.POTION_ICONS_ELEMENT, false);
        OverlayRegistry.registerOverlayBelow(ForgeIngameGui.HUD_TEXT_ELEMENT, "Mod Potion Icons", (gui, poseStack, partialTicks, screenWidth, screenHeight) -> {
            EffectScreenHandler.INSTANCE.onRenderGameOverlayText(poseStack, screenWidth, screenHeight);
        });
        // can't do this during construct as configs won't be loaded then
        EffectScreenHandler.INSTANCE.createHudRenderer();
        StylishEffects.CONFIG.addClientCallback(EffectScreenHandler.INSTANCE::createHudRenderer);
    }
}
