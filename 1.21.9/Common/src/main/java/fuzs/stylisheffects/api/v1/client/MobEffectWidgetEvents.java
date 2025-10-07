package fuzs.stylisheffects.api.v1.client;

import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.puzzleslib.api.event.v1.core.EventInvoker;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.stylisheffects.services.ClientAbstractions;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
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

    static {
        if (ModLoaderEnvironment.INSTANCE.isClient()) {
            ClientAbstractions.INSTANCE.registerEventHandlers();
        }
    }

    @FunctionalInterface
    public interface MouseClicked {

        /**
         * Called when the user clicks on an effect widget.
         *
         * @param context          the effect instance that has been clicked
         * @param screen           the currently displayed screen
         * @param mouseButtonEvent the mouse button event; for bundled values see
         *                         {@link com.mojang.blaze3d.platform.InputConstants}
         * @return <ul>
         *         <li>{@link EventResult#INTERRUPT INTERRUPT} to mark the click has been handled to prevent further processing</li>
         *         <li>{@link EventResult#PASS PASS} for the click to be processed normally</li>
         *         </ul>
         */
        EventResult onEffectMouseClicked(MobEffectWidgetContext context, Screen screen, MouseButtonEvent mouseButtonEvent, boolean doubleClick);
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
