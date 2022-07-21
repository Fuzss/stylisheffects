package fuzs.stylisheffects.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.puzzleslib.client.core.ClientCoreServices;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.api.client.event.ContainerScreenEvents;
import fuzs.stylisheffects.api.client.event.RenderGuiElementEvents;
import fuzs.stylisheffects.api.client.event.ScreenEvents;
import fuzs.stylisheffects.client.handler.EffectScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.Screen;

public class StylishEffectsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCoreServices.FACTORIES.clientModConstructor(StylishEffects.MOD_ID).accept(new StylishEffectsClient());
        registerHandlers();
    }

    private static void registerHandlers() {
        ScreenEvents.OPENING.register((Screen oldScreen, Screen newScreen) -> {
            EffectScreenHandler.INSTANCE.onScreenOpen(newScreen);
            return newScreen;
        });
        ContainerScreenEvents.BACKGROUND.register(EffectScreenHandler.INSTANCE::onDrawBackground);
        ScreenEvents.INVENTORY_MOB_EFFECTS.register((Screen screen, int availableSpace, boolean compact) -> {
            // disable vanilla effect rendering in inventory screen
            return ScreenEvents.MobEffectsRenderMode.NONE;
        });
        RenderGuiElementEvents.BEFORE.register((RenderGuiElementEvents.ElementType elementType, PoseStack poseStack, int screenWidth, int screenHeight) -> {
            if (elementType == RenderGuiElementEvents.ElementType.MOB_EFFECT_ICONS) {
                EffectScreenHandler.INSTANCE.onRenderGameOverlayText(poseStack, screenWidth, screenHeight);
                return false;
            }
            return true;
        });
    }
}
