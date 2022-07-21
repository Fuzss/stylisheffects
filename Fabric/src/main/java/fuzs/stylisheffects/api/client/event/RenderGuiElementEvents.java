package fuzs.stylisheffects.api.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * events for managing all the rendering done in {@link net.minecraft.client.gui.Gui}
 * this is modelled after Forge's system for handling individual components in the gui before Minecraft 1.17
 *
 * WARNING: this only currently is implemented for mob effect icons, as that's all this mod needs right now
 */
public class RenderGuiElementEvents {
    public static final Event<Before> BEFORE = EventFactory.createArrayBacked(Before.class, listeners -> (ElementType elementType, PoseStack poseStack, int screenWidth, int screenHeight) -> {
        for (Before event : listeners) {
            if (!event.onBeforeRenderGameOverlay(elementType, poseStack, screenWidth, screenHeight)) {
                return false;
            }
        }
        return true;
    });
    public static final Event<After> AFTER = EventFactory.createArrayBacked(After.class, listeners -> (ElementType elementType, PoseStack poseStack, int screenWidth, int screenHeight) -> {
        for (After event : listeners) {
            event.onAfterRenderGameOverlay(elementType, poseStack, screenWidth, screenHeight);
        }
    });

    @FunctionalInterface
    public interface Before {

        /**
         * called before a gui element is rendered, allows for cancelling rendering
         *
         * @param elementType   type of element being rendered
         * @param poseStack     the pose stack
         * @param screenWidth   width of the window's screen
         * @param screenHeight  height of the window's screen
         * @return              is this element permitted to render
         */
        boolean onBeforeRenderGameOverlay(ElementType elementType, PoseStack poseStack, int screenWidth, int screenHeight);
    }

    @FunctionalInterface
    public interface After {

        /**
         * called after a gui element is rendered
         *
         * @param elementType   type of element being rendered
         * @param poseStack     the pose stack
         * @param screenWidth   width of the window's screen
         * @param screenHeight  height of the window's screen
         */
        void onAfterRenderGameOverlay(ElementType elementType, PoseStack poseStack, int screenWidth, int screenHeight);
    }

    /**
     * types of elements that show up on the gui
     *
     * WARNING: this is highly incomplete right now, it only contains what is actually used in the mod right now
     * it will be expanded when necessary (maybe when a proper library for porting Forge events is being made?)
     */
    public enum ElementType {
        /**
         * the mob effect icons that show up in the top right corner of the gui
         */
        MOB_EFFECT_ICONS
    }
}
