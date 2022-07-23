package fuzs.stylisheffects.api.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;
import java.util.Optional;

/**
 * basic methods exposed by our EffectScreenHandler for other mods (mainly JEI/REI compat)
 * obtain an active instance from {@link StylishEffectsClientApi#getEffectScreenHandler()}
 */
public interface EffectScreenHandler {

    /**
     * rebuild effect renderers (only gui effect renderer actually) after e.g. a config reload
     */
    void rebuildEffectRenderers();

    /**
     * the effect widget the mouse is currently hovering, only available inside of a menu
     *
     * @param screen    the current screen
     * @param mouseX    mouse x screen coordinate
     * @param mouseY    mouse y screen coordinate
     * @return          optional context for hovered effect
     */
    Optional<MobEffectWidgetContext> getInventoryHoveredEffect(Screen screen, double mouseX, double mouseY);

    /**
     * @param screen    the current screen
     * @return          mob effect widgets mapped to the rectangular area they occupy for JEI/REI to prevent rendering anything there
     */
    List<Rect2i> getInventoryRenderAreas(Screen screen);
}
