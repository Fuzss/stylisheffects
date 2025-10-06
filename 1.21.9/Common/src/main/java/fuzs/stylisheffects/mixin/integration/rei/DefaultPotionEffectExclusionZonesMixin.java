package fuzs.stylisheffects.mixin.integration.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.plugin.client.exclusionzones.DefaultPotionEffectExclusionZones;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Collections;

@Pseudo
@Mixin(DefaultPotionEffectExclusionZones.class)
abstract class DefaultPotionEffectExclusionZonesMixin {

    @Inject(method = "provide", at = @At("HEAD"), cancellable = true, remap = false)
    public void provide(@Coerce Object o, CallbackInfoReturnable<Collection<Rectangle>> callback) {
        // we handle this ourselves by providing a separate rei plugin
        callback.setReturnValue(Collections.emptyList());
    }
}
