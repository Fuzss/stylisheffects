package fuzs.stylisheffects.config;

public enum BarPosition {
    CENTER(true) {
        @Override
        public float getScaledHeight(float height, float scale) {
            return height * scale;
        }
    },
    TOP(false) {
        @Override
        public float getScaledWidth(float width, float scale) {
            return width * scale;
        }
    },
    RIGHT(true) {
        @Override
        public float getScaledHeight(float height, float scale) {
            return height * scale;
        }
    },
    BOTTOM(false) {
        @Override
        public float getScaledWidth(float width, float scale) {
            return width * scale;
        }
    },
    LEFT(true) {
        @Override
        public float getScaledHeight(float height, float scale) {
            return height * scale;
        }
    };

    public final boolean flipAxis;

    BarPosition(boolean flipAxis) {
        this.flipAxis = flipAxis;
    }

    public float getScaledWidth(float width, float scale) {
        return width;
    }

    public float getScaledHeight(float height, float scale) {
        return height;
    }
}
