package fuzs.stylisheffects.client.gui.effects;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import fuzs.puzzleslib.api.client.gui.v2.AnchorPoint;
import fuzs.puzzleslib.api.client.gui.v2.ScreenHelper;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.client.util.TimeFormattingHelper;
import fuzs.stylisheffects.config.ClientConfig;
import fuzs.stylisheffects.config.ScreenSide;
import fuzs.stylisheffects.config.WidgetType;
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
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

import java.util.*;

public abstract class AbstractMobEffectRenderer {
    public static final float DEFAULT_WIDGET_SCALE = 4.0F;
    protected static final int MOB_EFFECT_SPRITE_SIZE = 18;
    protected static final int TINY_NUMBER_WIDTH = 3;
    protected static final int TINY_NUMBER_HEIGHT = 5;
    protected static final Component INFINITY_COMPONENT = Component.literal("\u221e");
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

    protected Either<Gui, AbstractContainerScreen<?>> environment;
    protected final ClientConfig.EffectWidgetsConfig config;
    private int screenWidth;
    private int screenHeight;
    private int startX;
    private int startY;

    protected AbstractMobEffectRenderer(Either<Gui, AbstractContainerScreen<?>> environment) {
        this.environment = environment;
        this.config = this.environment.map((Gui gui) -> {
            return StylishEffects.CONFIG.get(ClientConfig.class).guiWidgets;
        }, (AbstractContainerScreen<?> screen) -> {
            return StylishEffects.CONFIG.get(ClientConfig.class).inventoryWidgets;
        });
        this.init();
    }

    public void init() {
        this.environment.ifLeft(this::initGui).ifRight(this::initScreen);
    }

    private void initGui(Gui gui) {
        this.screenWidth = gui.minecraft.getWindow().getGuiScaledWidth() - this.config.horizontalOffset() * 2;
        this.screenHeight = gui.minecraft.getWindow().getGuiScaledHeight() - this.config.verticalOffset() * 2;
        this.startX = this.config.effectPositions.screenSide.isRight() ?
                gui.minecraft.getWindow().getGuiScaledWidth() - this.config.horizontalOffset() :
                this.config.horizontalOffset();
        this.startY = this.config.verticalOffset();
    }

    private void initScreen(AbstractContainerScreen<?> screen) {
        this.screenWidth = (this.config.effectPositions.screenSide.isLeft() ? screen.leftPos :
                screen.width - screen.imageWidth - screen.leftPos) - this.config.horizontalOffset();
        this.screenHeight = screen.imageHeight - this.config.verticalOffset();
        this.startX =
                this.config.effectPositions.screenSide.isLeft() ? screen.leftPos : screen.leftPos + screen.imageWidth;
        this.startY = screen.topPos;
    }

    public List<MobEffectInstance> getMobEffects(@Nullable Player player) {
        return player != null ? player.getActiveEffects().stream().filter(this::isShowingMobEffect).sorted().toList() :
                Collections.emptyList();
    }

    private boolean isShowingMobEffect(MobEffectInstance mobEffect) {
        if (mobEffect.isInfiniteDuration() && this.config.skipInfiniteEffects) {
            return false;
        } else if (!mobEffect.showIcon() && !this.config.ignoreHideParticles) {
            return false;
        } else {
            return this.environment.map((Gui gui) -> {
                return ScreenHelper.isEffectVisibleInGui(mobEffect);
            }, (AbstractContainerScreen<?> screen) -> {
                return ScreenHelper.isEffectVisibleInInventory(mobEffect);
            });
        }
    }

    public final boolean hasEnoughSpace() {
        return this.getMaxRows() > 0 && this.getMaxColumns() > 0;
    }

    public final float getWidgetScale() {
        return (float) (this.config.widgetScale / DEFAULT_WIDGET_SCALE);
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

    protected ScreenSide getScreenSide() {
        return this.environment.map((Gui gui) -> {
            return this.config.effectPositions.screenSide.flip();
        }, (AbstractContainerScreen<?> screen) -> {
            return this.config.effectPositions.screenSide;
        });
    }

    public WidgetType.@Nullable Factory getFallbackRenderer() {
        return null;
    }

    public final List<Rect2i> getGuiExtraAreas(List<MobEffectInstance> mobEffects) {
        return this.getEffectPositions(mobEffects).stream().map(Pair::getValue).map((Vector2ic position) -> {
            return new Rect2i(position.x(), position.y(), this.getScaledWidth(), this.getScaledHeight());
        }).toList();
    }

    public List<Pair<MobEffectInstance, Vector2ic>> getEffectPositions(List<MobEffectInstance> mobEffects) {
        List<Pair<MobEffectInstance, Vector2ic>> mobEffectPositions = new ArrayList<>();
        for (int counter = 0; counter < mobEffects.size(); counter++) {
            int posX = counter % this.getMaxClampedColumns();
            int posY = counter / this.getMaxClampedColumns();
            mobEffectPositions.add(Pair.of(mobEffects.get(counter),
                    this.translateMobEffectWidgetPosition(posX, posY, mobEffects)));
        }

        return mobEffectPositions;
    }

    protected int getTopOffset() {
        return 0;
    }

    protected Vector2ic translateMobEffectWidgetPosition(int posX, int posY, List<MobEffectInstance> mobEffects) {
        return switch (this.getScreenSide()) {
            case LEFT -> new Vector2i(this.startX - (this.getScaledWidth() + 1)
                    - (this.getScaledWidth() + this.config.effectPositions.horizontalSpacing) * posX,
                    this.startY + this.getTopOffset() + this.getAdjustedHeight(mobEffects) * posY);

            case RIGHT -> new Vector2i(
                    this.startX + 1 + (this.getScaledWidth() + this.config.effectPositions.horizontalSpacing) * posX,
                    this.startY + this.getTopOffset() + this.getAdjustedHeight(mobEffects) * posY);
        };
    }

    public void renderEffectWidgets(GuiGraphics guiGraphics, List<MobEffectInstance> mobEffects) {
        for (Pair<MobEffectInstance, Vector2ic> entry : this.getEffectPositions(mobEffects)) {
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
        return Math.min(this.screenWidth,
                this.config.effectPositions.maxColumns * (this.getScaledWidth()
                        + this.config.effectPositions.horizontalSpacing));
    }

    private int getAvailableHeight() {
        return Math.min(this.screenHeight,
                this.config.effectPositions.maxRows * (this.getScaledHeight()
                        + this.config.effectPositions.verticalSpacing));
    }

    private int getMaxColumns() {
        return this.getAvailableWidth() / (this.getScaledWidth() + this.config.effectPositions.horizontalSpacing);
    }

    public int getMaxClampedColumns() {
        return Mth.clamp(this.getMaxColumns(), 1, this.config.effectPositions.maxColumns);
    }

    private int getAdjustedHeight(List<MobEffectInstance> mobEffects) {
        if (this.getRows(mobEffects) > this.getMaxClampedRows()) {
            return (this.getAvailableHeight() - this.getScaledHeight()) / Math.max(1, this.getRows(mobEffects) - 1);
        }
        return this.getScaledHeight() + this.config.effectPositions.verticalSpacing;
    }

    private int getMaxRows() {
        return this.getAvailableHeight() / (this.getScaledHeight() + this.config.effectPositions.verticalSpacing);
    }

    public int getMaxClampedRows() {
        return Mth.clamp(this.getMaxRows(), 1, this.config.effectPositions.maxRows);
    }

    public int getRows(List<MobEffectInstance> mobEffects) {
        return this.splitByColumns(mobEffects.size());
    }

    protected int splitByColumns(int beneficialEffectsAmount) {
        return (int) Math.ceil(beneficialEffectsAmount / (float) this.getMaxClampedColumns());
    }

    public final void renderWidget(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance mobEffect) {
        guiGraphics.pose().pushMatrix();
        float scale = this.getWidgetScale();
        if (scale != 1.0F) {
            guiGraphics.pose().scale(scale, scale);
            posX /= scale;
            posY /= scale;
        }

        this.renderContents(guiGraphics, posX, posY, mobEffect);
        guiGraphics.pose().popMatrix();
    }

    protected ActiveTextCollector activeTextCollector(GuiGraphics guiGraphics) {
        ActiveTextCollector activeTextCollector = guiGraphics.textRenderer(GuiGraphics.HoveredTextEffects.NONE);
        activeTextCollector.defaultParameters(activeTextCollector.defaultParameters()
                .withOpacity((float) this.config.widgetAlpha));
        return activeTextCollector;
    }

    protected void renderBackground(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance mobEffect) {
        Identifier backgroundSprite = this.getEffectBackgroundSprite(
                mobEffect.isAmbient() && this.config.ambientBorder);
        int colorValue = ARGB.white((float) this.config.widgetAlpha);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                backgroundSprite,
                posX,
                posY,
                this.getWidth(),
                this.getHeight(),
                colorValue);
    }

    protected abstract Identifier getEffectBackgroundSprite(boolean isAmbient);

    protected void renderContents(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance mobEffect) {
        this.renderBackground(guiGraphics, posX, posY, mobEffect);
        if (!this.renderCustomSprite(guiGraphics, posX, posY, mobEffect)) {
            this.renderSprite(guiGraphics, posX, posY, mobEffect);
        }

        this.renderLabels(guiGraphics, posX, posY, mobEffect);
        if (this.config.effectAmplifier.effectAmplifier) {
            this.renderForeground(guiGraphics, posX, posY, mobEffect);
        }
    }

    protected void renderSprite(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance mobEffect) {
        float blinkingAlpha = this.config.blinkingSprite ? this.getBlinkingAlpha(mobEffect) : 1.0F;
        int colorValue = ARGB.white(blinkingAlpha * (float) this.config.widgetAlpha);
        Identifier mobEffectSprite = Gui.getMobEffectSprite(mobEffect.getEffect());
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                mobEffectSprite,
                posX + this.getSpriteOffsetX(),
                posY + this.getSpriteOffsetY(this.getEffectDuration(mobEffect, -1) == null),
                MOB_EFFECT_SPRITE_SIZE,
                MOB_EFFECT_SPRITE_SIZE,
                colorValue);
    }

    private boolean renderCustomSprite(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance mobEffect) {
        return this.environment.map((Gui gui) -> {
            return ClientAbstractions.INSTANCE.renderGuiIcon(mobEffect,
                    gui,
                    guiGraphics,
                    posX,
                    posY,
                    0,
                    this.getBlinkingAlpha(mobEffect) * (float) this.config.widgetAlpha);
        }, (AbstractContainerScreen<?> screen) -> {
            return ClientAbstractions.INSTANCE.renderInventoryIcon(mobEffect, screen, guiGraphics, posX, posY, 0);
        });
    }

    protected void renderForeground(GuiGraphics guiGraphics, int posX, int posY, MobEffectInstance mobEffect) {
        if (mobEffect.getAmplifier() >= 1 && mobEffect.getAmplifier() <= 8) {
            int mobEffectColor = this.config.effectAmplifier.amplifierColor.getMobEffectColor(mobEffect);
            AnchorPoint.Positioner positioner = this.config.effectAmplifier.amplifierPosition.createPositioner(this.getWidth(),
                    this.getHeight(),
                    TINY_NUMBER_WIDTH,
                    TINY_NUMBER_HEIGHT);
            int offsetX = positioner.getPosX(this.getAmplifierOffsetX());
            int offsetY = positioner.getPosY(this.getAmplifierOffsetY());
            int backgroundColorValue = ARGB.color((float) this.config.widgetAlpha, 0);
            Identifier numberSprite = TINY_NUMBER_SPRITES.get(mobEffect.getAmplifier() + 1);
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i != 0 || j != 0) {
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                                numberSprite,
                                posX + offsetX + i,
                                posY + offsetY + j,
                                TINY_NUMBER_WIDTH,
                                TINY_NUMBER_HEIGHT,
                                backgroundColorValue);
                    }
                }
            }

            int colorValue = ARGB.color((float) this.config.widgetAlpha, mobEffectColor);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    numberSprite,
                    posX + offsetX,
                    posY + offsetY,
                    TINY_NUMBER_WIDTH,
                    TINY_NUMBER_HEIGHT,
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
                                ARGB.white((float) this.config.widgetAlpha),
                                false);
                    }
                }
            }

            Style durationStyle = this.config.effectDuration.durationColor.getMobEffectStyle(mobEffect);
            guiGraphics.drawString(Minecraft.getInstance().font,
                    ComponentUtils.mergeStyles(component, durationStyle),
                    x,
                    y,
                    ARGB.white((float) this.config.widgetAlpha),
                    false);
        }
    }

    protected final @Nullable Component getEffectDuration(MobEffectInstance mobEffect, int maxWidth) {
        if (mobEffect.isAmbient() && !this.config.effectDuration.ambientDuration) {
            return null;
        }

        if (mobEffect.isInfiniteDuration()) {
            return this.config.effectDuration.infiniteDuration ? INFINITY_COMPONENT : null;
        }

        if (this.config.effectDuration.shortenEffectDuration) {
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

    public Optional<List<Component>> getHoveredEffectTooltip(int mouseX, int mouseY, List<MobEffectInstance> mobEffects) {
        if (this.config.hoveringTooltip()) {
            return this.environment.map((Gui gui) -> {
                return Optional.empty();
            }, (AbstractContainerScreen<?> screen) -> {
                return this.getHoveredEffect(mouseX, mouseY, mobEffects).map((MobEffectInstance mobEffect) -> {
                    List<Component> tooltipLines = new ArrayList<>(Collections.singletonList(this.getEffectDisplayName(
                            mobEffect,
                            this.config.tooltipDuration())));
                    // call the event here, so we still have access to the effect instance
                    ClientAbstractions.INSTANCE.onGatherEffectScreenTooltip(screen, mobEffect, tooltipLines);
                    return tooltipLines;
                });
            });
        } else {
            return Optional.empty();
        }
    }

    public Optional<MobEffectInstance> getHoveredEffect(int mouseX, int mouseY, List<MobEffectInstance> mobEffects) {
        for (Map.Entry<MobEffectInstance, Vector2ic> entry : Lists.reverse(this.getEffectPositions(mobEffects))) {
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
