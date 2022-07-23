package fuzs.stylisheffects.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.stylisheffects.api.client.event.ExtraScreenEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectRenderingInventoryScreen.class)
public abstract class EffectRenderingInventoryScreenMixin<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    @Unique
    private boolean cancelEffectRendering;

    public EffectRenderingInventoryScreenMixin(T p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
    }

    @ModifyVariable(method = "renderEffects", at = @At("STORE"), ordinal = 0)
    private boolean renderEffects$modify$effectSize(boolean fullSize) {
        int rightPos = this.leftPos + this.imageWidth + 2;
        int availableSpace = this.width - rightPos;
        ExtraScreenEvents.MobEffectsRenderMode mode = ExtraScreenEvents.INVENTORY_MOB_EFFECTS.invoker().onRenderInventoryMobEffects(this, availableSpace, !fullSize);
        this.cancelEffectRendering = mode == ExtraScreenEvents.MobEffectsRenderMode.NONE;
        return mode != ExtraScreenEvents.MobEffectsRenderMode.COMPACT;
    }

    @Inject(method = "renderEffects", at = @At(value = "INVOKE", target = "Ljava/util/Collection;size()I", ordinal = 0), cancellable = true)
    private void renderEffects$invoke$size(PoseStack poseStack, int mouseX, int mouseY, CallbackInfo callback) {
        if (this.cancelEffectRendering) callback.cancel();
    }
}
