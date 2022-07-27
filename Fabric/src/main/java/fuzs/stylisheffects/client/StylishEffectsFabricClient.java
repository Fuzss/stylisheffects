package fuzs.stylisheffects.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.puzzleslib.client.core.ClientCoreServices;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.api.client.event.ContainerScreenEvents;
import fuzs.stylisheffects.api.client.event.MobEffectWidgetEvents;
import fuzs.stylisheffects.api.client.event.RenderGuiElementEvents;
import fuzs.stylisheffects.api.client.event.ExtraScreenEvents;
import fuzs.stylisheffects.client.handler.EffectScreenHandlerImpl;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.effect.MobEffectInstance;

public class StylishEffectsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCoreServices.FACTORIES.clientModConstructor(StylishEffects.MOD_ID).accept(new StylishEffectsClient());
        registerHandlers();
    }

    private static void registerHandlers() {
        ClientTickEvents.END_CLIENT_TICK.register(EffectScreenHandlerImpl.INSTANCE::onClientTick);
        ExtraScreenEvents.OPENING.register((Screen oldScreen, Screen newScreen) -> {
            EffectScreenHandlerImpl.INSTANCE.onScreenOpen(newScreen);
            return newScreen;
        });
        ContainerScreenEvents.BACKGROUND.register(EffectScreenHandlerImpl.INSTANCE::onDrawBackground);
        ContainerScreenEvents.FOREGROUND.register(EffectScreenHandlerImpl.INSTANCE::onDrawForeground);
        ExtraScreenEvents.INVENTORY_MOB_EFFECTS.register((Screen screen, int availableSpace, boolean compact) -> {
            // disable vanilla effect rendering in inventory screen
            return ExtraScreenEvents.MobEffectsRenderMode.NONE;
        });
        RenderGuiElementEvents.BEFORE.register((RenderGuiElementEvents.ElementType elementType, PoseStack poseStack, int screenWidth, int screenHeight) -> {
            if (elementType == RenderGuiElementEvents.ElementType.MOB_EFFECT_ICONS) {
                EffectScreenHandlerImpl.INSTANCE.onRenderMobEffectIconsOverlay(poseStack, screenWidth, screenHeight);
                return false;
            }
            return true;
        });
        ScreenEvents.BEFORE_INIT.register((Minecraft client, Screen screen, int scaledWidth, int scaledHeight) -> {
            ScreenMouseEvents.beforeMouseClick(screen).register(EffectScreenHandlerImpl.INSTANCE::onMouseClicked);
        });
        ScreenEvents.AFTER_INIT.register((Minecraft client, Screen screen, int scaledWidth, int scaledHeight) -> {
            EffectScreenHandlerImpl.INSTANCE.onScreenInit(screen);
        });
    }
}
