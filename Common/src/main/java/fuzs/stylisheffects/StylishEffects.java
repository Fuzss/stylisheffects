package fuzs.stylisheffects;

import fuzs.puzzleslib.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StylishEffects implements ModConstructor {
    public static final String MOD_ID = "stylisheffects";
    public static final String MOD_NAME = "Stylish Effects";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID).client(ClientConfig.class);

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains(".mixin.integration.jei.")) {
            return ModLoaderEnvironment.INSTANCE.isModLoadedSafe("jei");
        } else if (mixinClassName.contains(".mixin.integration.rei.")) {
            return ModLoaderEnvironment.INSTANCE.isModLoadedSafe("roughlyenoughitems");
        } else {
            return true;
        }
    }
}
