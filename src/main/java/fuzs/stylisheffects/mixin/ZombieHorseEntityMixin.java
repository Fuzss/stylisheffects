package fuzs.stylisheffects.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.passive.horse.ZombieHorseEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Mixin(ZombieHorseEntity.class)
public abstract class ZombieHorseEntityMixin extends AbstractHorseEntity {

    protected ZombieHorseEntityMixin(EntityType<? extends ZombieHorseEntity> p_i48563_1_, World p_i48563_2_) {

        super(p_i48563_1_, p_i48563_2_);
    }

    @Redirect(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/horse/ZombieHorseEntity;isVehicle()Z"))
    public boolean isFullVehicle(ZombieHorseEntity horseEntity) {

        return horseEntity.getPassengers().size() > 1;
    }

}
