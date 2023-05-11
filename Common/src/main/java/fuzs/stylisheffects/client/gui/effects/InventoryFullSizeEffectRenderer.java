package fuzs.stylisheffects.client.gui.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.api.client.MobEffectWidgetContext;
import fuzs.stylisheffects.client.core.ClientAbstractions;
import fuzs.stylisheffects.client.handler.EffectRendererEnvironment;
import fuzs.stylisheffects.client.util.ColorUtil;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Optional;

public class InventoryFullSizeEffectRenderer extends AbstractEffectRenderer {

    public InventoryFullSizeEffectRenderer(EffectRendererEnvironment environment) {
        super(environment);
    }

    @Override
    public int getWidth() {
        return 120;
    }

    @Override
    public int getHeight() {
        return 32;
    }

    @Override
    protected int getBackgroundTextureX() {
        return 0;
    }

    @Override
    protected int getBackgroundTextureY() {
        return 0;
    }

    @Override
    protected int getSpriteOffsetX() {
        return 7;
    }

    @Override
    protected int getSpriteOffsetY(boolean withoutDuration) {
        return 7;
    }

    @Override
    public MobEffectWidgetContext.Renderer getEffectRenderer() {
        return MobEffectWidgetContext.Renderer.INVENTORY_FULL_SIZE;
    }

    @Override
    public EffectRendererEnvironment.Factory getFallbackRenderer() {
        return InventoryCompactEffectRenderer::new;
    }

    @Override
    protected ClientConfig.InventoryFullSizeWidgetConfig widgetConfig() {
        return StylishEffects.CONFIG.get(ClientConfig.class).inventoryFullSizeWidget();
    }

    @Override
    protected void drawEffectText(PoseStack poseStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectinstance) {
        if (!(this.screen instanceof EffectRenderingInventoryScreen<?> effectInventoryScreen) || !ClientAbstractions.INSTANCE.renderInventoryText(effectinstance, effectInventoryScreen, poseStack, posX, posY, effectInventoryScreen.getBlitOffset())) {
            MutableComponent component = this.getEffectDisplayName(effectinstance);
            int nameColor = ColorUtil.getEffectColor(this.widgetConfig().nameColor, effectinstance);
            minecraft.font.drawShadow(poseStack, component, posX + 12 + 18, posY + 7 + (!this.widgetConfig().ambientDuration && effectinstance.isAmbient() ? 4 : 0), (int) (this.rendererConfig().widgetAlpha * 255.0F) << 24 | nameColor);
            if (this.widgetConfig().ambientDuration || !effectinstance.isAmbient()) {
                this.getEffectDuration(effectinstance).ifPresent(duration -> {
                    int durationColor = ColorUtil.getEffectColor(this.widgetConfig().durationColor, effectinstance);
                    minecraft.font.drawShadow(poseStack, duration, posX + 12 + 18, posY + 7 + 11, (int) (this.rendererConfig().widgetAlpha * 255.0F) << 24 | durationColor);
                });
            }
        }
    }

    @Override
    protected Optional<Component> getEffectDuration(MobEffectInstance effectInstance) {
        return Optional.of(Component.literal(formatDuration(effectInstance)));
    }
}
