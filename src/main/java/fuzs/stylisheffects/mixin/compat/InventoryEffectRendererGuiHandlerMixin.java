package fuzs.stylisheffects.mixin.compat;

import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.renderer.Rectangle2d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(targets = "mezz.jei.plugins.vanilla.InventoryEffectRendererGuiHandler")
public abstract class InventoryEffectRendererGuiHandlerMixin {

    @Inject(method = "getGuiExtraAreas", at = @At("HEAD"), cancellable = true, remap = false)
    public void getGuiExtraAreas(DisplayEffectsScreen<?> containerScreen, CallbackInfoReturnable<List<Rectangle2d>> callbackInfo) {
        // we handle this ourselves by providing a separate jei plugin
        callbackInfo.setReturnValue(Collections.emptyList());
    }
}
