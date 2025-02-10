package fuzs.stylisheffects.api.v1.client;

import fuzs.puzzleslib.api.event.v1.core.EventInvoker;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Convenient client-side callbacks when dealing with Stylish Effect's effect widgets.
 * <p>
 * You may also want to look at {@link EffectScreenHandler}.
 * <p>
 * Note that the events can be used in a common module via Puzzles Lib's event invoker system.
 */
public final class MobEffectWidgetEvents {
    public static final EventInvoker<MouseClicked> CLICKED = EventInvoker.lookup(MouseClicked.class);
    public static final EventInvoker<EffectTooltip> TOOLTIP = EventInvoker.lookup(EffectTooltip.class);

    private MobEffectWidgetEvents() {
        // NO-OP
    }

    @FunctionalInterface
    public interface MouseClicked {

        /**
         * Called when the user clicks on an effect widget.
         *
         * @param context the effect instance that has been clicked
         * @param screen  the screen effects are currently rendered on
         * @param mouseX  mouse x screen coordinate
         * @param mouseY  mouse y screen coordinate
         * @param button  the mouse button that has been clicked with can be identified by the constants in
         *                {@link com.mojang.blaze3d.platform.InputConstants}
         * @return {@link EventResult#INTERRUPT} to mark the click has been handled to prevent further processing,
         *         {@link EventResult#PASS} for the click to be processed normally.
         */
        EventResult onEffectMouseClicked(MobEffectWidgetContext context, Screen screen, double mouseX, double mouseY, int button);
    }

    @FunctionalInterface
    public interface EffectTooltip {

        /**
         * Called when tooltip lines are collected for a tooltip to render.
         * <p>
         * Modify the lines as you wish, clear it to prevent a tooltip from rendering at all.
         *
         * @param context      the effect instance tooltips are being collected for
         * @param tooltipLines the default components currently container in the tooltip
         * @param tooltipFlag  vanilla's {@link TooltipFlag}, not used in the default implementation
         */
        void onGatherEffectTooltipLines(MobEffectWidgetContext context, List<Component> tooltipLines, TooltipFlag tooltipFlag);
    }
}
