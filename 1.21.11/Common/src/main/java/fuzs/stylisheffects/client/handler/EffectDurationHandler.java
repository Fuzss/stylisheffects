package fuzs.stylisheffects.client.handler;

import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.config.ClientConfig;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The map holds the duration value for a mob effect when it first appeared on the player, which we use as the starting
 * duration value.
 * <p>
 * Only newly added effects are added properly into the map with a positive starting duration value, see
 * {@link #isNewlyAddedMobEffect(MobEffectInstance, Player)}.
 * <p>
 * All other existing effects are added with a negative starting duration for keeping track if they are ever overridden
 * with a higher starting duration.
 */
public class EffectDurationHandler {
    private static final Object2IntMap<MobEffectKey> MOB_EFFECT_INITIAL_DURATIONS = new Object2IntOpenHashMap<>();

    public static float getMobEffectDurationScale(MobEffectInstance mobEffect, boolean unknownStartingDuration) {
        float startingDuration = MOB_EFFECT_INITIAL_DURATIONS.getInt(new MobEffectKey(mobEffect));
        // Do not divide by zero further below.
        if (startingDuration != 0.0F) {
            // Negative starting values will usually just be ignored, as the return value is only used when greater than zero.
            float durationScale =
                    mobEffect.getDuration() / (unknownStartingDuration ? Math.abs(startingDuration) : startingDuration);
            return Math.clamp(durationScale, 0.0F, 1.0F);
        } else {
            return -1.0F;
        }
    }

    public static void onStartClientTick(Minecraft minecraft) {
        if (!StylishEffects.CONFIG.get(ClientConfig.class).guiWidgets.effectBar.effectBar && !StylishEffects.CONFIG.get(
                ClientConfig.class).inventoryWidgets.effectBar.effectBar) {
            return;
        }

        if (minecraft.player != null) {
            Set<MobEffectKey> mobEffectKeys = minecraft.player.getActiveEffects()
                    .stream()
                    .map(MobEffectKey::new)
                    .collect(Collectors.toSet());
            MOB_EFFECT_INITIAL_DURATIONS.keySet().removeIf(Predicate.not(mobEffectKeys::contains));
            for (MobEffectInstance mobEffect : minecraft.player.getActiveEffects()) {
                if (!mobEffect.isInfiniteDuration() && mobEffect.getDuration() > 0) {
                    MobEffectKey mobEffectKey = new MobEffectKey(mobEffect);
                    // If the effect is already in the map with a lower starting duration, we allow overriding it.
                    if (isNewlyAddedMobEffect(mobEffect, minecraft.player)
                            || Math.abs(MOB_EFFECT_INITIAL_DURATIONS.getOrDefault(mobEffectKey, Integer.MAX_VALUE))
                            < mobEffect.getDuration()) {
                        MOB_EFFECT_INITIAL_DURATIONS.put(mobEffectKey, mobEffect.getDuration());
                    } else if (MOB_EFFECT_INITIAL_DURATIONS.getInt(mobEffectKey) <= 0) {
                        // Allow ticking down running effects, so they can be superseded by new effects with longer durations (since that is allowed on the server-side).
                        MOB_EFFECT_INITIAL_DURATIONS.put(mobEffectKey, -mobEffect.getDuration());
                    }
                }
            }
        } else {
            MOB_EFFECT_INITIAL_DURATIONS.clear();
        }
    }

    /**
     * We can distinguish between newly added and existing effects as existing effects have
     * {@link MobEffectInstance#skipBlending()} called when added on the client. We can detect that by checking the
     * values returned from {@link MobEffectInstance#getBlendFactor(LivingEntity, float)}.
     */
    private static boolean isNewlyAddedMobEffect(MobEffectInstance mobEffect, Player player) {
        float blendFactor = mobEffect.getBlendFactor(player, 0.5F);
        return blendFactor != 0.0F && blendFactor != 1.0F;
    }

    private record MobEffectKey(Holder<MobEffect> effect,
                                int amplifier,
                                boolean ambient,
                                boolean visible,
                                boolean showIcon) {

        public MobEffectKey(MobEffectInstance mobEffect) {
            this(mobEffect.getEffect(),
                    mobEffect.getAmplifier(),
                    mobEffect.isAmbient(),
                    mobEffect.isVisible(),
                    mobEffect.showIcon());
        }
    }
}
