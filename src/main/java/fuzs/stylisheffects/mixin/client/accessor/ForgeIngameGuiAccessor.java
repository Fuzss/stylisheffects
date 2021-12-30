package fuzs.stylisheffects.mixin.client.accessor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ForgeIngameGui.class)
public interface ForgeIngameGuiAccessor {
    @Accessor(value = "FOOD_LEVEL_ELEMENT", remap = false)
    @Mutable
    static void setFoodLevelElement(IIngameOverlay foodLevelElement) {
        throw new IllegalStateException();
    }

    @Accessor(value = "JUMP_BAR_ELEMENT", remap = false)
    @Mutable
    static void setJumpBarElement(IIngameOverlay jumpBarElement) {
        throw new IllegalStateException();
    }

    @Accessor(value = "EXPERIENCE_BAR_ELEMENT", remap = false)
    @Mutable
    static void setExperienceBarElement(IIngameOverlay experienceBarElement) {
        throw new IllegalStateException();
    }

    @Invoker(remap = false)
    void callRenderExperience(int x, PoseStack poseStack);
}
