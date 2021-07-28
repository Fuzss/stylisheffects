package fuzs.stylisheffects.mixin;

import fuzs.stylisheffects.StylishEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IEquipable;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(AbstractHorseEntity.class)
public abstract class AbstractHorseEntityMixin extends AnimalEntity implements IInventoryChangedListener, IJumpingMount, IEquipable {

    protected AbstractHorseEntityMixin(EntityType<? extends AbstractHorseEntity> p_i48568_1_, World p_i48568_2_) {

        super(p_i48568_1_, p_i48568_2_);
    }

    @Override
    protected boolean canAddPassenger(Entity p_184219_1_) {

        return this.getPassengers().size() < 2;
    }

    @Inject(method = "positionRider", at = @At("TAIL"))
    public void positionRider(Entity rider, CallbackInfo callbackInfo) {

        positionRider(this, rider, 0.2F, -0.4F);
    }

    private static void positionRider(Entity vehicle, Entity rider, float rider0XOffset, float rider1XOffset) {

        if (vehicle.getPassengers().size() > 1) {

            float riderXOffset = vehicle.getPassengers().indexOf(rider) == 0 ? rider0XOffset : rider1XOffset;
            if (rider instanceof AnimalEntity) {

                riderXOffset = riderXOffset + 0.2F;
                int animalRandomRotation = rider.getId() % 2 == 0 ? 90 : 270;
                rider.setYBodyRot(((AnimalEntity) rider).yBodyRot + (float) animalRandomRotation);
                rider.setYHeadRot(rider.getYHeadRot() + (float) animalRandomRotation);
            }

            Vector3d vector3d = (new Vector3d((double)riderXOffset, 0.0D, 0.0D)).yRot(-vehicle.yRot * ((float) Math.PI / 180F) - ((float) Math.PI / 2F));
            rider.setPos(rider.getX() + vector3d.x, rider.getY(), rider.getZ() + vector3d.z);
            clampRotation(vehicle, rider);
        }
    }

    private static void clampRotation(Entity vehicle, Entity rider) {

        rider.setYBodyRot(vehicle.yRot);
        float rotDeltaDegrees = MathHelper.wrapDegrees(rider.yRot - vehicle.yRot);
        float wrappedRotDegrees = MathHelper.clamp(rotDeltaDegrees, -105.0F, 105.0F);
        rider.yRotO += wrappedRotDegrees - rotDeltaDegrees;
        rider.yRot += wrappedRotDegrees - rotDeltaDegrees;
        rider.setYHeadRot(rider.yRot);
    }

}
