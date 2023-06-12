package fuzs.stylisheffects.client;

import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.event.v1.*;
import fuzs.puzzleslib.api.core.v1.context.ModLifecycleContext;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.MutableBoolean;
import fuzs.puzzleslib.api.event.v1.data.MutableInt;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.api.client.stylisheffects.v1.EffectScreenHandler;
import fuzs.stylisheffects.client.handler.EffectScreenHandlerImpl;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class StylishEffectsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        ClientModConstructor.super.onConstructMod();
        registerHandlers();
    }

    private static void registerHandlers() {
        ClientTickEvents.END.register(EffectScreenHandlerImpl.INSTANCE::onClientTick);
        ScreenOpeningCallback.EVENT.register(EffectScreenHandlerImpl.INSTANCE::onScreenOpen);
        ContainerScreenEvents.BACKGROUND.register(EffectScreenHandlerImpl.INSTANCE::onDrawBackground);
        ContainerScreenEvents.FOREGROUND.register(EffectScreenHandlerImpl.INSTANCE::onDrawForeground);
        InventoryMobEffectsCallback.EVENT.register((Screen screen, int availableSpace, MutableBoolean smallWidgets, MutableInt horizontalOffset) -> {
            // disable vanilla effect rendering in inventory screen
            return EventResult.INTERRUPT;
        });
        RenderGuiElementEvents.before(RenderGuiElementEvents.POTION_ICONS).register(EffectScreenHandlerImpl.INSTANCE::onRenderMobEffectIconsOverlay);
        ScreenMouseEvents.beforeMouseClick(AbstractContainerScreen.class).register(EffectScreenHandlerImpl.INSTANCE::onMouseClicked);
        ScreenEvents.AFTER_INIT.register(EffectScreenHandlerImpl.INSTANCE::onAfterInit);
    }

    @Override
    public void onClientSetup(ModLifecycleContext context) {
        // can't do this during construct as configs won't be loaded then
        EffectScreenHandler.INSTANCE.rebuildEffectRenderers();
        StylishEffects.CONFIG.getHolder(ClientConfig.class).accept(EffectScreenHandler.INSTANCE::rebuildEffectRenderers);
    }
}
