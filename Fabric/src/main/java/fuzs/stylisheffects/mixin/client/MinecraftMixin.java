package fuzs.stylisheffects.mixin.client;

import fuzs.stylisheffects.api.client.event.ExtraScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Nullable
    public Screen screen;
    // not sure if this is really necessary, but Architectury does it, so might as well
    @Unique
    private final ThreadLocal<Screen> oldScreen = new ThreadLocal<>();

    @Inject(method = "setScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", ordinal = 0))
    public void setScreen$inject$field(@Nullable Screen screen, CallbackInfo callback) {
        // we need to safe the old screen so vanilla can mostly just run through, and we still have all instances we need
        this.oldScreen.set(this.screen);
        // set screen to null so Screen::removed is not called (we will manually call it later if necessary)
        this.screen = null;
    }

    @Inject(method = "setScreen", at = @At(value = "JUMP", opcode = Opcodes.IFNONNULL, ordinal = 0))
    public void setScreen$inject$jump(@Nullable Screen screen, CallbackInfo callback) {
        // return screen to the original value which we had to set to null to prevent Screen::removed from being called
        // this is not necessary in vanilla as everything that happens after this being set to the new screen, but maybe other mixins assume this to still correctly contain the old screen
        this.screen = this.oldScreen.get();
    }

    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;reset()V"), cancellable = true)
    public void setScreen$inject$invoke(@Nullable Screen screen, CallbackInfo callback) {
        Screen oldScreen = this.oldScreen.get();
        // at this point the local screen variable has already been set to Minecraft#screen (meaning the new screen is already set)
        if (this.screen != null) {
            Screen newScreen = ExtraScreenEvents.OPENING.invoker().onScreenOpening(oldScreen, this.screen);
            if (oldScreen == newScreen) {
                // the old screen has been returned, meaning opening the new screen has been cancelled
                // so we return from the method to prevent any setup on the screen
                this.screen = oldScreen;
                callback.cancel();
                return;
            } else {
                // a new screen has been set, this may or may not already be set to Minecraft#screen
                this.screen = newScreen;
            }
        }
        // reaching this point means a new screen has been set successfully, just check now if there was an old screen to begin with
        if (oldScreen != null) {
            ExtraScreenEvents.CLOSING.invoker().onScreenClosing(oldScreen);
            // this is basically the vanilla code we prevented from running earlier by setting Minecraft#screen to null
            oldScreen.removed();
            // this call is skipped earlier by us setting Minecraft#screen to null, so we insert it here as this is where the screen is actually closed now
            net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.remove(oldScreen).invoker().onRemove(oldScreen);
        }
    }

    @ModifyVariable(method = "setScreen", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;reset()V", shift = At.Shift.AFTER), ordinal = 0)
    public Screen setScreen$modify$invoke(@Nullable Screen screen) {
        // make sure the local variable is set to this as it is used for setting up the screen instead of the field
        return this.screen;
    }
}
