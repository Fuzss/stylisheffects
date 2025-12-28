package fuzs.stylisheffects.config;

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
    public boolean isRight() {
        return this == RIGHT;
    }

    /**
     * @return get the other side
     */
    public ScreenSide cycle() {
        return this.isRight() ? LEFT : RIGHT;
    }
}
