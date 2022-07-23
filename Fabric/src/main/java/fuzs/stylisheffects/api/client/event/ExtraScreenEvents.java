package fuzs.stylisheffects.api.client.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.Screen;

public class ExtraScreenEvents {
    public static final Event<Opening> OPENING = EventFactory.createArrayBacked(Opening.class, listeners -> (Screen oldScreen, Screen newScreen) -> {
        for (Opening event : listeners) {
            Screen screen = event.onScreenOpening(oldScreen, newScreen);
            if (screen != newScreen) {
                return screen;
            }
        }
        return newScreen;
    });
    public static final Event<Closing> CLOSING = EventFactory.createArrayBacked(Closing.class, listeners -> (Screen screen) -> {
        for (Closing event : listeners) {
            event.onScreenClosing(screen);
        }
    });
    public static final Event<RenderInventoryMobEffects> INVENTORY_MOB_EFFECTS = EventFactory.createArrayBacked(RenderInventoryMobEffects.class, listeners -> (Screen screen, int availableSpace, boolean compact) -> {
        for (RenderInventoryMobEffects event : listeners) {
            MobEffectsRenderMode mode = event.onRenderInventoryMobEffects(screen, availableSpace, compact);
            if (compact && mode != MobEffectsRenderMode.COMPACT || !compact && mode != MobEffectsRenderMode.FULL_SIZE) {
                return mode;
            }
        }
        return compact ? MobEffectsRenderMode.COMPACT : MobEffectsRenderMode.FULL_SIZE;
    });

    @FunctionalInterface
    public interface Opening {

        /**
         * called when a new screen is set in {@link net.minecraft.client.Minecraft#setScreen}
         * allows for replacing the new screen with a different one returned by this callback
         *
         * IMPORTANT: for cancelling a new screen from being set and to keep the old one, simply return <code>oldScreen</code>
         *          this is equivalent to cancelling the event on Forge
         *
         * DO NOT use {@link net.minecraft.client.Minecraft#setScreen} when this is called, there will be an infinite loop
         *
         * @param oldScreen     the screen that is being removed
         * @param newScreen     the new screen that is being set
         * @return              the screen that is actually going to be set, <code>newScreen</code> by default
         */
        Screen onScreenOpening(Screen oldScreen, Screen newScreen);
    }

    @FunctionalInterface
    public interface Closing {

        /**
         * called when a screen is closed by setting a null object to {@link net.minecraft.client.Minecraft#setScreen}
         *
         * @param screen the screen that has been closed
         */
        void onScreenClosing(Screen screen);
    }

    @FunctionalInterface
    public interface RenderInventoryMobEffects {

        /**
         * called before mob effects are drawn next to the inventory menu, used to choose the rendering mode if any
         *
         * @param screen            the screen
         * @param availableSpace    space available to the right of the menu
         * @param compact           is compact rendering mode selected by vanilla (this boolean should really be the other way around as in the vanilla method, but Forge has it this way)
         * @return                  the new rendering mode
         */
        MobEffectsRenderMode onRenderInventoryMobEffects(Screen screen, int availableSpace, boolean compact);
    }

    /**
     * modes for rendering inventory mob effects
     */
    public enum MobEffectsRenderMode {
        /**
         * full sized rendering as it was always done before Minecraft 1.18
         */
        FULL_SIZE,
        /**
         * the new compact rendering if not enough space is available
         */
        COMPACT,
        /**
         * rendering is prevented
         */
        NONE
    }
}
