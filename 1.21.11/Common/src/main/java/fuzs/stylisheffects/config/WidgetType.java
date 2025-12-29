package fuzs.stylisheffects.config;

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
    GUI_SQUARE,
    /**
     * our default rendering, similar to {@link #GUI_SQUARE}, just slightly larger with more information
     */
    GUI_RECTANGLE,
    /**
     * vanilla's compact inventory widgets
     */
    INVENTORY_SQUARE,
    /**
     * vanilla's full sized inventory widgets
     */
    INVENTORY_RECTANGLE
}
