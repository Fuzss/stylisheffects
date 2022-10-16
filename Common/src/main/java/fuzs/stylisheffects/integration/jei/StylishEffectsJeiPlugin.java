package fuzs.stylisheffects.integration.jei;

import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.api.client.StylishEffectsClientApi;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@JeiPlugin
public class StylishEffectsJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(StylishEffects.MOD_ID, "gui_extra_areas");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(AbstractContainerScreen.class, new IGuiContainerHandler<>() {

            @Override
            public List<Rect2i> getGuiExtraAreas(AbstractContainerScreen<?> screen) {
                return StylishEffectsClientApi.getEffectScreenHandler().getInventoryRenderAreas(screen);
            }
        });
    }
}