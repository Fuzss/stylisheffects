package fuzs.stylisheffects.mixin.client.accessor;

import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EffectRenderingInventoryScreen.class)
public interface EffectRenderingInventoryScreenAccessor {
    @Accessor
    void setDoRenderEffects(boolean doRenderEffects);
}
