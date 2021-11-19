package fuzs.stylisheffects.client.gui.effects;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraftforge.client.EffectRenderer;
import net.minecraftforge.client.RenderProperties;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public abstract class AbstractEffectRenderer implements IEffectWidget, IHasRenderAreas {
    protected static final ResourceLocation EFFECT_BACKGROUND = new ResourceLocation(StylishEffects.MODID,"textures/gui/mob_effect_background.png");

    private final EffectRendererType type;

    private GuiComponent screen;
    private int availableWidth;
    private int availableHeight;
    private int startX;
    private int startY;
    private ClientConfig.ScreenSide screenSide;
    protected List<MobEffectInstance> activeEffects;

    public AbstractEffectRenderer(EffectRendererType type) {
        this.type = type;
    }

    public void setScreenDimensions(GuiComponent screen, int availableWidth, int availableHeight, int startX, int startY, ClientConfig.ScreenSide screenSide) {
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

    public final void setActiveEffects(Collection<MobEffectInstance> activeEffects) {
        if (activeEffects.isEmpty()) throw new IllegalArgumentException("Rendering empty effects list not supported");
        this.activeEffects = activeEffects.stream()
                .filter(this.type::test)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<Rect2i> getRenderAreas() {
        if (this.activeEffects != null) {
            return this.getEffectPositions(this.activeEffects).stream()
                    .map(Pair::getValue)
                    .map(pos -> new Rect2i(pos[0], pos[1], this.getWidth(), this.getHeight()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public abstract List<Pair<MobEffectInstance, int[]>> getEffectPositions(List<MobEffectInstance> activeEffects);

    protected abstract int getTopOffset();

    protected int[] coordsToEffectPosition(int coordX, int coordY) {
        int[] renderPositions = new int[2];
        switch (this.screenSide) {
            case LEFT -> {
                renderPositions[0] = this.startX - (this.getWidth() + 1) - (this.getWidth() + this.config().widgetSpace) * coordX;
                renderPositions[1] = this.startY + this.getTopOffset() + this.getAdjustedHeight() * coordY;
            }
            case RIGHT -> {
                renderPositions[0] = this.startX + 1 + (this.getWidth() + this.config().widgetSpace) * coordX;
                renderPositions[1] = this.startY + this.getTopOffset() + this.getAdjustedHeight() * coordY;
            }
        }
        return renderPositions;
    }

    public void renderEffects(PoseStack matrixStack, Minecraft minecraft) {
        for (Pair<MobEffectInstance, int[]> entry : this.getEffectPositions(this.activeEffects)) {
            this.renderWidget(matrixStack, entry.getValue()[0], entry.getValue()[1], minecraft, entry.getKey());
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
        return Math.min(this.availableWidth, this.config().maxColumns * (this.getWidth() + this.config().widgetSpace));
    }

    private int getAvailableHeight() {
        return Math.min(this.availableHeight, this.config().maxRows * (this.getHeight() + this.config().widgetSpace));
    }

    public int getMaxColumns() {
        return Mth.clamp(this.getAvailableWidth() / (this.getWidth() + this.config().widgetSpace), 1, this.config().maxColumns);
    }

    private int getAdjustedHeight() {
        if (this.config().overflowMode == ClientConfig.OverflowMode.CONDENSE && this.getRows() > this.getMaxRows()) {
            return (this.getAvailableHeight() - this.getHeight()) / Math.max(1, this.getRows() - 1);
        }
        return this.getHeight() + this.config().widgetSpace;
    }

    public int getMaxRows() {
        return Mth.clamp(this.getAvailableHeight() / (this.getHeight() + this.config().widgetSpace), 1, this.config().maxRows);
    }

    public int getRows() {
        return this.splitByColumns(this.activeEffects.size());
    }

    protected int splitByColumns(int amountToSplit) {
        return (int) Math.ceil(amountToSplit / (float) this.getMaxColumns());
    }

    protected ClientConfig.EffectRendererConfig config() {
        return switch (this.type) {
            case INVENTORY -> StylishEffects.CONFIG.client().inventoryRenderer();
            case HUD -> StylishEffects.CONFIG.client().hudRenderer();
        };
    }

    protected void drawCustomEffect(PoseStack matrixStack, int posX, int posY, MobEffectInstance effectinstance) {
        // we make it possible to display effects on any container screen, so this is sometimes unusable
        final EffectRenderer effectRenderer = RenderProperties.getEffectRenderer(effectinstance);
        if (this.screen instanceof EffectRenderingInventoryScreen) {
            effectRenderer.renderInventoryEffect(effectinstance, (EffectRenderingInventoryScreen<?>) this.screen, matrixStack, posX, posY, this.screen.getBlitOffset());
        } else if (this.screen instanceof Gui) {
            effectRenderer.renderHUDEffect(effectinstance, this.screen, matrixStack, posX, posY, this.screen.getBlitOffset(), this.getBlinkingAlpha(effectinstance) * this.config().widgetAlpha);
        }
    }

    protected Optional<MutableComponent> getEffectDuration(MobEffectInstance effectInstance, ClientConfig.LongDurationString longDurationString) {
        String effectDuration = MobEffectUtil.formatDuration(effectInstance, 1.0F);
        if (effectDuration.equals("**:**")) {
            switch (longDurationString) {
                case INFINITY:
                    // infinity char
                    return Optional.of(new TextComponent("\u221e"));
                case NONE:
                    return Optional.empty();
                case VANILLA:
            }
        }
        return Optional.of(new TextComponent(effectDuration));
    }

    public Optional<List<Component>> getHoveredEffectTooltip(int mouseX, int mouseY) {
        if (this.type == EffectRendererType.INVENTORY && StylishEffects.CONFIG.client().inventoryRenderer().hoveringTooltip) {
            return this.getHoveredEffect(mouseX, mouseY)
                    .map(effect -> this.makeEffectTooltip(effect, StylishEffects.CONFIG.client().inventoryRenderer().tooltipDuration));
        }
        return Optional.empty();
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
        return mouseX >= posX && mouseX <= posX + this.getWidth() && mouseY >= posY && mouseY <= posY + this.getHeight();
    }

    protected List<Component> makeEffectTooltip(MobEffectInstance effectInstance, boolean withDuration) {
        List<Component> tooltip = Lists.newArrayList();
        String potionName = effectInstance.getEffect().getDescriptionId();
        MutableComponent textComponent = new TranslatableComponent(potionName);
        if (effectInstance.getAmplifier() >= 1 && effectInstance.getAmplifier() <= 9) {
            textComponent.append(" ").append(new TranslatableComponent("enchantment.level." + (effectInstance.getAmplifier() + 1)));
        }
        // description may be provided by Potion Descriptions mod
        String descriptionKey = "description." + potionName;
        if (Language.getInstance().has(descriptionKey)) {
            if (withDuration) {
                // inline duration when there is a description
                textComponent.append(" ").append(new TextComponent("(").append(MobEffectUtil.formatDuration(effectInstance, 1.0F)).append(")").withStyle(ChatFormatting.GRAY));
            }
            tooltip.add(textComponent);
            tooltip.add(new TranslatableComponent(descriptionKey).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(textComponent);
            if (withDuration) {
                tooltip.add(new TextComponent(MobEffectUtil.formatDuration(effectInstance, 1.0F)).withStyle(ChatFormatting.GRAY));
            }
        }
        return tooltip;
    }

    public enum EffectRendererType {
        INVENTORY(EffectRenderer::shouldRender),
        HUD(EffectRenderer::shouldRenderHUD);

        private final BiPredicate<EffectRenderer, MobEffectInstance> filter;

        EffectRendererType(BiPredicate<EffectRenderer, MobEffectInstance> filter) {
            this.filter = filter;
        }

        public boolean test(MobEffectInstance effectInstance) {
            final EffectRenderer effectRenderer = RenderProperties.getEffectRenderer(effectInstance);
            return this.filter.test(effectRenderer, effectInstance);
        }
    }
}
