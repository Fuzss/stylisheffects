package fuzs.stylisheffects.api.client.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class MobEffectWidgetEvents {
    public static final Event<EffectTooltip> TOOLTIP = EventFactory.createArrayBacked(EffectTooltip.class, listeners -> (MobEffectInstance effectInstance, List<Component> tooltipLines, TooltipFlag tooltipFlag) -> {
        for (EffectTooltip event : listeners) {
            event.onGatherEffectTooltipLines(effectInstance, tooltipLines, tooltipFlag);
        }
    });

    public interface MouseClicked {

    }

    @FunctionalInterface
    public interface EffectTooltip {

        void onGatherEffectTooltipLines(MobEffectInstance effectInstance, List<Component> tooltipLines, TooltipFlag tooltipFlag);
    }
}
