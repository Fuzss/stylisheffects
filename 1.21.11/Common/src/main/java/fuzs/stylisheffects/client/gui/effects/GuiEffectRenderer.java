package fuzs.stylisheffects.client.gui.effects;

import fuzs.stylisheffects.client.handler.EffectRendererEnvironment;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class GuiEffectRenderer extends CompactEffectRenderer {

    public GuiEffectRenderer(EffectRendererEnvironment environment) {
        super(environment);
    }

    @Override
    public int getRows() {
        if (this.widgetConfig().separateEffects) {
            final int beneficialEffects = this.countBeneficialEffects(this.activeEffects);
            return this.splitByColumns(beneficialEffects) + this.splitByColumns(
                    this.activeEffects.size() - beneficialEffects);
        }
        return super.getRows();
    }

    @Override
    protected int getTopOffset() {
        return 1;
    }

    @Override
    protected int getSpriteOffsetY(boolean withoutDuration) {
        // draw icon a bit further down when no duration is displayed to trim empty space
        return withoutDuration ? 3 : 2;
    }

    protected int getAmplifierOffsetX() {
        return 3;
    }

    protected int getAmplifierOffsetY() {
        return 2;
    }

    @Override
    public List<Pair<MobEffectInstance, int[]>> getEffectPositions(List<MobEffectInstance> activeEffects) {
        int beneficialRows = this.splitByColumns(this.countBeneficialEffects(activeEffects));
        int beneficialCounter = 0, harmfulCounter = 0;
        List<Pair<MobEffectInstance, int[]>> effectToPos = new ArrayList<>();
        for (MobEffectInstance effect : activeEffects) {
            int counter;
            boolean beneficial = !this.widgetConfig().separateEffects || effect.getEffect().value().isBeneficial();
            if (beneficial) {
                counter = beneficialCounter++;
            } else {
                counter = harmfulCounter++;
            }

            int posX = counter % this.getMaxClampedColumns();
            int posY = counter / this.getMaxClampedColumns();
            if (!beneficial) {
                posY += beneficialRows;
            }

            effectToPos.add(Pair.of(effect, this.coordsToEffectPosition(posX, posY)));
        }

        // sorting is need for rendering in condensed mode (when too many effects are active and the widgets overlap), so that the overlap is in the right order
        if (this.widgetConfig().separateEffects) {
            effectToPos.sort(Comparator.<Pair<MobEffectInstance, int[]>, Boolean>comparing(o -> o.getLeft()
                    .getEffect()
                    .value()
                    .isBeneficial()).reversed());
        }

        if (beneficialCounter + harmfulCounter != activeEffects.size()) {
            throw new RuntimeException("Effects amount mismatch");
        }

        return effectToPos;
    }

    private int countBeneficialEffects(List<MobEffectInstance> activeEffects) {
        return (int) activeEffects.stream()
                .map(MobEffectInstance::getEffect)
                .map(Holder::value)
                .filter(MobEffect::isBeneficial)
                .count();
    }

    @Override
    protected abstract ClientConfig.GuiWidgetConfig widgetConfig();
}
