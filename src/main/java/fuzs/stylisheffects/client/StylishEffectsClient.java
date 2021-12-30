package fuzs.stylisheffects.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.handler.EffectScreenHandler;
import fuzs.stylisheffects.mixin.client.accessor.ForgeIngameGuiAccessor;
import fuzs.stylisheffects.mixin.client.accessor.OverlayRegistryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = StylishEffects.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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
        replaceVanillaMountHud();
    }

    private static void replaceVanillaMountHud() {
        final IIngameOverlay foodLevelElement = (ForgeIngameGui gui, PoseStack poseStack, float partialTicks, int width, int height) -> {
            final Minecraft minecraft = Minecraft.getInstance();
            if (!minecraft.options.hideGui && gui.shouldDrawSurvivalElements()) {
                gui.setupOverlayRenderState(true, false);
                gui.renderFood(width, height, poseStack);
            }
        };
        final IIngameOverlay jumpBarElement = (ForgeIngameGui gui, PoseStack poseStack, float partialTicks, int width, int height) -> {
            final Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player.getJumpRidingScale() != 0.0F && !minecraft.options.hideGui) {
                gui.setupOverlayRenderState(true, false);
                gui.renderJumpMeter(poseStack, width / 2 - 91);
            }
        };
        final IIngameOverlay experienceBarElement = (ForgeIngameGui gui, PoseStack poseStack, float partialTicks, int width, int height) -> {
            final Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player.getJumpRidingScale() == 0.0F && !minecraft.options.hideGui) {
                gui.setupOverlayRenderState(true, false);
                ((ForgeIngameGuiAccessor) gui).callRenderExperience(width / 2 - 91, poseStack);
            }
        };
        replaceVanillaOverlays(foodLevelElement, jumpBarElement, experienceBarElement);
    }

    private static void replaceVanillaOverlays(IIngameOverlay foodLevelElement, IIngameOverlay jumpBarElement, IIngameOverlay experienceBarElement) {
        replaceVanillaOverlay(ForgeIngameGui.FOOD_LEVEL_ELEMENT, foodLevelElement);
        ForgeIngameGuiAccessor.setFoodLevelElement(foodLevelElement);
        replaceVanillaOverlay(ForgeIngameGui.JUMP_BAR_ELEMENT, jumpBarElement);
        ForgeIngameGuiAccessor.setJumpBarElement(jumpBarElement);
        replaceVanillaOverlay(ForgeIngameGui.EXPERIENCE_BAR_ELEMENT, experienceBarElement);
        ForgeIngameGuiAccessor.setExperienceBarElement(experienceBarElement);
    }

    private static void replaceVanillaOverlay(IIngameOverlay original, IIngameOverlay replacement) {
        final OverlayRegistry.OverlayEntry originalEntry = OverlayRegistryAccessor.getOverlays().remove(original);
        if (originalEntry != null) {
            final OverlayRegistry.OverlayEntry newEntry = new OverlayRegistry.OverlayEntry(replacement, originalEntry.getDisplayName());
            final List<OverlayRegistry.OverlayEntry> overlaysOrdered = OverlayRegistryAccessor.getOverlaysOrdered();
            final int index = overlaysOrdered.indexOf(originalEntry);
            if (index != -1) {
                overlaysOrdered.set(index, newEntry);
                OverlayRegistryAccessor.getOverlays().put(replacement, newEntry);
                // no way to set this directly for an OverlayEntry
                if (!originalEntry.isEnabled()) {
                    OverlayRegistry.enableOverlay(replacement, false);
                }
            }
        }
    }
}
