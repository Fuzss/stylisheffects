package fuzs.stylisheffects.config;

public enum ScreenSide {
    LEFT,
    RIGHT;

    public boolean isLeft() {
        return this == LEFT;
    }

    public boolean isRight() {
        return this == RIGHT;
    }

    public ScreenSide flip() {
        return this == RIGHT ? LEFT : RIGHT;
    }
}
