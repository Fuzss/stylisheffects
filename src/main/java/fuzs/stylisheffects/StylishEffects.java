package fuzs.stylisheffects;

import fuzs.puzzleslib.PuzzlesLib;
import fuzs.puzzleslib.element.AbstractElement;
import fuzs.puzzleslib.element.ElementRegistry;
import fuzs.stylisheffects.client.element.BetterModsButtonElement;
import fuzs.stylisheffects.client.element.PotionTimeElement;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("Convert2MethodRef")
@Mod(StylishEffects.MODID)
public class StylishEffects {

    public static final String MODID = "stylisheffects";
    public static final String NAME = "Stylish Effects";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    private static final ElementRegistry ELEMENT_REGISTRY = PuzzlesLib.create(MODID);

    public static final AbstractElement POTION_TIME = ELEMENT_REGISTRY.register("potion_time", () -> new PotionTimeElement(), Dist.CLIENT);
    public static final AbstractElement BETTER_MODS_BUTTON = ELEMENT_REGISTRY.register("better_mods_button", () -> new BetterModsButtonElement(), Dist.CLIENT);

    public StylishEffects() {

        PuzzlesLib.setup(true);
        PuzzlesLib.setSideOnly();
    }

}
