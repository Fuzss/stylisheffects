package fuzs.stylisheffects.compat.jei;

import fuzs.stylisheffects.client.gui.effects.AbstractEffectRenderer;
import fuzs.stylisheffects.client.handler.EffectScreenHandler;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.inventory.container.Container;
import net.minecraft.potion.EffectInstance;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

class EffectRendererGuiHandler<T extends Container> implements IGuiContainerHandler<ContainerScreen<T>> {
    @Override
    public List<Rectangle2d> getGuiExtraAreas(ContainerScreen<T> screen) {
        // field may get changed during config reload from different thread
        final AbstractEffectRenderer inventoryRenderer = EffectScreenHandler.inventoryRenderer;
        if (inventoryRenderer != null) {
            return EffectScreenHandler.inventoryRenderer.getRenderAreas();
        }
        return Collections.emptyList();
    }
}
