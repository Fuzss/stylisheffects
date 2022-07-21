package fuzs.stylisheffects.client;

import fuzs.puzzleslib.client.core.ClientCoreServices;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.handler.EffectScreenHandler;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = StylishEffects.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class StylishEffectsForgeClient {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ClientCoreServices.FACTORIES.clientModConstructor(StylishEffects.MOD_ID).accept(new StylishEffectsClient());
        registerHandlers();
    }

    private static void registerHandlers() {
        MinecraftForge.EVENT_BUS.addListener((final ScreenEvent.Opening evt) -> {
            EffectScreenHandler.INSTANCE.onScreenOpen(evt.getNewScreen());
        });
        MinecraftForge.EVENT_BUS.addListener((final ContainerScreenEvent.Render.Background evt) -> {
            EffectScreenHandler.INSTANCE.onDrawBackground(evt.getPoseStack(), evt.getMouseX(), evt.getMouseY(), evt.getContainerScreen());
        });
        MinecraftForge.EVENT_BUS.addListener((final ScreenEvent.RenderInventoryMobEffects evt) -> {
            // disable vanilla effect rendering in inventory screen
            evt.setCanceled(true);
        });
        MinecraftForge.EVENT_BUS.addListener((final RenderGuiOverlayEvent evt) -> {
            if (evt.getOverlay() == VanillaGuiOverlay.POTION_ICONS.type()) {
                EffectScreenHandler.INSTANCE.onRenderGameOverlayText(evt.getPoseStack(), evt.getWindow().getGuiScaledWidth(), evt.getWindow().getGuiScaledHeight());
                evt.setCanceled(true);
            }
        });
    }
}
