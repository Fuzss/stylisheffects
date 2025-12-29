package fuzs.stylisheffects.config;

import com.mojang.datafixers.util.Either;
import fuzs.stylisheffects.client.gui.effects.AbstractMobEffectRenderer;
import fuzs.stylisheffects.client.gui.effects.GuiMobEffectRenderer;
import fuzs.stylisheffects.client.gui.effects.InventoryMobEffectRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.function.Function;

/**
 * type of renderer that is used
 */
public enum WidgetType {
    /**
     * nothing is rendered
     */
    NONE((Either<Gui, AbstractContainerScreen<?>> either) -> {
        throw new IllegalStateException("Cannot create effect renderer");
    }),
    /**
     * vanilla's native effect rendering on the in-game gui
     */
    GUI_SQUARE(GuiMobEffectRenderer.Small::new),
    /**
     * our default rendering, similar to {@link #GUI_SQUARE}, just slightly larger with more information
     */
    GUI_RECTANGLE(GuiMobEffectRenderer.Large::new),
    /**
     * vanilla's compact inventory widgets
     */
    INVENTORY_SQUARE(InventoryMobEffectRenderer.Small::new),
    /**
     * vanilla's full sized inventory widgets
     */
    INVENTORY_RECTANGLE(InventoryMobEffectRenderer.Large::new);

    public final Factory factory;

    WidgetType(Factory factory) {
        this.factory = factory;
    }

    @FunctionalInterface
    public interface Factory extends Function<Either<Gui, AbstractContainerScreen<?>>, AbstractMobEffectRenderer> {

    }
}
