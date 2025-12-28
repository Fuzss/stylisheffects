package fuzs.stylisheffects.client;

import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.GuiLayersContext;
import fuzs.puzzleslib.api.client.event.v1.ClientTickEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.PrepareInventoryMobEffectsCallback;
import fuzs.puzzleslib.api.client.event.v1.gui.ScreenEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.ScreenOpeningCallback;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.MutableBoolean;
import fuzs.puzzleslib.api.event.v1.data.MutableInt;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.handler.EffectScreenHandler;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class StylishEffectsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        ClientTickEvents.END.register(EffectScreenHandler.INSTANCE::onClientTick);
        ScreenOpeningCallback.EVENT.register(EffectScreenHandler.INSTANCE::onScreenOpening);
        ScreenEvents.afterBackground(AbstractContainerScreen.class)
                .register(EffectScreenHandler.INSTANCE::onAfterBackground);
        ScreenEvents.afterInit(Screen.class).register(EffectScreenHandler.INSTANCE::onAfterInit);
        PrepareInventoryMobEffectsCallback.EVENT.register((Screen screen, int availableSpace, MutableBoolean smallWidgets, MutableInt horizontalOffset) -> {
            // disable vanilla effect rendering in inventory screen
            return EventResult.INTERRUPT;
        });
    }

    @Override
    public void onClientSetup() {
        // can't do this during construct as configs won't be loaded then
        EffectScreenHandler.INSTANCE.rebuildEffectRenderers();
        StylishEffects.CONFIG.getHolder(ClientConfig.class)
                .addCallback(EffectScreenHandler.INSTANCE::rebuildEffectRenderers);
    }

    @Override
    public void onRegisterGuiLayers(GuiLayersContext context) {
        context.replaceGuiLayer(GuiLayersContext.STATUS_EFFECTS, (GuiLayersContext.Layer layer) -> {
            return EffectScreenHandler.INSTANCE::renderStatusEffects;
        });
    }
}
