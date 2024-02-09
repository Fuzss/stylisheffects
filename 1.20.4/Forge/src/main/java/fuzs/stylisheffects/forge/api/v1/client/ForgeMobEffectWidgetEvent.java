package fuzs.stylisheffects.forge.api.v1.client;

import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.forge.api.event.v1.core.ForgeEventInvokerRegistry;
import fuzs.stylisheffects.api.v1.client.EffectScreenHandler;
import fuzs.stylisheffects.api.v1.client.MobEffectWidgetContext;
import fuzs.stylisheffects.api.v1.client.MobEffectWidgetEvents;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Convenient client-side callbacks when dealing with Stylish Effect's effect widgets.
 * <p>
 * You may also want to look at {@link EffectScreenHandler}.
 */
public class ForgeMobEffectWidgetEvent extends Event {
    final MobEffectWidgetContext context;

    @ApiStatus.Internal
    public ForgeMobEffectWidgetEvent(MobEffectWidgetContext context) {
        this.context = context;
    }

    /**
     * @return the effect instance tooltips are being collected for
     */
    public MobEffectWidgetContext getContext() {
        return this.context;
    }

    static {
        if (ModLoaderEnvironment.INSTANCE.isClient()) {
            ForgeEventInvokerRegistry.INSTANCE.register(MobEffectWidgetEvents.MouseClicked.class, MouseClicked.class, (MobEffectWidgetEvents.MouseClicked callback, MouseClicked evt) -> {
                EventResult result = callback.onEffectMouseClicked(evt.context, evt.screen, evt.mouseX, evt.mouseY,
                        evt.button
                );
                if (result.isInterrupt()) evt.setCanceled(true);
            });
            ForgeEventInvokerRegistry.INSTANCE.register(MobEffectWidgetEvents.EffectTooltip.class, EffectTooltip.class, (MobEffectWidgetEvents.EffectTooltip callback, EffectTooltip evt) -> {
                callback.onGatherEffectTooltipLines(evt.context, evt.tooltipLines, evt.tooltipFlag);
            });
        }
    }

    /**
     * Called when the user clicks on an effect widget.
     * <p>
     * Cancel to signal the mouse click has been handled and to stop further processing.
     */
    @Cancelable
    public static class MouseClicked extends ForgeMobEffectWidgetEvent {
        private final Screen screen;
        private final double mouseX;
        private final double mouseY;
        private final int button;

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
         * @return the mouse button that has been clicked with can be identified by the constants in
         *         {@link org.lwjgl.glfw.GLFW GLFW}
         */
        public int getButton() {
            return this.button;
        }
    }

    /**
     * Called when tooltip lines are collected for a tooltip to render.
     * <p>
     * Modify the lines as you wish, clear it to prevent a tooltip from rendering at all.
     */
    public static class EffectTooltip extends ForgeMobEffectWidgetEvent {
        private final List<Component> tooltipLines;
        private final TooltipFlag tooltipFlag;

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
