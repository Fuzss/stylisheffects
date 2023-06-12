package fuzs.stylisheffects.client.gui.effects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;

public interface EffectWidget {

    int getWidth();

    int getHeight();

    void renderWidget(GuiGraphics guiGraphics, int posX, int posY, Minecraft minecraft, MobEffectInstance effectInstance);
}
