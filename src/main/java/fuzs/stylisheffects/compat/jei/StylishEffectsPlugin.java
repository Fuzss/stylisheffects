package fuzs.stylisheffects.compat.jei;

import fuzs.stylisheffects.StylishEffects;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("unused")
@JeiPlugin
public class StylishEffectsPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {

        return new ResourceLocation(StylishEffects.MODID, StylishEffects.MODID);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {

//        registration.addGenericGuiContainerHandler(ContainerScreen.class, new InventoryEffectRendererGuiHandler<>());
    }

}
