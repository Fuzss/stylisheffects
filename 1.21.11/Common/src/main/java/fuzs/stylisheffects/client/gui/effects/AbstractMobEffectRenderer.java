package fuzs.stylisheffects.client.gui.effects;

import com.google.common.collect.Lists;
import fuzs.puzzleslib.api.client.gui.v2.ScreenHelper;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.handler.EffectRendererEnvironment;
import fuzs.stylisheffects.client.util.TimeFormattingHelper;
import fuzs.stylisheffects.config.ClientConfig;
import fuzs.stylisheffects.config.EffectAmplifier;
import fuzs.stylisheffects.config.ScreenSide;
import fuzs.stylisheffects.services.ClientAbstractions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractMobEffectRenderer {
    public static final float DEFAULT_WIDGET_SCALE = 4.0F;
    protected static final int MOB_EFFECT_SPRITE_SIZE = 18;
    protected static final Component INFINITY_COMPONENT = Component.literal("\u221e");
    protected static final Identifier EFFECT_BACKGROUND_SPRITE = Identifier.withDefaultNamespace(
            "container/inventory/effect_background");
    protected static final Identifier EFFECT_BACKGROUND_AMBIENT_SPRITE = Identifier.withDefaultNamespace(
            "container/inventory/effect_background_ambient");
    private static final List<Identifier> TINY_NUMBER_SPRITES = List.of(StylishEffects.id("number/zero"),
            StylishEffects.id("number/one"),
            StylishEffects.id("number/two"),
            StylishEffects.id("number/three"),
            StylishEffects.id("number/four"),
            StylishEffects.id("number/five"),
            StylishEffects.id("number/six"),
            StylishEffects.id("number/seven"),
            StylishEffects.id("number/eight"),
            StylishEffects.id("number/nine"));

    private final EffectRendererEnvironment environment;
    protected Object screen;
    private int availableWidth;
    private int availableHeight;
    private int startX;
    private int startY;
    private ScreenSide screenSide;
    protected List<MobEffectInstance> activeEffects;

    protected AbstractMobEffectRenderer(EffectRendererEnvironment environment) {
        this.environment = environment;
    }

    public void setScreenDimensions(Object screen, int availableWidth, int availableHeight, int startX, int startY, ScreenSide screenSide) {
        this.screen = screen;
        this.availableWidth = availableWidth;
        this.availableHeight = availableHeight;
        this.startX = startX;
        this.startY = startY;
        this.screenSide = screenSide;
        switch (this.environment) {
            case GUI -> {
                this.screenSide = this.screenSide.cycle();
                this.availableWidth -= ((ClientConfig.GuiRendererConfig) this.rendererConfig()).offsetX;
                this.availableHeight -= ((ClientConfig.GuiRendererConfig) this.rendererConfig()).offsetY;
                this.startX += (this.screenSide.isRight() ? 1 : -1)
                        * ((ClientConfig.GuiRendererConfig) this.rendererConfig()).offsetX;
                this.startY += ((ClientConfig.GuiRendererConfig) this.rendererConfig()).offsetY;
            }
            case INVENTORY ->
                    this.availableWidth -= ((ClientConfig.InventoryRendererConfig) this.rendererConfig()).screenBorderDistance;
        }
    }

    public final void setActiveEffects(Collection<MobEffectInstance> activeEffects) {
        if (activeEffects.isEmpty()) {
            this.activeEffects = null;
        } else {
            this.activeEffects = activeEffects.stream().filter(this::isEffectAllowedToShow).sorted().toList();
        }
    }

    private boolean isEffectAllowedToShow(MobEffectInstance mobEffect) {
        if (mobEffect.isInfiniteDuration() && this.rendererConfig().skipInfiniteEffects) {
            return false;
        } else if (!mobEffect.showIcon() && !this.rendererConfig().ignoreHideParticles) {
            return false;
        } else {
            return switch (this.environment) {
                case GUI -> ScreenHelper.isEffectVisibleInGui(mobEffect);
                case INVENTORY -> ScreenHelper.isEffectVisibleInInventory(mobEffect);
            };
        }
    }

    public final boolean isActive() {
        return this.activeEffects != null && !this.activeEffects.isEmpty();
    }

    public final boolean isValid() {
        return !this.rendererConfig().allowFallback || this.getMaxRows() > 0 && this.getMaxColumns() > 0;
    }

    public final float getWidgetScale() {
        return (float) (this.rendererConfig().scale / DEFAULT_WIDGET_SCALE);
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public final int getScaledWidth() {
        return (int) (this.getWidth() * this.getWidgetScale());
    }

    public final int getScaledHeight() {
        return (int) (this.getHeight() * this.getWidgetScale());
    }

    protected int getSpriteOffsetX() {
        return (this.getWidth() - MOB_EFFECT_SPRITE_SIZE) / 2;
    }

    protected abstract int getSpriteOffsetY(boolean withoutDuration);

    protected int getDurationOffsetX() {
        return this.getWidth() / 2;
    }

    protected abstract int getDurationOffsetY();

    protected abstract int getBorderSize();

    protected abstract int getAmplifierOffsetX();

    protected abstract int getAmplifierOffsetY();

    public EffectRendererEnvironment.@Nullable Factory getFallbackRenderer() {
        return null;
    }

    public final List<Rect2i> getRenderAreas() {
        return this.getEffectPositions(this.activeEffects)
                .stream()
                .map(Pair::getValue)
                .map((Vector2ic position) -> new Rect2i(position.x(),
                        position.y(),
                        this.getScaledWidth(),
                        this.getScaledHeight()))
                .collect(Collectors.toList());
    }

    public List<Pair<MobEffectInstance, Vector2ic>> getEffectPositions(List<MobEffectInstance> activeEffects) {
        List<Pair<MobEffectInstance, Vector2ic>> mobEffectPositions = new ArrayList<>();
        for (int counter = 0; counter < activeEffects.size(); counter++) {
            int posX = counter % this.getMaxClampedColumns();
            int posY = counter / this.getMaxClampedColumns();
            mobEffectPositions.add(Pair.of(activeEffects.get(counter),
                    this.translateMobEffectWidgetPosition(posX, posY)));
        }

        return mobEffectPositions;
    }

    protected int getTopOffset() {
        return 0;
    }

    protected Vector2ic translateMobEffectWidgetPosition(int posX, int posY) {
        return switch (this.screenSide) {
            case LEFT -> new Vector2i(this.startX - (this.getScaledWidth() + 1)
                    - (this.getScaledWidth() + this.rendererConfig().horizontalSpacing) * posX,
                    this.startY + this.getTopOffset() + this.getAdjustedHeight() * posY);

            case RIGHT -> new Vector2i(
                    this.startX + 1 + (this.getScaledWidth() + this.rendererConfig().horizontalSpacing) * posX,
                    this.startY + this.getTopOffset() + this.getAdjustedHeight() * posY);
        };
    }

    public void renderEffects(GuiGraphics guiGraphics) {
        for (Pair<MobEffectInstance, Vector2ic> entry : this.getEffectPositions(this.activeEffects)) {
            this.renderWidget(guiGraphics, entry.getValue().x(), entry.getValue().y(), entry.getKey());
        }
    }

    /**
     * @see Gui#renderEffects(GuiGraphics, DeltaTracker)
     */
    protected float getBlinkingAlpha(MobEffectInstance mobEffect) {
        if (!mobEffect.isAmbient() && !mobEffect.isInfiniteDuration() && mobEffect.getDuration() <= 200) {
            int duration = 10 - mobEffect.getDuration() / 20;
            return Mth.clamp((float) mobEffect.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
                    + Mth.cos((float) mobEffect.getDuration() * (float) Math.PI / 5.0F) * Mth.clamp(
                    (float) duration / 10.0F * 0.25F, 0.0F, 0.25F);
        } else {
            return 1.0F;
        }
    }

    private int getAvailableWidth() {
        return Math.min(this.availableWidth,
                this.rendererConfig().maxColumns * (this.getScaledWidth() + this.rendererConfig().horizontalSpacing));
    }

    private int getAvailableHeight() {
        return Math.min(this.availableHeight,
                this.rendererConfig().maxRows * (this.getScaledHeight() + this.rendererConfig().verticalSpacing));
    }

    private int getMaxColumns() {
        return this.getAvailableWidth() / (this.getScaledWidth() + this.rendererConfig().horizontalSpacing);
    }

    public int getMaxClampedColumns() {
        return Mth.clamp(this.getMaxColumns(), 1, this.rendererConfig().maxColumns);
    }

    private int getAdjustedHeight() {
        if (this.getRows() > this.getMaxClampedRows()) {
            return (this.getAvailableHeight() - this.getScaledHeight()) / Math.max(1, this.getRows() - 1);
        }
        return this.getScaledHeight() + this.rendererConfig().verticalSpacing;
    }

    private int getMaxRows() {
        return this.getAvailableHeight() / (this.getScaledHeight() + this.rendererConfig().verticalSpacing);
    }

    public int getMaxClampedRows() {
        return Mth.clamp(this.getMaxRows(), 1, this.rendererConfig().maxRows);
    }

    public int getRows() {
        return this.splitByColumns(this.activeEffects.size());
    }

    protected int splitByColumns(int beneficialEffectsAmount) {
        return (int) Math.ceil(beneficialEffectsAmount / (float) this.getMaxClampedColumns());
    }

    protected ClientConfig.EffectRendererConfig rendererConfig() {
        return switch (this.environment) {
            case INVENTORY -> StylishEffects.CONFIG.get(ClientConfig.class).inventoryRendering;
            case GUI -> StylishEffects.CONFIG.get(ClientConfig.class).guiRendering;
        };
    }

    protected ClientConfig.EffectWidgetConfig widgetConfig() {
        return switch (this.environment) {
            case INVENTORY -> StylishEffects.CONFIG.get(ClientConfig.class).inventoryWidgets;
            case GUI -> StylishEffects.CONFIG.get(ClientConfig.class).guiWidgets;
        };
    }

    public final void renderWidget(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance mobEffect) {
        guiGraphics.pose().pushMatrix();
        float scale = this.getWidgetScale();
        if (scale != 1.0F) {
            guiGraphics.pose().scale(scale, scale);
            posX /= scale;
            posY /= scale;
        }

        this.renderBackground(guiGraphics, posX, posY, mobEffect);
        this.renderContents(guiGraphics, posX, posY, mobEffect);
        this.renderLabels(guiGraphics, posX, posY, mobEffect);
        guiGraphics.pose().popMatrix();
    }

    protected ActiveTextCollector activeTextCollector(GuiGraphics guiGraphics) {
        ActiveTextCollector activeTextCollector = guiGraphics.textRenderer(GuiGraphics.HoveredTextEffects.NONE);
        activeTextCollector.defaultParameters(activeTextCollector.defaultParameters()
                .withOpacity((float) this.rendererConfig().alpha));
        return activeTextCollector;
    }

    protected void renderBackground(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance mobEffect) {
        Identifier effectBackgroundSprite = this.getEffectBackgroundSprite(
                mobEffect.isAmbient() && this.widgetConfig().ambientBorder);
        int colorValue = ARGB.white((float) this.rendererConfig().alpha);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                effectBackgroundSprite,
                posX,
                posY,
                this.getWidth(),
                this.getHeight(),
                colorValue);
    }

    protected Identifier getEffectBackgroundSprite(boolean isAmbient) {
        return isAmbient ? EFFECT_BACKGROUND_AMBIENT_SPRITE : EFFECT_BACKGROUND_SPRITE;
    }

    protected void renderContents(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance mobEffect) {
        if (!this.drawCustomEffect(guiGraphics, posX, posY, mobEffect)) {
            this.drawEffectSprite(guiGraphics, posX, posY, mobEffect);
        }

        if (this.widgetConfig().effectAmplifier != EffectAmplifier.NONE) {
            this.drawEffectAmplifier(guiGraphics, posX, posY, mobEffect);
        }
    }

    protected void drawEffectSprite(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance mobEffect) {
        float blinkingAlpha = this.widgetConfig().blinkingAlpha ? this.getBlinkingAlpha(mobEffect) : 1.0F;
        int colorValue = ARGB.white(blinkingAlpha * (float) this.rendererConfig().alpha);
        Identifier identifier = Gui.getMobEffectSprite(mobEffect.getEffect());
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                identifier,
                posX + this.getSpriteOffsetX(),
                posY + this.getSpriteOffsetY(this.getEffectDuration(mobEffect, -1) == null),
                MOB_EFFECT_SPRITE_SIZE,
                MOB_EFFECT_SPRITE_SIZE,
                colorValue);
    }

    private boolean drawCustomEffect(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance mobEffect) {
        // we make it possible to display effects on any container screen, so this is sometimes unusable
        if (this.screen instanceof AbstractContainerScreen<?> screen && screen.showsActiveEffects()) {
            return ClientAbstractions.INSTANCE.renderInventoryIcon(mobEffect, screen, guiGraphics, posX, posY, 0);
        } else if (this.screen instanceof Gui gui) {
            return ClientAbstractions.INSTANCE.renderGuiIcon(mobEffect,
                    gui,
                    guiGraphics,
                    posX,
                    posY,
                    0,
                    this.getBlinkingAlpha(mobEffect) * (float) this.rendererConfig().alpha);
        } else {
            return false;
        }
    }

    protected void drawEffectAmplifier(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance mobEffect) {
        if (mobEffect.getAmplifier() >= 1 && mobEffect.getAmplifier() <= 8) {
            int mobEffectColor = this.widgetConfig().amplifierColor.getMobEffectColor(mobEffect);
            // subtract amplifier width of 3
            int offsetX = this.widgetConfig().effectAmplifier == EffectAmplifier.TOP_LEFT ? this.getAmplifierOffsetX() :
                    this.getWidth() - this.getAmplifierOffsetX() - 3;
            int offsetY = this.getAmplifierOffsetY();
            // drop shadow on all sides
            int backgroundColorValue = ARGB.color((float) this.rendererConfig().alpha, 0);
            Identifier numberSprite = TINY_NUMBER_SPRITES.get(mobEffect.getAmplifier() + 1);
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i != 0 || j != 0) {
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                                numberSprite,
                                posX + offsetX + i,
                                posY + offsetY + j,
                                3,
                                5,
                                backgroundColorValue);
                    }
                }
            }

            // actual number
            int colorValue = ARGB.color((float) this.rendererConfig().alpha, mobEffectColor);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    numberSprite,
                    posX + offsetX,
                    posY + offsetY,
                    3,
                    5,
                    colorValue);
        }
    }

    protected void renderLabels(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance mobEffect) {
        Component component = this.getEffectDuration(mobEffect, this.getWidth() - this.getBorderSize() * 2);
        if (component != null) {
            int x = posX + this.getDurationOffsetX() - Minecraft.getInstance().font.width(component) / 2;
            int y = posY + this.getDurationOffsetY();
            Component backgroundComponent = ComponentUtils.mergeStyles(component,
                    Style.EMPTY.withColor(ChatFormatting.BLACK));
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i != 0 || j != 0) {
                        guiGraphics.drawString(Minecraft.getInstance().font,
                                backgroundComponent,
                                x + i,
                                y + j,
                                ARGB.white((float) this.rendererConfig().alpha),
                                false);
                    }
                }
            }

            Style durationStyle = this.widgetConfig().durationColor.getMobEffectStyle(mobEffect);
            guiGraphics.drawString(Minecraft.getInstance().font,
                    ComponentUtils.mergeStyles(component, durationStyle),
                    x,
                    y,
                    ARGB.white((float) this.rendererConfig().alpha),
                    false);
        }
    }

    protected final @Nullable Component getEffectDuration(MobEffectInstance mobEffect, int maxWidth) {
        if (mobEffect.isAmbient() && !this.widgetConfig().ambientDuration) {
            return null;
        }

        if (mobEffect.isInfiniteDuration()) {
            return this.widgetConfig().infiniteDuration ? INFINITY_COMPONENT : null;
        }

        if (this.widgetConfig().shortenEffectDuration) {
            return Component.literal(TimeFormattingHelper.applyShortTickDurationFormat(mobEffect.getDuration()));
        } else {
            Component component = Component.literal(TimeFormattingHelper.applyLongTickDurationFormat(mobEffect.getDuration()));
            if (Minecraft.getInstance().font.width(component) > maxWidth) {
                return Component.literal(TimeFormattingHelper.applyShortTickDurationFormat(mobEffect.getDuration()));
            } else {
                return component;
            }
        }
    }

    public Optional<List<Component>> getHoveredEffectTooltip(AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        if (this.rendererConfig().hoveringTooltip()) {
            return this.getHoveredEffect(mouseX, mouseY).map((MobEffectInstance mobEffect) -> {
                List<Component> tooltipLines = new ArrayList<>(Collections.singletonList(this.getEffectDisplayName(
                        mobEffect,
                        this.rendererConfig().tooltipDuration())));
                // call the event here, so we still have access to the effect instance
                ClientAbstractions.INSTANCE.onGatherEffectScreenTooltip(screen, mobEffect, tooltipLines);
                return tooltipLines;
            });
        } else {
            return Optional.empty();
        }
    }

    public Optional<MobEffectInstance> getHoveredEffect(int mouseX, int mouseY) {
        for (Map.Entry<MobEffectInstance, Vector2ic> entry : Lists.reverse(this.getEffectPositions(this.activeEffects))) {
            if (ScreenHelper.isHovering(entry.getValue().x(),
                    entry.getValue().y(),
                    this.getScaledWidth(),
                    this.getScaledHeight(),
                    mouseX,
                    mouseY)) {
                return Optional.of(entry.getKey());
            }
        }

        return Optional.empty();
    }

    protected Component getEffectDisplayName(MobEffectInstance mobEffect, boolean includeDuration) {
        MutableComponent component = mobEffect.getEffect().value().getDisplayName().copy();
        String translationKey = "enchantment.level." + (mobEffect.getAmplifier() + 1);
        if (mobEffect.getAmplifier() >= 1 && mobEffect.getAmplifier() <= 9 || Language.getInstance()
                .has(translationKey)) {
            component.append(" ").append(Component.translatable(translationKey));
        }

        if (includeDuration && !mobEffect.isInfiniteDuration()) {
            String durationTime = TimeFormattingHelper.applyLongTickDurationFormat(mobEffect.getDuration());
            return component.append(CommonComponents.SPACE)
                    .append(Component.literal("(").append(durationTime).append(")").withStyle(ChatFormatting.GRAY));
        } else {
            return component;
        }
    }
}
