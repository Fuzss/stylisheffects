package fuzs.stylisheffects.client.gui.effects;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.api.v1.client.MobEffectWidgetContext;
import fuzs.stylisheffects.client.handler.EffectRendererEnvironment;
import fuzs.stylisheffects.client.util.ColorUtil;
import fuzs.stylisheffects.config.ClientConfig;
import fuzs.stylisheffects.services.ClientAbstractions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.TooltipFlag;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractEffectRenderer implements EffectWidget, RenderAreasProvider {
    public static final double DEFAULT_WIDGET_SCALE = 4.0;
    protected static final ResourceLocation EFFECT_BACKGROUND = StylishEffects.id(
            "textures/gui/mob_effect_background.png");

    private final EffectRendererEnvironment environment;
    protected Object screen;
    private int availableWidth;
    private int availableHeight;
    private int startX;
    private int startY;
    private MobEffectWidgetContext.ScreenSide screenSide;
    protected List<MobEffectInstance> activeEffects;

    protected AbstractEffectRenderer(EffectRendererEnvironment environment) {
        this.environment = environment;
    }

    public void setScreenDimensions(Object screen, int availableWidth, int availableHeight, int startX, int startY, MobEffectWidgetContext.ScreenSide screenSide) {
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
                this.startX += (this.screenSide.right() ? 1 : -1) *
                        ((ClientConfig.GuiRendererConfig) this.rendererConfig()).offsetX;
                this.startY += ((ClientConfig.GuiRendererConfig) this.rendererConfig()).offsetY;
            }
            case INVENTORY ->
                    this.availableWidth -= ((ClientConfig.InventoryRendererConfig) this.rendererConfig()).screenBorderDistance;
        }
    }

    public final void setActiveEffects(Collection<MobEffectInstance> activeEffects) {
        if (activeEffects.isEmpty()) {
            this.activeEffects = null;
            return;
        }
        this.activeEffects = activeEffects.stream()
                .filter(this::isEffectAllowedToShow)
                .sorted()
                .collect(Collectors.toList());
    }

    private boolean isEffectAllowedToShow(MobEffectInstance mobEffect) {
        if (mobEffect.isInfiniteDuration() && this.rendererConfig().hideInfiniteEffects) {
            return false;
        } else if (!mobEffect.showIcon() && this.rendererConfig().respectHideParticles) {
            return false;
        } else {
            return switch (this.environment) {
                case GUI ->
                        fuzs.puzzleslib.api.client.core.v1.ClientAbstractions.INSTANCE.isEffectVisibleInGui(mobEffect);
                case INVENTORY ->
                        fuzs.puzzleslib.api.client.core.v1.ClientAbstractions.INSTANCE.isEffectVisibleInInventory(
                                mobEffect);
            };
        }
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
        return this.getEffectPositions(this.activeEffects)
                .stream()
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
                renderPositions[0] = this.startX - (this.getScaledWidth() + 1) -
                        (this.getScaledWidth() + this.rendererConfig().widgetSpaceX) * coordX;
                renderPositions[1] = this.startY + this.getTopOffset() + this.getAdjustedHeight() * coordY;
            }
            case RIGHT -> {
                renderPositions[0] =
                        this.startX + 1 + (this.getScaledWidth() + this.rendererConfig().widgetSpaceX) * coordX;
                renderPositions[1] = this.startY + this.getTopOffset() + this.getAdjustedHeight() * coordY;
            }
        }
        return renderPositions;
    }

    public void renderEffects(GuiGraphics guiGraphics, Minecraft minecraft) {
        for (Pair<MobEffectInstance, int[]> entry : this.getEffectPositions(this.activeEffects)) {
            this.renderWidget(guiGraphics, entry.getValue()[0], entry.getValue()[1], minecraft, entry.getKey());
        }
    }

    protected float getBlinkingAlpha(MobEffectInstance mobEffectInstance) {
        if (!mobEffectInstance.isAmbient() && !mobEffectInstance.isInfiniteDuration() &&
                mobEffectInstance.getDuration() <= 200) {
            int duration = 10 - mobEffectInstance.getDuration() / 20;
            return Mth.clamp((float) mobEffectInstance.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) +
                    Mth.cos((float) mobEffectInstance.getDuration() * (float) Math.PI / 5.0F) *
                            Mth.clamp((float) duration / 10.0F * 0.25F, 0.0F, 0.25F);
        }
        return 1.0F;
    }

    private int getAvailableWidth() {
        return Math.min(this.availableWidth,
                this.rendererConfig().maxColumns * (this.getScaledWidth() + this.rendererConfig().widgetSpaceX));
    }

    private int getAvailableHeight() {
        return Math.min(this.availableHeight,
                this.rendererConfig().maxRows * (this.getScaledHeight() + this.rendererConfig().widgetSpaceY));
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
    public final void renderWidget(GuiGraphics guiGraphics, int posX, int posY, Minecraft minecraft, MobEffectInstance mobEffectInstance) {
        RenderSystem.enableBlend();
        guiGraphics.pose().pushPose();
        double scale = this.getWidgetScale();
        if (scale != 1.0) {
            guiGraphics.pose().scale((float) scale, (float) scale, 1.0F);
            posX /= scale;
            posY /= scale;
        }
        this.drawWidgetBackground(guiGraphics, posX, posY, mobEffectInstance);
        this.drawEffectSprite(guiGraphics, posX, posY, minecraft, mobEffectInstance);
        this.drawEffectText(guiGraphics, posX, posY, minecraft, mobEffectInstance);
        this.drawEffectAmplifier(guiGraphics, posX, posY, mobEffectInstance);
        guiGraphics.pose().popPose();
    }

    protected void drawWidgetBackground(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance mobEffectInstance) {
        int backgroundY = this.getBackgroundY(mobEffectInstance,
                this.widgetConfig().ambientBorder,
                this.widgetConfig().qualityBorder);
        int colorValue = ARGB.white((float) this.rendererConfig().widgetAlpha);
        guiGraphics.blit(RenderType::guiTextured,
                EFFECT_BACKGROUND,
                posX,
                posY,
                this.getBackgroundTextureX(),
                this.getBackgroundTextureY() + backgroundY * this.getHeight(),
                this.getWidth(),
                this.getHeight(),
                256,
                256,
                colorValue);
    }

    protected void drawEffectSprite(GuiGraphics guiGraphics, int posX, int posY, Minecraft minecraft, MobEffectInstance mobEffectInstance) {
        if (this.drawCustomEffect(guiGraphics, posX, posY, mobEffectInstance)) return;
        float blinkingAlpha = this.widgetConfig().blinkingAlpha ? this.getBlinkingAlpha(mobEffectInstance) : 1.0F;
        int colorValue = ARGB.white(blinkingAlpha * (float) this.rendererConfig().widgetAlpha);
        TextureAtlasSprite atlasSprite = minecraft.getMobEffectTextures().get(mobEffectInstance.getEffect());
        guiGraphics.blitSprite(RenderType::guiTextured,
                atlasSprite,
                posX + this.getSpriteOffsetX(),
                posY + this.getSpriteOffsetY(!this.widgetConfig().ambientDuration && mobEffectInstance.isAmbient()),
                18,
                18,
                colorValue);
    }

    private boolean drawCustomEffect(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance mobEffectInstance) {
        // we make it possible to display effects on any container screen, so this is sometimes unusable
        if (this.screen instanceof AbstractContainerScreen<?> screen && screen.showsActiveEffects()) {
            return ClientAbstractions.INSTANCE.renderInventoryIcon(mobEffectInstance,
                    screen,
                    guiGraphics,
                    posX,
                    posY,
                    0);
        } else if (this.screen instanceof Gui gui) {
            return ClientAbstractions.INSTANCE.renderGuiIcon(mobEffectInstance,
                    gui,
                    guiGraphics,
                    posX,
                    posY,
                    0,
                    this.getBlinkingAlpha(mobEffectInstance) * (float) this.rendererConfig().widgetAlpha);
        } else {
            return false;
        }
    }

    protected void drawEffectText(GuiGraphics guiGraphics, int posX, int posY, Minecraft minecraft, MobEffectInstance mobEffectInstance) {
        if (!this.widgetConfig().ambientDuration && mobEffectInstance.isAmbient()) return;
        this.getEffectDuration(mobEffectInstance).ifPresent(durationComponent -> {
            int potionColor = ColorUtil.getEffectColor(this.widgetConfig().durationColor, mobEffectInstance);
            int colorValue = ARGB.color(ARGB.as8BitChannel((float) this.rendererConfig().widgetAlpha), potionColor);
            int backgroundColorValue = ARGB.color(ARGB.as8BitChannel((float) this.rendererConfig().widgetAlpha), 0);
            FormattedCharSequence text = durationComponent.getVisualOrderText();
            // render shadow on every side to avoid clashing with colorful background
            final int offsetX = this.getDurationOffsetX();
            final int offsetY = this.getDurationOffsetY();
            guiGraphics.drawSpecial((MultiBufferSource bufferSource) -> {
                minecraft.font.drawInBatch8xOutline(text,
                        posX + offsetX - minecraft.font.width(text) / 2.0F,
                        posY + offsetY,
                        colorValue,
                        backgroundColorValue,
                        guiGraphics.pose().last().pose(),
                        bufferSource,
                        0XF000F0);
            });
        });
    }

    protected void drawEffectAmplifier(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance mobEffectInstance) {
        // NO-OP
    }

    protected int getBackgroundY(MobEffectInstance mobEffectInstance, boolean showAmbient, boolean showQuality) {
        if (showAmbient && mobEffectInstance.isAmbient()) {
            return 1;
        } else if (showQuality) {
            return mobEffectInstance.getEffect().value().isBeneficial() ? 2 : 3;
        } else {
            return 0;
        }
    }

    protected final Optional<Component> getEffectDuration(MobEffectInstance mobEffectInstance) {
        if (this.isInfiniteDuration(mobEffectInstance)) {
            return Optional.ofNullable(this.getInfiniteDurationString()).map(Component::literal);
        } else {
            return Optional.of(Component.literal(this.formatEffectDuration(mobEffectInstance)));
        }
    }

    @Nullable
    protected String getInfiniteDurationString() {
        return "\u221e";
    }

    protected boolean isInfiniteDuration(MobEffectInstance mobEffectInstance) {
        return mobEffectInstance.getDuration() >= 72000 || mobEffectInstance.isInfiniteDuration();
    }

    protected String formatEffectDuration(MobEffectInstance mobEffectInstance) {
        return formatTickDuration(mobEffectInstance.getDuration());
    }

    public static String formatTickDuration(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        int hours = minutes / 60;
        minutes %= 60;
        if (hours > 0) {
            return String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.ROOT, "%d:%02d", minutes, seconds);
        }
    }

    public static String formatCompactTickDuration(int ticks) {
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
        if (this.environment == EffectRendererEnvironment.INVENTORY &&
                StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().hoveringTooltip) {
            return this.getHoveredEffect(mouseX, mouseY).map(mobEffectInstance -> {
                List<Component> tooltipLines = this.makeEffectTooltip(mobEffectInstance,
                        StylishEffects.CONFIG.get(ClientConfig.class).inventoryRenderer().tooltipDuration);
                // call the event here, so we still have access to the effect instance
                ClientAbstractions.INSTANCE.onGatherEffectTooltipLines(this.buildContext(mobEffectInstance),
                        tooltipLines,
                        tooltipFlag);
                return tooltipLines;
            });
        } else {
            return Optional.empty();
        }
    }

    public MobEffectWidgetContext buildContext(MobEffectInstance mobEffectInstance) {
        return MobEffectWidgetContext.of(mobEffectInstance, this.getEffectRenderer(), this.screenSide);
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
        return mouseX >= posX && mouseX <= posX + this.getScaledWidth() && mouseY >= posY &&
                mouseY <= posY + this.getScaledHeight();
    }

    protected List<Component> makeEffectTooltip(MobEffectInstance mobEffectInstance, boolean withDuration) {
        List<Component> tooltip = Lists.newArrayList();
        MutableComponent textComponent = this.getEffectDisplayName(mobEffectInstance);
        if (withDuration && !mobEffectInstance.isInfiniteDuration()) {
            textComponent.append(CommonComponents.SPACE)
                    .append(Component.literal("(")
                            .append(formatTickDuration(mobEffectInstance.getDuration()))
                            .append(")")
                            .withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(textComponent);
        String descriptionKey = getDescriptionTranslationKey(mobEffectInstance.getEffect().value().getDescriptionId());
        if (descriptionKey != null) {
            tooltip.add(Component.translatable(descriptionKey).withStyle(ChatFormatting.GRAY));
        }
        return tooltip;
    }

    @Nullable
    private static String getDescriptionTranslationKey(String id) {
        if (Language.getInstance().has(id + ".desc")) {
            // our own format, similar to Enchantment Descriptions mod format
            return id + ".desc";
        } else if (Language.getInstance().has(id + ".description")) {
            // Just Enough Effect Descriptions mod format
            return id + ".description";
        } else if (Language.getInstance().has("description." + id)) {
            // Potion Descriptions mod format
            return "description." + id;
        } else {
            return null;
        }
    }

    protected MutableComponent getEffectDisplayName(MobEffectInstance mobEffectInstance) {
        MutableComponent textComponent = Component.empty()
                .append(mobEffectInstance.getEffect().value().getDisplayName());
        String translationKey = "enchantment.level." + (mobEffectInstance.getAmplifier() + 1);
        if (mobEffectInstance.getAmplifier() >= 1 && mobEffectInstance.getAmplifier() <= 9 ||
                Language.getInstance().has(translationKey)) {
            textComponent.append(" ").append(Component.translatable(translationKey));
        }

        return textComponent;
    }
}
