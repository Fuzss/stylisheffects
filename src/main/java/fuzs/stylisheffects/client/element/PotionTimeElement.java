package fuzs.stylisheffects.client.element;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.puzzleslib.config.option.OptionsBuilder;
import fuzs.puzzleslib.element.AbstractElement;
import fuzs.puzzleslib.element.side.IClientElement;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.mixin.client.accessor.DisplayEffectsScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.*;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public class PotionTimeElement extends AbstractElement implements IClientElement {

    private static final ResourceLocation EFFECT_BACKGROUND = new ResourceLocation(StylishEffects.MODID,"textures/gui/mob_effect_background.png");
    private static final ResourceLocation TINY_NUMBERS_TEXTURE = new ResourceLocation(StylishEffects.MODID,"textures/font/tiny_numbers.png");
    private static final String EFFECT_FORMATTING = "EFFECT";

    private final Minecraft mc = Minecraft.getInstance();
    private final TextFormatting defaultAmplifierColor = TextFormatting.WHITE;

    private boolean showEverywhere;
    private int inventoryMaxWidth;
    private int inventoryMaxHeight;
    private boolean stylizeName;
    private boolean showDuration;
    private TextFormatting durationColor;
    private TextFormatting amplifierColor;
    private boolean coloredFrame;
    private float widgetAlpha;

    @Override
    public void constructClient() {

        this.addListener(this::onPotionShift);
        this.addListener(this::onInitGuiPost);
        this.addListener(this::onDrawScreenPost);
        this.addListener(this::onRenderGameOverlayPre);
        this.addListener(this::onRenderGameOverlayText);
    }

    @Override
    public String[] getDescription() {

        return new String[]{"Add remaining duration to potion icons shown in-game."};
    }

    @Override
    protected boolean isPersistent() {

        return true;
    }

    @Override
    public void setupClientConfig(OptionsBuilder builder) {

        builder.define("Show Everywhere", true).comment("Render active status effects in every container, not just in the player inventory.").sync(v -> this.showEverywhere = v);
        builder.define("Inventory Max Width", 255).min(1).max(255).comment("Maximum amount of status effects rendered in a single row inside the inventory.").sync(v -> this.inventoryMaxWidth = v);
        builder.define("Inventory Max Height", 255).min(1).max(255).comment("Maximum amount of status effects rendered in a single column inside the inventory.").sync(v -> this.inventoryMaxHeight = v);
        builder.push("widget");
        builder.define("Colored Frame", false).comment("Color widget frame depending on effect type.").sync(v -> this.coloredFrame = v);
        builder.define("Duration Color", EFFECT_FORMATTING).comment("Effect duration color on widget. \"EFFECT\" value will use native effect color.", "Allowed Values: " + Stream.concat(Stream.of(EFFECT_FORMATTING), Stream.of(TextFormatting.values()).filter(TextFormatting::isColor).map(Enum::name)).collect(Collectors.joining(", "))).sync(v -> deserializeEffectFormatting(v, null, color -> this.durationColor = color));
        builder.define("Amplifier Color", this.defaultAmplifierColor.name()).comment("Effect amplifier color on widget. \"EFFECT\" value will use native effect color.", "Allowed Values: " + Stream.concat(Stream.of(EFFECT_FORMATTING), Stream.of(TextFormatting.values()).filter(TextFormatting::isColor).map(Enum::name)).collect(Collectors.joining(", "))).sync(v -> deserializeEffectFormatting(v, this.defaultAmplifierColor, color -> this.amplifierColor = color));
        builder.define("Widget Alpha", 1.0).min(0.0).max(1.0).comment("Alpha value for widget.").sync(v -> this.widgetAlpha = v.floatValue());
        builder.pop();
        builder.push("tooltip");
        builder.define("Stylize Name", false).comment("Change effect name color depending on effect type.").sync(v -> this.stylizeName = v);
        builder.define("Show Duration", false).comment("Render remaining effect duration on tooltip.").sync(v -> this.showDuration = v);
        builder.pop();
    }

    private static void deserializeEffectFormatting(String value, TextFormatting defaultColor, Consumer<TextFormatting> setColor) {

        if (value.equals(EFFECT_FORMATTING)) {

            setColor.accept(null);
            return;
        }

        TextFormatting textColor = TextFormatting.getByName(value);
        if (textColor != null && textColor.isColor()) {

            setColor.accept(textColor);
        } else {

            StylishEffects.LOGGER.warn("Not a text color: {}", value);
        }

        setColor.accept(defaultColor);
    }

    private void onPotionShift(final GuiScreenEvent.PotionShiftEvent evt) {

        evt.setCanceled(true);
    }

    private void onInitGuiPost(final GuiScreenEvent.InitGuiEvent.Post evt) {

        if (evt.getGui() instanceof DisplayEffectsScreen) {

            // disable vanilla rendering in creative mode inventory, survival inventory has to be disabled via mixin
            // this is not needed by us, we just check before rendering as survival inventory does
            ((DisplayEffectsScreenAccessor) evt.getGui()).setDoRenderEffects(false);
        }
    }

    private void onDrawScreenPost(final GuiScreenEvent.DrawScreenEvent.Post evt) {

        if ((evt.getGui() instanceof DisplayEffectsScreen || this.showEverywhere && evt.getGui() instanceof ContainerScreen) && (!(evt.getGui() instanceof IRecipeShownListener) || !((IRecipeShownListener) evt.getGui()).getRecipeBookComponent().isVisible())) {

            int guiLeft = ((ContainerScreen<?>) evt.getGui()).getGuiLeft();
            this.drawPotionIcons(evt.getMatrixStack(), guiLeft, ((ContainerScreen<?>) evt.getGui()).getGuiTop(), evt.getMouseX(), evt.getMouseY(), Math.min(Math.max(1, guiLeft / 30), this.inventoryMaxWidth)).ifPresent(effectInstance -> {

                if (effectInstance.shouldRenderInvText()) {

                    String potionName = effectInstance.getEffect().getDescriptionId();
                    IFormattableTextComponent textComponent = new TranslationTextComponent(potionName);
                    if (effectInstance.getAmplifier() >= 1 && effectInstance.getAmplifier() <= 9) {

                        textComponent.append(" ").append(new TranslationTextComponent("enchantment.level." + (effectInstance.getAmplifier() + 1)));
                    }

                    if (this.stylizeName) {

                        if (effectInstance.isAmbient()) {

                            textComponent.withStyle(TextFormatting.AQUA);
                        } else if (effectInstance.getEffect().isBeneficial()) {

                            textComponent.withStyle(TextFormatting.BLUE);
                        } else {

                            textComponent.withStyle(TextFormatting.RED);
                        }
                    }

                    List<ITextComponent> list = Lists.newArrayList(textComponent);
                    if (this.showDuration) {

                        String effectDuration = getEffectDuration(effectInstance, 1.0F);
                        list.add(new StringTextComponent(effectDuration).withStyle(TextFormatting.GRAY));
                    }

                    // description may be provided by Potion Descriptions mod
                    String descriptionKey = "description." + potionName;
                    if (LanguageMap.getInstance().has(descriptionKey)) {

                        list.add(new TranslationTextComponent(descriptionKey).withStyle(TextFormatting.GRAY));
                    }

                    evt.getGui().renderComponentTooltip(evt.getMatrixStack(), list, evt.getMouseX(), evt.getMouseY());
                }
            });
        }
    }

    private void onRenderGameOverlayPre(final RenderGameOverlayEvent.Pre evt) {

        if (evt.getType() != ElementType.POTION_ICONS) {

            return;
        }

        evt.setCanceled(true);
    }

    private void onRenderGameOverlayText(final RenderGameOverlayEvent.Text evt) {

        // use this event so potion icons are drawn behind the debug menu like in vanilla
        this.drawPotionIcons(evt.getMatrixStack(), evt.getWindow().getGuiScaledWidth(), 1, -1, -1, -1);
    }

    private Optional<EffectInstance> drawPotionIcons(MatrixStack matrixStack, int x, int y, int mouseX, int mouseY, final int maxWidth) {

        assert this.mc.player != null;
        Collection<EffectInstance> activePotionEffects = this.mc.player.getActiveEffects();
        Optional<EffectInstance> hoveredEffect = Optional.empty();
        if (!activePotionEffects.isEmpty()) {

            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            final int beneficialRows = (int) Math.ceil((double) activePotionEffects.stream()
                    .map(EffectInstance::getEffect)
                    .filter(Effect::isBeneficial)
                    .count() / maxWidth);
            int beneficialCounter = 0;
            int harmfulCounter = 0;
            PotionSpriteUploader potionspriteuploader = this.mc.getMobEffectTextures();
            List<Runnable> effects = Lists.newArrayListWithExpectedSize(activePotionEffects.size());
            for (EffectInstance effectinstance : Ordering.natural().reverse().sortedCopy(activePotionEffects)) {

                if (beneficialCounter / maxWidth + harmfulCounter / maxWidth >= this.inventoryMaxHeight) {

                    break;
                }

                // Rebind in case previous renderHUDEffect changed texture
                this.mc.getTextureManager().bind(EFFECT_BACKGROUND);
                if (maxWidth != -1 || effectinstance.shouldRenderHUD() && effectinstance.showIcon()) {

                    Effect effect = effectinstance.getEffect();
                    int width = x;
                    int height = y;
                    if (this.mc.isDemo()) {

                        // TODO not in inventory
                        height += 15;
                    }

                    if (effect.isBeneficial()) {

                        if (maxWidth != -1) {

                            height += 25 * (beneficialCounter / maxWidth);
                        }

                        beneficialCounter++;
                        width -= 30 * (maxWidth != -1 ? (beneficialCounter - 1) % maxWidth + 1 : beneficialCounter);
                    } else {

                        height += maxWidth != -1 ? 25 * beneficialRows + 1 : 26;
                        if (maxWidth != -1) {

                            height += 25 * (harmfulCounter / maxWidth);
                        }

                        harmfulCounter++;
                        width -= 30 * (maxWidth != -1 ? (harmfulCounter - 1) % maxWidth + 1 : harmfulCounter);
                    }

                    float blinkingAlpha = this.widgetAlpha;
                    int colorOffset = 0;
                    if (effectinstance.isAmbient()) {

                        colorOffset = 1;
                    } else if (this.coloredFrame) {

                        colorOffset = effect.isBeneficial() ? 2 : 3;
                    }

                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.widgetAlpha);
                    AbstractGui.blit(matrixStack, width, height, colorOffset * 29, 0, 29, 24, 256, 256);
                    if (!effectinstance.isAmbient() && effectinstance.getDuration() <= 200) {

                        int duration = 10 - effectinstance.getDuration() / 20;
                        blinkingAlpha *= MathHelper.clamp((float) effectinstance.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + MathHelper.cos((float) effectinstance.getDuration() * (float)Math.PI / 5.0F) * MathHelper.clamp((float) duration / 10.0F * 0.25F, 0.0F, 0.25F);
                    }

                    if (mouseX >= width && mouseX <= width + 30 && mouseY > height && mouseY <= height + 26) {

                        hoveredEffect = Optional.of(effectinstance);
                    }

                    TextureAtlasSprite textureatlassprite = potionspriteuploader.get(effect);
                    effects.add(this.getEffectRenderer(matrixStack, effectinstance, textureatlassprite, width, height, blinkingAlpha));
                    effectinstance.renderHUDEffect(this.mc.gui, matrixStack, width, height, this.mc.gui.getBlitOffset(), blinkingAlpha);
                }
            }

            effects.forEach(Runnable::run);
            RenderSystem.enableDepthTest();
        }

        return hoveredEffect;
    }

    private Runnable getEffectRenderer(MatrixStack matrixStack, EffectInstance effectinstance, TextureAtlasSprite textureatlassprite, int width, int height, float alpha) {

        return () -> {

            RenderSystem.enableBlend();
            this.mc.getTextureManager().bind(textureatlassprite.atlas().location());
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
            AbstractGui.blit(matrixStack, width + 5, height + (effectinstance.isAmbient() ? 3 : 2), this.mc.gui.getBlitOffset(), 18, 18, textureatlassprite);
            if (effectinstance.getAmplifier() >= 1 && effectinstance.getAmplifier() <= 9) {

                this.mc.getTextureManager().bind(TINY_NUMBERS_TEXTURE);
                int potionColor = getEffectColor(this.amplifierColor, effectinstance);
                float r = (potionColor >> 16 & 255) / 255.0F;
                float g = (potionColor >> 8 & 255) / 255.0F;
                float b = (potionColor >> 0 & 255) / 255.0F;
                RenderSystem.color4f(r * 0.25F, g * 0.25F, b * 0.25F, this.widgetAlpha);
                AbstractGui.blit(matrixStack, width + 24, height + 3, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
                RenderSystem.color4f(r, g, b, this.widgetAlpha);
                AbstractGui.blit(matrixStack, width + 23, height + 2, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
            }

            if (!effectinstance.isAmbient()) {

                StringTextComponent durationComponent = new StringTextComponent(getEffectDuration(effectinstance, 1.0F));
                int potionColor = getEffectColor(this.durationColor, effectinstance);
                AbstractGui.drawCenteredString(matrixStack, this.mc.font, durationComponent, width + 15, height + 14, (int) (this.widgetAlpha * 255.0F) << 24 | potionColor);
            }
        };
    }
    
    @SuppressWarnings("PointlessBitwiseExpression")
    private static int isColorTooDark(int potionColor) {

        int r = potionColor >> 16 & 255;
        int g = potionColor >> 8 & 255;
        int b = potionColor >> 0 & 255;

        return r + g + b < 128 ? 8355711 : potionColor;
    }

    @SuppressWarnings("SameParameterValue")
    private static String getEffectDuration(EffectInstance effectInstance, float partialTicks) {

        String effectDuration = EffectUtils.formatDuration(effectInstance, partialTicks);
        if (effectDuration.equals("**:**")) {

            // infinity char
            effectDuration = "\u221e";
        }

        return effectDuration;
    }

    @SuppressWarnings("ConstantConditions")
    private static int getEffectColor(TextFormatting color, EffectInstance effectinstance) {

        if (color != null) {

            return color.getColor();
        }

        return isColorTooDark(PotionUtils.getColor(Lists.newArrayList(effectinstance)));
    }

}
