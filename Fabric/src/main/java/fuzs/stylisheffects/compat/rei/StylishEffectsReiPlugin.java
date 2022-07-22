package fuzs.stylisheffects.compat.rei;

import fuzs.stylisheffects.client.gui.effects.AbstractEffectRenderer;
import fuzs.stylisheffects.client.handler.EffectScreenHandler;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StylishEffectsReiPlugin implements REIClientPlugin {

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(AbstractContainerScreen.class, screen -> {
            return this.getGuiExtraAreas(screen).stream()
                    .map(rect2i -> new Rectangle(rect2i.getX(), rect2i.getY(), rect2i.getWidth(), rect2i.getHeight()))
                    .collect(Collectors.toList());
        });
    }

    private List<Rect2i> getGuiExtraAreas(AbstractContainerScreen<?> screen) {
        // field may get changed during config reload from different thread
        final AbstractEffectRenderer inventoryRenderer = EffectScreenHandler.createRendererOrFallback(screen);
        if (inventoryRenderer != null) {
            return inventoryRenderer.getRenderAreas();
        }
        return Collections.emptyList();
    }
}
