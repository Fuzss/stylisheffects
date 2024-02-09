package fuzs.stylisheffects.neoforge.api.v1.client;

import fuzs.stylisheffects.api.v1.client.EffectScreenHandler;
import fuzs.stylisheffects.api.v1.client.MobEffectWidgetContext;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * convenient client-side events when dealing with Stylish Effect's effect widgets
 * you may also want to look at {@link EffectScreenHandler}
 */
public class NeoForgeMobEffectWidgetEvent extends Event {
    /**
     * the effect instance tooltips are being collected for
     */
    private final MobEffectWidgetContext context;

    /**
     * internal event constructor
     */
    @ApiStatus.Internal
    public NeoForgeMobEffectWidgetEvent(MobEffectWidgetContext context) {
        this.context = context;
    }

    /**
     * @return the effect instance tooltips are being collected for
     */
    public MobEffectWidgetContext getContext() {
        return this.context;
    }

    /**
     * called when the user clicks on an effect widget
     * cancel this to signal the mouse click has been handled and to stop further processing
     */
    public static class MouseClicked extends NeoForgeMobEffectWidgetEvent implements ICancellableEvent {
        /**
         * the screen effects are currently rendered on
         */
        private final Screen screen;
        /**
         * mouse x screen coordinate
         */
        private final double mouseX;
        /**
         * mouse y screen coordinate
         */
        private final double mouseY;
        /**
         * the mouse button that has been clicked with can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
         */
        private final int button;

        /**
         * internal event constructor
         */
        @ApiStatus.Internal
        public MouseClicked(MobEffectWidgetContext context, Screen screen, double mouseX, double mouseY, int button) {
            super(context);
            this.screen = screen;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.button = button;
        }

        /**
         * @return the screen effects are currently rendered on
         */
        public Screen getScreen() {
            return this.screen;
        }

        /**
         * @return mouse x screen coordinate
         */
        public double getMouseX() {
            return this.mouseX;
        }

        /**
         * @return mouse y screen coordinate
         */
        public double getMouseY() {
            return this.mouseY;
        }

        /**
         * @return the mouse button that has been clicked with can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
         */
        public int getButton() {
            return this.button;
        }
    }

    /**
     * called when tooltip lines are collected for a tooltip to render
     * modify <code>tooltipLines</code> as you wish, clear it to prevent a tooltip from rendering at all
     */
    public static class EffectTooltip extends NeoForgeMobEffectWidgetEvent {
        /**
         * the default components currently container in the tooltip
         */
        private final List<Component> tooltipLines;
        /**
         * vanilla's {@link TooltipFlag}, not used in the default implementation
         */
        private final TooltipFlag tooltipFlag;

        /**
         * internal event constructor
         */
        @ApiStatus.Internal
        public EffectTooltip(MobEffectWidgetContext context, List<Component> tooltipLines, TooltipFlag tooltipFlag) {
            super(context);
            this.tooltipLines = tooltipLines;
            this.tooltipFlag = tooltipFlag;
        }

        /**
         * @return the default components currently container in the tooltip
         */
        public List<Component> getTooltipLines() {
            return this.tooltipLines;
        }

        /**
         * @return vanilla's {@link TooltipFlag}, not used in the default implementation
         */
        public TooltipFlag getTooltipFlag() {
            return this.tooltipFlag;
        }
    }
}
