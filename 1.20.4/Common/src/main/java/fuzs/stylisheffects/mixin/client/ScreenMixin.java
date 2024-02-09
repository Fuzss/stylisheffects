package fuzs.stylisheffects.mixin.client;

import fuzs.stylisheffects.client.handler.EffectScreenHandlerImpl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
abstract class ScreenMixin extends AbstractContainerEventHandler {

    @Inject(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
                    shift = At.Shift.AFTER
            )
    )
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {
        // TODO remove when the implementation is fixed
        if (AbstractContainerScreen.class.isInstance(this)) {
            EffectScreenHandlerImpl.INSTANCE.onDrawBackground(AbstractContainerScreen.class.cast(this),
                    guiGraphics,
                    mouseX,
                    mouseY
            );
        }
    }
}
