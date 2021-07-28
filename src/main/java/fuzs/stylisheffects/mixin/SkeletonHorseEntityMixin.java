package fuzs.stylisheffects.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.passive.horse.SkeletonHorseEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Mixin(SkeletonHorseEntity.class)
public abstract class SkeletonHorseEntityMixin extends AbstractHorseEntity {

    protected SkeletonHorseEntityMixin(EntityType<? extends SkeletonHorseEntity> p_i48563_1_, World p_i48563_2_) {

        super(p_i48563_1_, p_i48563_2_);
    }

    @Redirect(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/horse/SkeletonHorseEntity;isVehicle()Z"))
    public boolean isFullVehicle(SkeletonHorseEntity horseEntity) {

        return horseEntity.getPassengers().size() > 1;
    }

}
