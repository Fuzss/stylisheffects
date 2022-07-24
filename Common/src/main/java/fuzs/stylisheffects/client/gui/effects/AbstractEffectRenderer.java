package fuzs.stylisheffects.client.gui.effects;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.api.client.MobEffectWidgetContext;
import fuzs.stylisheffects.client.core.ClientModServices;
import fuzs.stylisheffects.client.handler.EffectRendererEnvironment;
import fuzs.stylisheffects.client.util.ColorUtil;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.TooltipFlag;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractEffectRenderer implements EffectWidget, RenderAreasProvider {
    public static final double DEFAULT_WIDGET_SCALE = 4.0;
    protected static final ResourceLocation EFFECT_BACKGROUND = new ResourceLocation(StylishEffects.MOD_ID,"textures/gui/mob_effect_background.png");

    private final EffectRendererEnvironment environment;
    protected GuiComponent screen;
    private int availableWidth;
    private int availableHeight;
    private int startX;
    private int startY;
    private MobEffectWidgetContext.ScreenSide screenSide;
    protected List<MobEffectInstance> activeEffects;

    protected AbstractEffectRenderer(EffectRendererEnvironment environment) {
        this.environment = environment;
    }

    public void setScreenDimensions(GuiComponent screen, int availableWidth, int availableHeight, int startX, int startY, MobEffectWidgetContext.ScreenSide screenSide) {
        this.screen = screen;
        this.availableWidth = availableWidth;
        this.availableHeight = availableHeight;
        this.startX = startX;
        this.startY = startY;
        this.screenSide = screenSide;
        switch (this.environment) {
            case GUI -> {
                this.screenSide = this.screenSide.inverse();
                this.availableWidth -= ((ClientConfig.GuiRendererConfig) this.rendererConfig()).offsetX;
                this.availableHeight -= ((ClientConfig.GuiRendererConfig) this.rendererConfig()).offsetY;
                this.startX += (this.screenSide.right() ? 1 : -1) * ((ClientConfig.GuiRendererConfig) this.rendererConfig()).offsetX;
                this.startY += ((ClientConfig.GuiRendererConfig) this.rendererConfig()).offsetY;
            }
            case INVENTORY -> this.availableWidth -= ((ClientConfig.InventoryRendererConfig) this.rendererConfig()).screenBorderDistance;
        }
    }

    public final void setActiveEffects(Collection<MobEffectInstance> activeEffects) {
        if (activeEffects.isEmpty()) {
            this.activeEffects = null;
            return;
        }
        this.activeEffects = activeEffects.stream()
                .filter(e -> e.getDuration() > 0)
                .filter(e -> !this.rendererConfig().respectHideParticles || e.showIcon())
                .filter(e -> ClientModServices.ABSTRACTIONS.isMobEffectVisibleIn(this.environment, e))
                .sorted()
                .collect(Collectors.toList());
    }

    public final boolean isActive() {
        return this.activeEffects != null && !this.activeEffects.isEmpty();
    }

    public final boolean isValid() {
        return !this.rendererConfig().allowFallback || this.getMaxRows() > 0 && this.getMaxColumns() > 0;
    }

    public final double getWidgetScale() {
        return this.rendererConfig().scale / DEFAULT_WIDGET_SCALE;
    }

    public final int getScaledWidth() {
        return (int) (this.getWidth() * this.getWidgetScale());
    }

    public final int getScaledHeight() {
        return (int) (this.getHeight() * this.getWidgetScale());
    }

    public abstract MobEffectWidgetContext.Renderer getEffectRenderer();

    protected abstract int getBackgroundTextureX();

    protected abstract int getBackgroundTextureY();

    protected abstract int getSpriteOffsetX();

    protected abstract int getSpriteOffsetY(boolean withoutDuration);

    protected int getDurationOffsetX() {
        return this.getWidth() / 2;
    }

    protected int getDurationOffsetY() {
        return this.getHeight() - 10;
    }

    @Nullable
    public abstract EffectRendererEnvironment.Factory getFallbackRenderer();

    @Override
    public List<Rect2i> getRenderAreas() {
        return this.getEffectPositions(this.activeEffects).stream()
                .map(Pair::getValue)
                .map(pos -> new Rect2i(pos[0], pos[1], this.getScaledWidth(), this.getScaledHeight()))
                .collect(Collectors.toList());
    }

    public List<Pair<MobEffectInstance, int[]>> getEffectPositions(List<MobEffectInstance> activeEffects) {
        List<Pair<MobEffectInstance, int[]>> effectToPos = Lists.newArrayList();
        for (int counter = 0; counter < activeEffects.size(); counter++) {
            int posX = counter % this.getMaxClampedColumns();
            int posY = counter / this.getMaxClampedColumns();
            effectToPos.add(Pair.of(activeEffects.get(counter), this.coordsToEffectPosition(posX, posY)));
        }
        return effectToPos;
    }

    protected int getTopOffset() {
        return 0;
    }

    protected int[] coordsToEffectPosition(int coordX, int coordY) {
        int[] renderPositions = new int[2];
        switch (this.screenSide) {
            case LEFT -> {
                renderPositions[0] = this.startX - (this.getScaledWidth() + 1) - (this.getScaledWidth() + this.rendererConfig().widgetSpaceX) * coordX;
                renderPositions[1] = this.startY + this.getTopOffset() + this.getAdjustedHeight() * coordY;
            }
            case RIGHT -> {
                renderPositions[0] = this.startX + 1 + (this.getScaledWidth() + this.rendererConfig().widgetSpaceX) * coordX;
                renderPositions[1] = this.startY + this.getTopOffset() + this.getAdjustedHeight() * coordY;
            }
        }
        return renderPositions;
    }

    public void renderEffects(PoseStack poseStack, Minecraft minecraft) {
        for (Pair<MobEffectInstance, int[]> entry : this.getEffectPositions(this.activeEffects)) {
            this.renderWidget(poseStack, entry.getValue()[0], entry.getValue()[1], minecraft, entry.getKey());
        }
    }

    protected float getBlinkingAlpha(MobEffectInstance effectinstance) {
        if (!effectinstance.isAmbient() && effectinstance.getDuration() <= 200) {
            int duration = 10 - effectinstance.getDuration() / 20;
            return Mth.clamp((float) effectinstance.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + Mth.cos((float) effectinstance.getDuration() * (float)Math.PI / 5.0F) * Mth.clamp((float) duration / 10.0F * 0.25F, 0.0F, 0.25F);
        }
        return 1.0F;
    }

    private int getAvailableWidth() {
        return Math.min(this.availableWidth, this.rendererConfig().maxColumns * (this.getScaledWidth() + this.rendererConfig().widgetSpaceX));
    }

    private int getAvailableHeight() {
        return Math.min(this.availableHeight, this.rendererConfig().maxRows * (this.getScaledHeight() + this.rendererConfig().widgetSpaceY));
    }

    private int getMaxColumns() {
        return this.getAvailableWidth() / (this.getScaledWidth() + this.rendererConfig().widgetSpaceX);
    }

    public int getMaxClampedColumns() {
        return Mth.clamp(this.getMaxColumns(), 1, this.rendererConfig().maxColumns);
    }

    private int getAdjustedHeight() {
        if (this.getRows() > this.getMaxClampedRows()) {
            return (this.getAvailableHeight() - this.getScaledHeight()) / Math.max(1, this.getRows() - 1);
        }
        return this.getScaledHeight() + this.rendererConfig().widgetSpaceY;
    }

    private int getMaxRows() {
        return this.getAvailableHeight() / (this.getScaledHeight() + this.rendererConfig().widgetSpaceY);
    }

    public int getMaxClampedRows() {
        return Mth.clamp(this.getMaxRows(), 1, this.rendererConfig().maxRows);
    }

    public int getRows() {
        return this.splitByColumns(this.activeEffects.size());
    }

    protected int splitByColumns(int amountToSplit) {
        return (int) Math.ceil(amountToSplit / (float) this.getMaxClampedColumns());
    }

    protected ClientConfig.EffectRendererConfig rendererConfig() {
        return switch (this.environment) {
            case INVENTORY -> StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer();
            case GUI -> StylishEffects.CONFIG.get(ClientConfig.class).guiRenderer();
        };
    }

    protected abstract ClientConfig.EffectWidgetConfig widgetConfig();

    @Override
    public final void renderWidget(PoseStack poseStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectInstance) {
        RenderSystem.enableBlend();
        poseStack.pushPose();
        double scale = this.getWidgetScale();
        if (scale != 1.0) {
            poseStack.scale((float) scale, (float) scale, 1.0F);
            posX /= scale;
            posY /= scale;
        }
        this.drawWidgetBackground(poseStack, posX, posY, effectInstance);
        this.drawEffectSprite(poseStack, posX, posY, minecraft, effectInstance);
        this.drawEffectText(poseStack, posX, posY, minecraft, effectInstance);
        this.drawEffectAmplifier(poseStack, posX, posY, effectInstance);
        poseStack.popPose();
    }

    protected void drawWidgetBackground(PoseStack poseStack, int posX, int posY, MobEffectInstance effectInstance) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, EFFECT_BACKGROUND);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, (float) this.rendererConfig().widgetAlpha);
        int backgroundY = this.getBackgroundY(effectInstance, this.widgetConfig().ambientBorder, this.widgetConfig().qualityBorder);
        GuiComponent.blit(poseStack, posX, posY, this.getBackgroundTextureX(), this.getBackgroundTextureY() + backgroundY * this.getHeight(), this.getWidth(), this.getHeight(), 256, 256);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    protected void drawEffectSprite(PoseStack poseStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectinstance) {
        if (this.drawCustomEffect(poseStack, posX, posY, effectinstance)) return;
        MobEffectTextureManager potionspriteuploader = minecraft.getMobEffectTextures();
        TextureAtlasSprite textureatlassprite = potionspriteuploader.get(effectinstance.getEffect());
        RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
        float blinkingAlpha = this.widgetConfig().blinkingAlpha ? this.getBlinkingAlpha(effectinstance) : 1.0F;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, blinkingAlpha * (float) this.rendererConfig().widgetAlpha);
        GuiComponent.blit(poseStack, posX + this.getSpriteOffsetX(), posY + this.getSpriteOffsetY(!this.widgetConfig().ambientDuration && effectinstance.isAmbient()), 0, 18, 18, textureatlassprite);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private boolean drawCustomEffect(PoseStack poseStack, int posX, int posY, MobEffectInstance effectinstance) {
        // we make it possible to display effects on any container screen, so this is sometimes unusable
        if (this.screen instanceof EffectRenderingInventoryScreen effectInventoryScreen) {
            return ClientModServices.ABSTRACTIONS.renderInventoryIcon(effectinstance, effectInventoryScreen, poseStack, posX, posY, effectInventoryScreen.getBlitOffset());
        } else if (this.screen instanceof Gui gui) {
            return ClientModServices.ABSTRACTIONS.renderGuiIcon(effectinstance, gui, poseStack, posX, posY, this.screen.getBlitOffset(), this.getBlinkingAlpha(effectinstance) * (float) this.rendererConfig().widgetAlpha);
        }
        return false;
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    protected void drawEffectText(PoseStack poseStack, int posX, int posY, Minecraft minecraft, MobEffectInstance effectinstance) {
        if (!this.widgetConfig().ambientDuration && effectinstance.isAmbient()) return;
        this.getEffectDuration(effectinstance, this.widgetConfig().longDuration).ifPresent(durationComponent -> {
            int potionColor = ColorUtil.getEffectColor(this.widgetConfig().durationColor, effectinstance);
            int alpha = (int) (this.rendererConfig().widgetAlpha * 255.0F) << 24;
            FormattedCharSequence ireorderingprocessor = durationComponent.getVisualOrderText();
            // render shadow on every side to avoid clashing with colorful background
            final int offsetX = this.getDurationOffsetX();
            final int offsetY = this.getDurationOffsetY();
            minecraft.font.draw(poseStack, ireorderingprocessor, posX + offsetX - 1 - minecraft.font.width(ireorderingprocessor) / 2, posY + offsetY, alpha);
            minecraft.font.draw(poseStack, ireorderingprocessor, posX + offsetX + 1 - minecraft.font.width(ireorderingprocessor) / 2, posY + offsetY, alpha);
            minecraft.font.draw(poseStack, ireorderingprocessor, posX + offsetX - minecraft.font.width(ireorderingprocessor) / 2, posY + offsetY - 1, alpha);
            minecraft.font.draw(poseStack, ireorderingprocessor, posX + offsetX - minecraft.font.width(ireorderingprocessor) / 2, posY + offsetY + 1, alpha);
            minecraft.font.draw(poseStack, ireorderingprocessor, posX + offsetX - minecraft.font.width(ireorderingprocessor) / 2, posY + offsetY, alpha | potionColor);
        });
    }

    protected void drawEffectAmplifier(PoseStack poseStack, int posX, int posY, MobEffectInstance effectinstance) {

    }

    protected int getBackgroundY(MobEffectInstance effectInstance, boolean showAmbient, boolean showQuality) {
        if (showAmbient && effectInstance.isAmbient()) {
            return 1;
        }
        if (showQuality) {
            return effectInstance.getEffect().isBeneficial() ? 2 : 3;
        }
        return 0;
    }

    protected Optional<Component> getEffectDuration(MobEffectInstance effectInstance, ClientConfig.LongDuration longDuration) {
        String effectDuration = MobEffectUtil.formatDuration(effectInstance, 1.0F);
        if (effectDuration.equals("**:**")) {
            switch (longDuration) {
                case INFINITY:
                    // infinity char
                    return Optional.of(Component.literal("\u221e"));
                case NONE:
                    return Optional.empty();
                case VANILLA:
            }
        }
        return Optional.of(Component.literal(effectDuration));
    }

    public static String formatTickDuration(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        int days = hours / 24;
        seconds %= 60;
        minutes %= 60;
        hours %= 24;
        if (days > 0) {
            return days + "d";
        } else if (hours > 0) {
            return hours + "h";
        } else if (minutes > 0) {
            return minutes + "m";
        } else {
            return seconds + "s";
        }
    }

    public Optional<List<Component>> getHoveredEffectTooltip(int mouseX, int mouseY, TooltipFlag tooltipFlag) {
        if (this.environment == EffectRendererEnvironment.INVENTORY && StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().hoveringTooltip) {
            return this.getHoveredEffect(mouseX, mouseY)
                    .map(effectInstance -> {
                        List<Component> tooltipLines = this.makeEffectTooltip(effectInstance, StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().tooltipDuration);
                        // call the event here, so we still have access to the effect instance
                        ClientModServices.ABSTRACTIONS.onGatherEffectTooltipLines(this.buildContext(effectInstance), tooltipLines, tooltipFlag);
                        return tooltipLines;
                    });
        }
        return Optional.empty();
    }

    public MobEffectWidgetContext buildContext(MobEffectInstance effectInstance) {
        return MobEffectWidgetContext.of(effectInstance, this.getEffectRenderer(), this.screenSide);
    }

    public Optional<MobEffectInstance> getHoveredEffect(int mouseX, int mouseY) {
        for (Map.Entry<MobEffectInstance, int[]> entry : Lists.reverse(this.getEffectPositions(this.activeEffects))) {
            if (this.isMouseOver(entry.getValue()[0], entry.getValue()[1], mouseX, mouseY)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    private boolean isMouseOver(int posX, int posY, int mouseX, int mouseY) {
        return mouseX >= posX && mouseX <= posX + this.getScaledWidth() && mouseY >= posY && mouseY <= posY + this.getScaledHeight();
    }

    protected List<Component> makeEffectTooltip(MobEffectInstance effectInstance, boolean withDuration) {
        List<Component> tooltip = Lists.newArrayList();
        MutableComponent textComponent = this.getEffectDisplayName(effectInstance);
        if (withDuration && !effectInstance.isNoCounter()) {
            textComponent.append(" ").append(Component.literal("(").append(MobEffectUtil.formatDuration(effectInstance, 1.0F)).append(")").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(textComponent);
        // description may be provided by Just Enough Effect Descriptions mod
        String descriptionKey = effectInstance.getEffect().getDescriptionId() + ".description";
        if (Language.getInstance().has(descriptionKey)) {
            tooltip.add(Component.translatable(descriptionKey).withStyle(ChatFormatting.GRAY));
        }
        return tooltip;
    }

    protected MutableComponent getEffectDisplayName(MobEffectInstance effectInstance) {
        MutableComponent textComponent = Component.empty().append(effectInstance.getEffect().getDisplayName());
        if (effectInstance.getAmplifier() >= 1 && effectInstance.getAmplifier() <= 9) {
            textComponent.append(" ").append(Component.translatable("enchantment.level." + (effectInstance.getAmplifier() + 1)));
        }
        return textComponent;
    }
}
