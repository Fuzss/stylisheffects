package fuzs.stylisheffects.integration.jei;

import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.handler.EffectScreenHandler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;

import java.util.List;

@JeiPlugin
public class StylishEffectsJeiPlugin implements IModPlugin {

    @Override
    public Identifier getPluginUid() {
        return StylishEffects.id("main");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(AbstractContainerScreen.class, new IGuiContainerHandler<>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(AbstractContainerScreen<?> screen) {
                return EffectScreenHandler.getGuiExtraAreas(screen);
            }
        });
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        registration.getJeiFeatures().disableInventoryEffectRendererGuiHandler();
    }
}
