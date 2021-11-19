package fuzs.stylisheffects.compat.jei;

import fuzs.stylisheffects.client.gui.effects.AbstractEffectRenderer;
import fuzs.stylisheffects.client.handler.EffectScreenHandler;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

class EffectRendererGuiHandler<T extends AbstractContainerMenu> implements IGuiContainerHandler<AbstractContainerScreen<T>> {

    @Override
    public List<Rect2i> getGuiExtraAreas(AbstractContainerScreen<T> screen) {
        // field may get changed during config reload from different thread
        final AbstractEffectRenderer inventoryRenderer = EffectScreenHandler.INSTANCE.inventoryRenderer;
        if (inventoryRenderer != null && EffectScreenHandler.INSTANCE.supportsEffectsDisplay(screen)) {
            Minecraft minecraft = screen.getMinecraft();
            Collection<MobEffectInstance> activePotionEffects = minecraft.player.getActiveEffects();
            if (!activePotionEffects.isEmpty()) {
                return EffectScreenHandler.INSTANCE.inventoryRenderer.getRenderAreas();
            }
        }
        return Collections.emptyList();
    }
}
