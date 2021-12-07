package fuzs.stylisheffects.mixin.client.accessor;

import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Container.class)
public interface ContainerAccessor {
    @Accessor
    ContainerType<?> getMenuType();
}
