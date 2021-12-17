package fuzs.stylisheffects.client.gui.effects;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.*;
import net.minecraftforge.common.extensions.IForgeEffectInstance;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractEffectRenderer implements IEffectWidget, IHasRenderAreas {
    protected static final ResourceLocation EFFECT_BACKGROUND = new ResourceLocation(StylishEffects.MODID,"textures/gui/mob_effect_background.png");

    private final EffectRendererType type;
    private AbstractGui screen;
    private int availableWidth;
    private int availableHeight;
    private int startX;
    private int startY;
    private ClientConfig.ScreenSide screenSide;
    protected List<EffectInstance> activeEffects;

    public AbstractEffectRenderer(EffectRendererType type) {
        this.type = type;
    }

    public void setScreenDimensions(AbstractGui screen, int availableWidth, int availableHeight, int startX, int startY, ClientConfig.ScreenSide screenSide) {
        this.screen = screen;
        this.availableWidth = availableWidth;
        this.availableHeight = availableHeight;
        this.startX = startX;
        this.startY = startY;
        this.screenSide = screenSide;
        if (this.type == EffectRendererType.HUD) {
            this.screenSide = this.screenSide.inverse();
            this.availableWidth -= ((ClientConfig.HudRendererConfig) this.config()).offsetX;
            this.availableHeight -= ((ClientConfig.HudRendererConfig) this.config()).offsetY;
            this.startX += (this.screenSide.right() ? 1 : -1) * ((ClientConfig.HudRendererConfig) this.config()).offsetX;
            this.startY += ((ClientConfig.HudRendererConfig) this.config()).offsetY;
        }
    }

    public final void setActiveEffects(Collection<EffectInstance> activeEffects) {
        if (activeEffects.isEmpty()) {
            this.activeEffects = null;
            return;
        }
        this.activeEffects = activeEffects.stream()
                .filter(e -> !this.config().respectHideParticles || e.showIcon())
                .filter(this.type::test)
                .sorted()
                .collect(Collectors.toList());
    }

    public final boolean isActive() {
        return this.activeEffects != null && !this.activeEffects.isEmpty();
    }

    @Override
    public List<Rectangle2d> getRenderAreas() {
        if (this.isActive()) {
            return this.getEffectPositions(this.activeEffects).stream()
                    .map(Pair::getValue)
                    .map(pos -> new Rectangle2d(pos[0], pos[1], this.getWidth(), this.getHeight()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public abstract List<Pair<EffectInstance, int[]>> getEffectPositions(List<EffectInstance> activeEffects);

    protected abstract int getTopOffset();

    protected int[] coordsToEffectPosition(int coordX, int coordY) {
        int[] renderPositions = new int[2];
        switch (this.screenSide) {
            case LEFT:
                renderPositions[0] = this.startX - (this.getWidth() + 1) - (this.getWidth() + this.config().widgetSpace) * coordX;
                renderPositions[1] = this.startY + this.getTopOffset() + this.getAdjustedHeight() * coordY;
                break;
            case RIGHT:
                renderPositions[0] = this.startX + 1 + (this.getWidth() + this.config().widgetSpace) * coordX;
                renderPositions[1] = this.startY + this.getTopOffset() + this.getAdjustedHeight() * coordY;
                break;
            default:
                throw new IllegalStateException("unreachable statement");
        }
        return renderPositions;
    }

    public void renderEffects(MatrixStack matrixStack, Minecraft minecraft) {
        for (Pair<EffectInstance, int[]> entry : this.getEffectPositions(this.activeEffects)) {
            this.renderWidget(matrixStack, entry.getValue()[0], entry.getValue()[1], minecraft, entry.getKey());
        }
    }

    protected float getBlinkingAlpha(EffectInstance effectinstance) {
        if (!effectinstance.isAmbient() && effectinstance.getDuration() <= 200) {
            int duration = 10 - effectinstance.getDuration() / 20;
            return MathHelper.clamp((float) effectinstance.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + MathHelper.cos((float) effectinstance.getDuration() * (float)Math.PI / 5.0F) * MathHelper.clamp((float) duration / 10.0F * 0.25F, 0.0F, 0.25F);
        }
        return 1.0F;
    }

    private int getAvailableWidth() {
        return Math.min(this.availableWidth, this.config().maxColumns * (this.getWidth() + this.config().widgetSpace));
    }

    private int getAvailableHeight() {
        return Math.min(this.availableHeight, this.config().maxRows * (this.getHeight() + this.config().widgetSpace));
    }

    public int getMaxColumns() {
        return MathHelper.clamp(this.getAvailableWidth() / (this.getWidth() + this.config().widgetSpace), 1, this.config().maxColumns);
    }

    private int getAdjustedHeight() {
        if (this.config().overflowMode == ClientConfig.OverflowMode.CONDENSE && this.getRows() > this.getMaxRows()) {
            return (this.getAvailableHeight() - this.getHeight()) / Math.max(1, this.getRows() - 1);
        }
        return this.getHeight() + this.config().widgetSpace;
    }

    public int getMaxRows() {
        return MathHelper.clamp(this.getAvailableHeight() / (this.getHeight() + this.config().widgetSpace), 1, this.config().maxRows);
    }

    public int getRows() {
        return this.splitByColumns(this.activeEffects.size());
    }

    protected int splitByColumns(int amountToSplit) {
        return (int) Math.ceil(amountToSplit / (float) this.getMaxColumns());
    }

    protected ClientConfig.EffectRendererConfig config() {
        switch (this.type) {
            case INVENTORY:
                return StylishEffects.CONFIG.client().inventoryRenderer();
            case HUD:
                return StylishEffects.CONFIG.client().hudRenderer();
        }
        throw new IllegalStateException("unreachable statement");
    }

    protected void drawCustomEffect(MatrixStack matrixStack, int posX, int posY, EffectInstance effectinstance) {
        // we make it possible to display effects on any container screen, so this is sometimes unusable
        if (this.screen instanceof DisplayEffectsScreen) {
            effectinstance.renderInventoryEffect((DisplayEffectsScreen<?>) this.screen, matrixStack, posX, posY, this.screen.getBlitOffset());
        } else if (this.screen instanceof IngameGui) {
            effectinstance.renderHUDEffect(this.screen, matrixStack, posX, posY, this.screen.getBlitOffset(), this.getBlinkingAlpha(effectinstance) * this.config().widgetAlpha);
        }
    }

    protected Optional<IFormattableTextComponent> getEffectDuration(EffectInstance effectInstance, ClientConfig.LongDurationString longDurationString) {
        String effectDuration = EffectUtils.formatDuration(effectInstance, 1.0F);
        if (effectDuration.equals("**:**")) {
            switch (longDurationString) {
                case INFINITY:
                    // infinity char
                    return Optional.of(new StringTextComponent("\u221e"));
                case NONE:
                    return Optional.empty();
                case VANILLA:
            }
        }
        return Optional.of(new StringTextComponent(effectDuration));
    }

    public Optional<List<ITextComponent>> getHoveredEffectTooltip(int mouseX, int mouseY) {
        if (this.type == EffectRendererType.INVENTORY && StylishEffects.CONFIG.client().inventoryRenderer().hoveringTooltip) {
            return this.getHoveredEffect(mouseX, mouseY)
                    .map(effect -> this.makeEffectTooltip(effect, StylishEffects.CONFIG.client().inventoryRenderer().tooltipDuration));
        }
        return Optional.empty();
    }

    public Optional<EffectInstance> getHoveredEffect(int mouseX, int mouseY) {
        for (Map.Entry<EffectInstance, int[]> entry : Lists.reverse(this.getEffectPositions(this.activeEffects))) {
            if (this.isMouseOver(entry.getValue()[0], entry.getValue()[1], mouseX, mouseY)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    private boolean isMouseOver(int posX, int posY, int mouseX, int mouseY) {
        return mouseX >= posX && mouseX <= posX + this.getWidth() && mouseY >= posY && mouseY <= posY + this.getHeight();
    }

    protected List<ITextComponent> makeEffectTooltip(EffectInstance effectInstance, boolean withDuration) {
        List<ITextComponent> tooltip = Lists.newArrayList();
        String potionName = effectInstance.getEffect().getDescriptionId();
        IFormattableTextComponent textComponent = new TranslationTextComponent(potionName);
        if (effectInstance.getAmplifier() >= 1 && effectInstance.getAmplifier() <= 9) {
            textComponent.append(" ").append(new TranslationTextComponent("enchantment.level." + (effectInstance.getAmplifier() + 1)));
        }
        // description may be provided by Potion Descriptions mod
        String descriptionKey = "description." + potionName;
        if (LanguageMap.getInstance().has(descriptionKey)) {
            if (withDuration) {
                // inline duration when there is a description
                textComponent.append(" ").append(new StringTextComponent("(").append(EffectUtils.formatDuration(effectInstance, 1.0F)).append(")").withStyle(TextFormatting.GRAY));
            }
            tooltip.add(textComponent);
            tooltip.add(new TranslationTextComponent(descriptionKey).withStyle(TextFormatting.GRAY));
        } else {
            tooltip.add(textComponent);
            if (withDuration) {
                tooltip.add(new StringTextComponent(EffectUtils.formatDuration(effectInstance, 1.0F)).withStyle(TextFormatting.GRAY));
            }
        }
        return tooltip;
    }

    public enum EffectRendererType {
        INVENTORY(IForgeEffectInstance::shouldRender),
        HUD(EffectInstance::shouldRenderHUD);

        private final Predicate<EffectInstance> filter;

        EffectRendererType(Predicate<EffectInstance> filter) {
            this.filter = filter;
        }

        public boolean test(EffectInstance effectInstance) {
            return this.filter.test(effectInstance);
        }
    }
}
