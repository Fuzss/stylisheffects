package fuzs.stylisheffects;

import fuzs.puzzleslib.config.ConfigHolderV2;
import fuzs.puzzleslib.core.CoreServices;
import fuzs.puzzleslib.core.ModConstructor;
import fuzs.stylisheffects.config.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StylishEffects implements ModConstructor {
    public static final String MOD_ID = "stylisheffects";
    public static final String MOD_NAME = "Stylish Effects";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolderV2 CONFIG = CoreServices.FACTORIES.client(ClientConfig.class, () -> new ClientConfig());

    @Override
    public void onConstructMod() {
        CONFIG.bakeConfigs(MOD_ID);
    }
}
