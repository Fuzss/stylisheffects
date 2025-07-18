package fuzs.stylisheffects.fabric.api.v1.client;

import fuzs.puzzleslib.fabric.api.event.v1.core.FabricEventFactory;
import fuzs.stylisheffects.api.v1.client.EffectScreenHandler;
import fuzs.stylisheffects.api.v1.client.MobEffectWidgetEvents;
import net.fabricmc.fabric.api.event.Event;

/**
 * Convenient client-side callbacks when dealing with Stylish Effect's effect widgets.
 * <p>
 * You may also want to look at {@link EffectScreenHandler}.
 */
public class FabricMobEffectWidgetEvents {
    public static final Event<MobEffectWidgetEvents.MouseClicked> CLICKED = FabricEventFactory.createResult(
            MobEffectWidgetEvents.MouseClicked.class);
    public static final Event<MobEffectWidgetEvents.EffectTooltip> TOOLTIP = FabricEventFactory.create(
            MobEffectWidgetEvents.EffectTooltip.class);
}
