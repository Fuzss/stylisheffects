package fuzs.stylisheffects.client.gui.effects;

import fuzs.stylisheffects.client.handler.EffectRendererEnvironment;
import fuzs.stylisheffects.services.ClientAbstractions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectInstance;

public abstract class InventoryMobEffectRenderer extends AbstractMobEffectRenderer {

    public InventoryMobEffectRenderer(EffectRendererEnvironment environment) {
        super(environment);
    }

    @Override
    public int getWidth() {
        return 32;
    }

    @Override
    public int getHeight() {
        return 32;
    }

    @Override
    protected int getSpriteOffsetY(boolean withoutDuration) {
        return 7;
    }

    @Override
    protected int getBorderSize() {
        return 4;
    }

    @Override
    protected int getAmplifierOffsetX() {
        return 3;
    }

    @Override
    protected int getAmplifierOffsetY() {
        return 3;
    }

    @Override
    protected int getDurationOffsetY() {
        return this.getHeight() - 13;
    }

    public static class Small extends InventoryMobEffectRenderer {

        public Small(EffectRendererEnvironment environment) {
            super(environment);
        }

        @Override
        protected int getSpriteOffsetY(boolean withoutDuration) {
            return withoutDuration ? super.getSpriteOffsetY(withoutDuration) : 6;
        }

        @Override
        public EffectRendererEnvironment.Factory getFallbackRenderer() {
            return GuiMobEffectRenderer.Large::new;
        }
    }

    public static class Large extends InventoryMobEffectRenderer {

        public Large(EffectRendererEnvironment environment) {
            super(environment);
        }

        @Override
        public int getWidth() {
            return 120;
        }

        @Override
        protected int getSpriteOffsetX() {
            return 7;
        }

        @Override
        public EffectRendererEnvironment.Factory getFallbackRenderer() {
            return Small::new;
        }

        @Override
        protected void renderLabels(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance mobEffect) {
            if (!(this.screen instanceof AbstractContainerScreen<?> screen) || !screen.showsActiveEffects()
                    || !ClientAbstractions.INSTANCE.renderInventoryText(mobEffect,
                    screen,
                    guiGraphics,
                    posX,
                    posY,
                    0)) {
                Component displayNameComponent = this.getEffectDisplayName(mobEffect, false);
                Style displayNameStyle = this.widgetConfig().nameColor.getMobEffectStyle(mobEffect);
                int minX = posX + 12 + 18;
                int maxX = posX + this.getWidth() - 7;
                Component durationComponent = this.getEffectDuration(mobEffect, maxX - minX);
                int minY = posY + 6 + (durationComponent == null ? 4 : 0);
                int maxY = minY + Minecraft.getInstance().font.lineHeight;
                ActiveTextCollector activeTextCollector = this.activeTextCollector(guiGraphics);
                if (Minecraft.getInstance().font.width(displayNameComponent) > maxX - minX) {
                    activeTextCollector.acceptScrollingWithDefaultCenter(ComponentUtils.mergeStyles(displayNameComponent,
                            displayNameStyle), minX, maxX, minY, maxY);
                } else {
                    // The text renderer defaults to centering in the middle when the text fits; we do not want that.
                    activeTextCollector.accept(minX,
                            minY + 1,
                            ComponentUtils.mergeStyles(displayNameComponent, displayNameStyle));
                }

                if (durationComponent != null) {
                    Style durationStyle = this.widgetConfig().durationColor.getMobEffectStyle(mobEffect);
                    activeTextCollector.accept(minX,
                            minY + 1 + 11,
                            ComponentUtils.mergeStyles(durationComponent, durationStyle));
                }
            }
        }
    }
}
