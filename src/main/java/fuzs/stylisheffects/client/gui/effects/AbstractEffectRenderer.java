package fuzs.stylisheffects.client.gui.effects;

import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.config.ClientConfig;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractEffectRenderer implements IEffectPlaque {
    protected static final ResourceLocation EFFECT_BACKGROUND = new ResourceLocation(StylishEffects.MODID,"textures/gui/mob_effect_background.png");

    protected final int plaqueGap = 1;
    private final ClientConfig.EffectConfig config;

    protected int screenWidth;
    protected int screenHeight;
    protected int startX;
    protected int startY;
    protected ClientConfig.ScreenSide screenSide;
    protected Collection<EffectInstance> activeEffects;

    protected AbstractEffectRenderer(ClientConfig.EffectConfig config) {
        this.config = config;
    }

    public void setScreenDimensions(int screenWidth, int screenHeight, int startX, int startY, ClientConfig.ScreenSide screenSide) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.startX = startX;
        this.startY = startY;
        this.screenSide = screenSide;
    }

    public void setActiveEffects(Collection<EffectInstance> activeEffects) {
        if (activeEffects.isEmpty()) throw new IllegalArgumentException("Rendering empty effects list not supported");
        this.activeEffects = activeEffects;
    }

    public List<Rectangle2d> getEffectAreas() {
        return this.activeEffects.stream()
                .map(EffectInstance::getEffect)
                .map(Effect::isBeneficial)
                .map(this::getEffectPosition2)
                .map(pos -> new Rectangle2d(pos[0], pos[1], this.getPlaqueWidth(), this.getPlaqueHeight()))
                .collect(Collectors.toList());
    }

    public abstract int[] getEffectPosition(boolean isBeneficial);

    public int[] getEffectPosition2(boolean isBeneficial) {
        final int[] effectPosition = this.getEffectPosition(isBeneficial);
        int[] renderPositions = new int[2];
        switch (this.screenSide) {
            case LEFT:
                renderPositions[0] = this.startX - (this.getPlaqueWidth() + this.plaqueGap) * (effectPosition[0] + 1);
                renderPositions[1] = this.startY + (this.getPlaqueHeight() + this.plaqueGap) * effectPosition[1];
                break;
            case RIGHT:
                renderPositions[0] = this.startX + this.plaqueGap + (this.getPlaqueWidth() + this.plaqueGap) * effectPosition[0];
                renderPositions[1] = this.startY + (this.getPlaqueHeight() + this.plaqueGap) * effectPosition[1];
                break;
        }
        return renderPositions;
    }

    protected float getBlinkingAlpha(EffectInstance effectinstance) {
        if (this.config().blinkingAlpha) {
            if (!effectinstance.isAmbient() && effectinstance.getDuration() <= 200) {
                int duration = 10 - effectinstance.getDuration() / 20;
                return MathHelper.clamp((float) effectinstance.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + MathHelper.cos((float) effectinstance.getDuration() * (float)Math.PI / 5.0F) * MathHelper.clamp((float) duration / 10.0F * 0.25F, 0.0F, 0.25F);
            }
        }
        return 1.0F;
    }

    protected final ClientConfig.EffectConfig config() {
        return this.config;
    }

    public abstract int getMaxHorizontalEffects();

    public int getMaxVerticalEffects() {
        return this.screenHeight / (this.getPlaqueHeight() + this.plaqueGap);
    }

    private Optional<IFormattableTextComponent> getEffectDuration(EffectInstance effectInstance) {
        String effectDuration = EffectUtils.formatDuration(effectInstance, 1.0F);
        if (effectDuration.equals("**:**")) {
            switch (this.config().longDurationString) {
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
}
