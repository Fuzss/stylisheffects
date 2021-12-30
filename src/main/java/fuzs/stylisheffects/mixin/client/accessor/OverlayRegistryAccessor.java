package fuzs.stylisheffects.mixin.client.accessor;

import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(OverlayRegistry.class)
public interface OverlayRegistryAccessor {
    @Accessor(remap = false)
    static List<OverlayRegistry.OverlayEntry> getOverlaysOrdered() {
        throw new IllegalStateException();
    }

    @Accessor(remap = false)
    static Map<IIngameOverlay, OverlayRegistry.OverlayEntry> getOverlays() {
        throw new IllegalStateException();
    }
}
