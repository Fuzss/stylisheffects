package fuzs.stylisheffects.mixin.compat.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZonesProvider;
import me.shedaniel.rei.plugin.client.exclusionzones.DefaultPotionEffectExclusionZones;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Collections;

@Mixin(DefaultPotionEffectExclusionZones.class)
public abstract class DefaultPotionEffectExclusionZonesMixin implements ExclusionZonesProvider<EffectRenderingInventoryScreen<?>> {

    @Inject(method = "provide", at = @At("HEAD"), cancellable = true, remap = false)
    public void provide$inject$head(EffectRenderingInventoryScreen<?> screen, CallbackInfoReturnable<Collection<Rectangle>> callbackInfo) {
        // we handle this ourselves by providing a separate rei plugin
        callbackInfo.setReturnValue(Collections.emptyList());
    }
}
