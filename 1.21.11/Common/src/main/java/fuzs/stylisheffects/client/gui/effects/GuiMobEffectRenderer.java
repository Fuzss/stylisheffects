package fuzs.stylisheffects.client.gui.effects;

import com.mojang.datafixers.util.Either;
import fuzs.stylisheffects.config.WidgetType;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class GuiMobEffectRenderer extends AbstractMobEffectRenderer {
    protected static final Identifier EFFECT_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("hud/effect_background");
    protected static final Identifier EFFECT_BACKGROUND_AMBIENT_SPRITE = Identifier.withDefaultNamespace(
            "hud/effect_background_ambient");

    public GuiMobEffectRenderer(Either<Gui, AbstractContainerScreen<?>> environment) {
        super(environment);
    }

    @Override
    public int getWidth() {
        return 24;
    }

    @Override
    public int getHeight() {
        return 24;
    }

    @Override
    public int getRows(List<MobEffectInstance> mobEffects) {
        if (this.config.separateEffects()) {
            int beneficialEffectsAmount = this.getBeneficialEffectsAmount(mobEffects);
            return this.splitByColumns(beneficialEffectsAmount) + this.splitByColumns(
                    mobEffects.size() - beneficialEffectsAmount);
        } else {
            return super.getRows(mobEffects);
        }
    }

    @Override
    protected int getTopOffset() {
        return 1;
    }

    @Override
    protected int getSpriteOffsetY(boolean withoutDuration) {
        return withoutDuration ? 3 : 2;
    }

    @Override
    protected int getDurationOffsetY() {
        return this.getHeight() - 10;
    }

    @Override
    protected int getAmplifierOffsetX() {
        return 3;
    }

    @Override
    protected int getAmplifierOffsetY() {
        return 2;
    }

    @Override
    protected int getBorderSize() {
        return 3;
    }

    @Override
    public List<Pair<MobEffectInstance, Vector2ic>> getEffectPositions(List<MobEffectInstance> mobEffects) {
        int beneficialRows = this.splitByColumns(this.getBeneficialEffectsAmount(mobEffects));
        int beneficialCounter = 0, harmfulCounter = 0;
        List<Pair<MobEffectInstance, Vector2ic>> mobEffectPositions = new ArrayList<>();
        for (MobEffectInstance effect : mobEffects) {
            int counter;
            boolean isBeneficial = !this.config.separateEffects() || effect.getEffect().value().isBeneficial();
            if (isBeneficial) {
                counter = beneficialCounter++;
            } else {
                counter = harmfulCounter++;
            }

            int posX = counter % this.getMaxClampedColumns();
            int posY = counter / this.getMaxClampedColumns();
            if (!isBeneficial) {
                posY += beneficialRows;
            }

            mobEffectPositions.add(Pair.of(effect, this.translateMobEffectWidgetPosition(posX, posY, mobEffects)));
        }

        // sorting is need for rendering in condensed mode (when too many effects are active and the widgets overlap), so that the overlap is in the right order
        if (this.config.separateEffects()) {
            mobEffectPositions.sort(Comparator.<Pair<MobEffectInstance, Vector2ic>, Boolean>comparing((Pair<MobEffectInstance, Vector2ic> pair) -> pair.getLeft()
                    .getEffect()
                    .value()
                    .isBeneficial()).reversed());
        }

        if (beneficialCounter + harmfulCounter != mobEffects.size()) {
            throw new RuntimeException("Effects amount mismatch");
        }

        return mobEffectPositions;
    }

    private int getBeneficialEffectsAmount(List<MobEffectInstance> mobEffects) {
        return (int) mobEffects.stream()
                .map(MobEffectInstance::getEffect)
                .map(Holder::value)
                .filter(MobEffect::isBeneficial)
                .count();
    }

    @Override
    protected Identifier getEffectBackgroundSprite(boolean isAmbient) {
        return isAmbient ? EFFECT_BACKGROUND_AMBIENT_SPRITE : EFFECT_BACKGROUND_SPRITE;
    }

    public static class Small extends GuiMobEffectRenderer {

        public Small(Either<Gui, AbstractContainerScreen<?>> environment) {
            super(environment);
        }
    }

    public static class Large extends GuiMobEffectRenderer {

        public Large(Either<Gui, AbstractContainerScreen<?>> environment) {
            super(environment);
        }

        @Override
        public int getWidth() {
            return 30;
        }

        @Override
        public WidgetType.@Nullable Factory getFallbackRenderer() {
            return Small::new;
        }
    }
}
