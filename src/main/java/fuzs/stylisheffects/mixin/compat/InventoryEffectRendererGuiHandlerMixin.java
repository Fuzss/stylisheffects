package fuzs.stylisheffects.mixin.compat;

import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(targets = "mezz.jei.plugins.vanilla.InventoryEffectRendererGuiHandler")
public abstract class InventoryEffectRendererGuiHandlerMixin {
    @Inject(method = "getGuiExtraAreas", at = @At("HEAD"), cancellable = true, remap = false)
    public void getGuiExtraAreas(EffectRenderingInventoryScreen<?> containerScreen, CallbackInfoReturnable<List<Rect2i>> callbackInfo) {
        // we handle this ourselves by providing a separate jei plugin
        callbackInfo.setReturnValue(Collections.emptyList());
    }
}
