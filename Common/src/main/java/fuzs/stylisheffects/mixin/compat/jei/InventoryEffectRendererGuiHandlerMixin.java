package fuzs.stylisheffects.mixin.compat.jei;

import mezz.jei.common.plugins.vanilla.InventoryEffectRendererGuiHandler;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(InventoryEffectRendererGuiHandler.class)
public abstract class InventoryEffectRendererGuiHandlerMixin {

    @Inject(method = "getGuiExtraAreas", at = @At("HEAD"), cancellable = true, remap = false)
    public void getGuiExtraAreas$inject$head(EffectRenderingInventoryScreen<?> containerScreen, CallbackInfoReturnable<List<Rect2i>> callback) {
        // we handle this ourselves by providing a separate jei plugin
        callback.setReturnValue(Collections.emptyList());
    }
}
