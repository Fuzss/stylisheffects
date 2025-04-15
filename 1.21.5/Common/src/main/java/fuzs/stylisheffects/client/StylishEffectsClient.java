package fuzs.stylisheffects.client;

import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.event.v1.ClientTickEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.*;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.MutableBoolean;
import fuzs.puzzleslib.api.event.v1.data.MutableInt;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.api.v1.client.EffectScreenHandler;
import fuzs.stylisheffects.client.handler.EffectScreenHandlerImpl;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class StylishEffectsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        ClientTickEvents.END.register(EffectScreenHandlerImpl.INSTANCE::onClientTick);
        ScreenOpeningCallback.EVENT.register(EffectScreenHandlerImpl.INSTANCE::onScreenOpening);
        ContainerScreenEvents.BACKGROUND.register(EffectScreenHandlerImpl.INSTANCE::onDrawBackground);
        ContainerScreenEvents.FOREGROUND.register(EffectScreenHandlerImpl.INSTANCE::onDrawForeground);
        InventoryMobEffectsCallback.EVENT.register((Screen screen, int availableSpace, MutableBoolean smallWidgets, MutableInt horizontalOffset) -> {
            // disable vanilla effect rendering in inventory screen
            return EventResult.INTERRUPT;
        });
        RenderGuiLayerEvents.before(RenderGuiLayerEvents.EFFECTS).register(EffectScreenHandlerImpl.INSTANCE::onBeforeRenderGuiLayer);
        ScreenMouseEvents.beforeMouseClick(AbstractContainerScreen.class).register(EffectScreenHandlerImpl.INSTANCE::onMouseClicked);
        ScreenEvents.afterInit(Screen.class).register(EffectScreenHandlerImpl.INSTANCE::onAfterInit);
    }

    @Override
    public void onClientSetup() {
        // can't do this during construct as configs won't be loaded then
        EffectScreenHandler.INSTANCE.rebuildEffectRenderers();
        StylishEffects.CONFIG.getHolder(ClientConfig.class).addCallback(EffectScreenHandler.INSTANCE::rebuildEffectRenderers);
    }
}
