package fuzs.stylisheffects.api.client;

import org.jetbrains.annotations.ApiStatus;

/**
 * main api class for storing active {@link EffectScreenHandler}
 */
public class StylishEffectsClientApi {
    /**
     * the handler for rendering screen effects, exposes a few convenient methods
     */
    private static EffectScreenHandler handler;

    /**
     * utility class
     */
    private StylishEffectsClientApi() {

    }

    /**
     * @return the handler in use
     */
    public static EffectScreenHandler getEffectScreenHandler() {
        return StylishEffectsClientApi.handler;
    }

    /**
     * used to set handler during client start-up of Stylish Effects
     *
     * @param handler the handler in use
     */
    @ApiStatus.Internal
    public static void setEffectScreenHandler(EffectScreenHandler handler) {
        StylishEffectsClientApi.handler = handler;
    }
}
