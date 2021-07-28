package fuzs.stylisheffects.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.horse.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Mixin(HorseEntity.class)
public abstract class HorseEntityMixin extends AbstractHorseEntity {

    protected HorseEntityMixin(EntityType<? extends AbstractHorseEntity> p_i48563_1_, World p_i48563_2_) {

        super(p_i48563_1_, p_i48563_2_);
    }

    @Redirect(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/horse/HorseEntity;isVehicle()Z"))
    public boolean isFullVehicle(HorseEntity horseEntity) {

        return horseEntity.getPassengers().size() > 1;
    }

}
