package fuzs.stylisheffects.api.client.event;

import fuzs.stylisheffects.api.client.MobEffectWidgetContext;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * convenient client-side callbacks when dealing with Stylish Effect's effect widgets
 * you may also want to look at {@link fuzs.stylisheffects.api.client.EffectScreenHandler}
 */
public class MobEffectWidgetEvents {
    public static final Event<MouseClicked> CLICKED = EventFactory.createArrayBacked(MouseClicked.class, listeners -> (MobEffectWidgetContext context, Screen screen, double mouseX, double mouseY, int button) -> {
        for (MouseClicked event : listeners) {
            if (event.onEffectMouseClicked(context, screen, mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    });
    public static final Event<EffectTooltip> TOOLTIP = EventFactory.createArrayBacked(EffectTooltip.class, listeners -> (MobEffectWidgetContext context, List<Component> tooltipLines, TooltipFlag tooltipFlag) -> {
        for (EffectTooltip event : listeners) {
            event.onGatherEffectTooltipLines(context, tooltipLines, tooltipFlag);
        }
    });

    @FunctionalInterface
    public interface MouseClicked {

        /**
         * called when the user clicks on an effect widget, return <code>true</code> to stop further processing from the click
         *
         * @param context           the effect instance that has been clicked
         * @param screen            the screen effects are currently rendered on
         * @param mouseX            mouse x screen coordinate
         * @param mouseY            mouse y screen coordinate
         * @param button            the mouse button that has been clicked with can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
         * @return                  <code>true</code> when the click has been handled to prevent further processing
         */
        boolean onEffectMouseClicked(MobEffectWidgetContext context, Screen screen, double mouseX, double mouseY, int button);
    }

    @FunctionalInterface
    public interface EffectTooltip {

        /**
         * called when tooltip lines are collected for a tooltip to render
         * modify <code>tooltipLines</code> as you wish, clear it to prevent a tooltip from rendering at all
         *
         * @param context           the effect instance tooltips are being collected for
         * @param tooltipLines      the default components currently container in the tooltip
         * @param tooltipFlag       vanilla's {@link TooltipFlag}, not used in the default implementation
         */
        void onGatherEffectTooltipLines(MobEffectWidgetContext context, List<Component> tooltipLines, TooltipFlag tooltipFlag);
    }
}
