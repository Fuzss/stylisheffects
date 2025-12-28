package fuzs.stylisheffects.config;

import java.util.Locale;

/**
 * type of renderer that is used
 */
public enum WidgetType {
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
