package fuzs.stylisheffects.client;

import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.GuiLayersContext;
import fuzs.puzzleslib.api.client.event.v1.ClientLifecycleEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.PrepareInventoryMobEffectsCallback;
import fuzs.puzzleslib.api.client.event.v1.gui.ScreenEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.ScreenOpeningCallback;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.MutableBoolean;
import fuzs.puzzleslib.api.event.v1.data.MutableInt;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.handler.EffectScreenHandler;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class StylishEffectsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        ClientLifecycleEvents.STARTED.register(EffectScreenHandler::rebuildGuiRenderer);
        ScreenEvents.afterInit(AbstractContainerScreen.class).register(EffectScreenHandler::onAfterInit);
        ScreenEvents.afterBackground(AbstractContainerScreen.class).register(EffectScreenHandler::onAfterBackground);
        ScreenOpeningCallback.EVENT.register(EffectScreenHandler::onScreenOpening);
        PrepareInventoryMobEffectsCallback.EVENT.register((Screen screen, int maxWidth, MutableBoolean smallWidgets, MutableInt horizontalPosition) -> {
            return EventResult.INTERRUPT;
        });
    }

    @Override
    public void onClientSetup() {
        StylishEffects.CONFIG.getHolder(ClientConfig.class)
                .addCallback(() -> EffectScreenHandler.rebuildGuiRenderer(Minecraft.getInstance()));
    }

    @Override
    public void onRegisterGuiLayers(GuiLayersContext context) {
        context.replaceGuiLayer(GuiLayersContext.STATUS_EFFECTS, (GuiLayersContext.Layer layer) -> {
            return EffectScreenHandler::renderStatusEffects;
        });
    }
}
