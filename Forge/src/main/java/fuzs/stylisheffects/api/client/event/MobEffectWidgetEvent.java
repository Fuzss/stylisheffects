package fuzs.stylisheffects.api.client.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class MobEffectWidgetEvent extends Event {
    private final MobEffectInstance effectInstance;

    public MobEffectWidgetEvent(MobEffectInstance effectInstance) {
        this.effectInstance = effectInstance;
    }

    public MobEffectInstance getEffectInstance() {
        return this.effectInstance;
    }

    public static class MouseClicked extends MobEffectWidgetEvent {

        public MouseClicked(MobEffectInstance effectInstance) {
            super(effectInstance);
        }
    }

    public static class EffectTooltip extends MobEffectWidgetEvent {
        private final List<Component> tooltipLines;
        private final TooltipFlag tooltipFlag;

        public EffectTooltip(MobEffectInstance effectInstance, List<Component> tooltipLines, TooltipFlag tooltipFlag) {
            super(effectInstance);
            this.tooltipLines = tooltipLines;
            this.tooltipFlag = tooltipFlag;
        }

        public List<Component> getTooltipLines() {
            return this.tooltipLines;
        }

        public TooltipFlag getTooltipFlag() {
            return this.tooltipFlag;
        }
    }
}
