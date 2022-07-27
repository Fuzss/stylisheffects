package fuzs.stylisheffects.compat.rei;

import fuzs.stylisheffects.api.client.StylishEffectsClientApi;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.stream.Collectors;

public class StylishEffectsReiPlugin implements REIClientPlugin {

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(AbstractContainerScreen.class, screen -> {
            return StylishEffectsClientApi.getEffectScreenHandler().getInventoryRenderAreas(screen).stream()
                    .map(rect2i -> new Rectangle(rect2i.getX(), rect2i.getY(), rect2i.getWidth(), rect2i.getHeight()))
                    .collect(Collectors.toList());
        });
    }
}
