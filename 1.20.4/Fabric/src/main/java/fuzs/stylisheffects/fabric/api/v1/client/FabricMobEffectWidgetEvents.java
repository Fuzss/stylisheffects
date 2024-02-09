package fuzs.stylisheffects.fabric.api.v1.client;

import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.puzzleslib.fabric.api.event.v1.core.FabricEventFactory;
import fuzs.puzzleslib.fabric.api.event.v1.core.FabricEventInvokerRegistry;
import fuzs.stylisheffects.api.v1.client.EffectScreenHandler;
import fuzs.stylisheffects.api.v1.client.MobEffectWidgetEvents;
import net.fabricmc.fabric.api.event.Event;

/**
 * Convenient client-side callbacks when dealing with Stylish Effect's effect widgets.
 * <p>
 * You may also want to look at {@link EffectScreenHandler}.
 */
public class FabricMobEffectWidgetEvents {
    public static final Event<MobEffectWidgetEvents.MouseClicked> CLICKED = FabricEventFactory.createResult(MobEffectWidgetEvents.MouseClicked.class);
    public static final Event<MobEffectWidgetEvents.EffectTooltip> TOOLTIP = FabricEventFactory.createResult(MobEffectWidgetEvents.EffectTooltip.class);

    static {
        if (ModLoaderEnvironment.INSTANCE.isClient()) {
            FabricEventInvokerRegistry.INSTANCE.register(MobEffectWidgetEvents.MouseClicked.class, CLICKED);
            FabricEventInvokerRegistry.INSTANCE.register(MobEffectWidgetEvents.EffectTooltip.class, TOOLTIP);
        }
    }
}
