package fuzs.stylisheffects.api.client;

import net.minecraft.world.effect.MobEffectInstance;

import java.util.Locale;

/**
 * data context for mob effect widgets
 *
 * @param effectInstance    effect instance rendered on this widget
 * @param renderer          renderer type
 * @param screenSide        inventory/screen side we are rendering on
 */
public record MobEffectWidgetContext(MobEffectInstance effectInstance, Renderer renderer, ScreenSide screenSide) {

    /**
     * factory method
     *
     * @param effectInstance    effect instance rendered on this widget
     * @param renderer          renderer type
     * @param screenSide        inventory/screen side we are rendering on
     * @return                  new instance
     */
    public static MobEffectWidgetContext of(MobEffectInstance effectInstance, Renderer renderer, ScreenSide screenSide) {
        return new MobEffectWidgetContext(effectInstance, renderer, screenSide);
    }

    /**
     * type of renderer that is used
     */
    public enum Renderer {
        /**
         * nothing is rendered
         */
        NONE,
        /**
         * vanilla's native effect rendering on the in-game gui
         */
        GUI_SMALL,
        /**
         * our default rendering, similar to {@link #GUI_SMALL}, just slightly larger with more information
         */
        GUI_COMPACT,
        /**
         * vanilla's compact inventory widgets
         */
        INVENTORY_COMPACT,
        /**
         * vanilla's full sized inventory widgets
         */
        INVENTORY_FULL_SIZE;

        /**
         * @return is this widget using a compact renderer that does not show the mob effect name as proper text
         */
        public boolean isCompact() {
            return this != NONE && this != INVENTORY_FULL_SIZE;
        }

        @Override
        public String toString() {
            return this.name().toLowerCase(Locale.ROOT) + "_widget";
        }
    }

    /**
     * screen side effect widgets are rendered on
     */
    public enum ScreenSide {
        /**
         * the left screen side
         */
        LEFT,
        /**
         * the right screen side
         */
        RIGHT;

        /**
         * @return is this the right side
         */
        public boolean right() {
            return this == RIGHT;
        }

        /**
         * @return get the other side
         */
        public ScreenSide inverse() {
            return this.right() ? LEFT : RIGHT;
        }
    }
}
