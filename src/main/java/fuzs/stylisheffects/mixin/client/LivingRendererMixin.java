package fuzs.stylisheffects.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@SuppressWarnings("unused")
@Mixin(LivingRenderer.class)
public abstract class LivingRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements IEntityRenderer<T, M> {

    protected LivingRendererMixin(EntityRendererManager p_i46179_1_) {

        super(p_i46179_1_);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingRenderer;isBodyVisible(Lnet/minecraft/entity/LivingEntity;)Z"))
    protected boolean isBodyVisible(LivingRenderer<?, ?> livingRenderer, T entityIn) {

        if (entityIn.isInvisible()) {

            return false;
        }

        if (entityIn instanceof AbstractClientPlayerEntity) {

            Minecraft mc = Minecraft.getInstance();
            if (entityIn.isPassenger() && mc.options.getCameraType().isFirstPerson()) {

                List<Entity> passengers = entityIn.getVehicle().getPassengers();
                return passengers.size() < 2 || passengers.get(0) != entityIn || passengers.get(1) != mc.player;
            }
        }

        return true;
    }

}
