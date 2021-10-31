package fuzs.stylisheffects.client.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.stylisheffects.StylishEffects;
import fuzs.stylisheffects.config.ClientConfig;
import fuzs.stylisheffects.mixin.client.accessor.DisplayEffectsScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.*;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("deprecation")
public class EffectScreenHandler {

    private static final ResourceLocation EFFECT_BACKGROUND = new ResourceLocation(StylishEffects.MODID,"textures/gui/mob_effect_background.png");
    private static final ResourceLocation TINY_NUMBERS_TEXTURE = new ResourceLocation(StylishEffects.MODID,"textures/font/tiny_numbers.png");

    private final Minecraft mc = Minecraft.getInstance();

    @SubscribeEvent
    public void onRenderGameOverlayPre(final RenderGameOverlayEvent.Pre evt) {
        if (evt.getType() == ElementType.POTION_ICONS) evt.setCanceled(true);
    }

    @SubscribeEvent
    public void onPotionShift(final GuiScreenEvent.PotionShiftEvent evt) {
        evt.setCanceled(true);
    }

    @SubscribeEvent
    public void onInitGuiPost(final GuiScreenEvent.InitGuiEvent.Post evt) {
        if (evt.getGui() instanceof DisplayEffectsScreen) {
            // disable vanilla rendering in creative mode inventory, survival inventory has to be disabled separately
            // this is not needed by us, we just check before rendering as survival inventory does
            ((DisplayEffectsScreenAccessor) evt.getGui()).setDoRenderEffects(false);
        }
    }

    @SubscribeEvent
    public void onDrawBackground(final GuiContainerEvent.DrawBackground evt) {
        if (evt.getGuiContainer() instanceof InventoryScreen) {
            ((DisplayEffectsScreenAccessor) evt.getGuiContainer()).setDoRenderEffects(false);
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlayText(final RenderGameOverlayEvent.Text evt) {
        // use this event so potion icons are drawn behind debug menu as in vanilla
        this.drawPotionIcons(evt.getMatrixStack(), evt.getWindow().getGuiScaledWidth(), 1, -1, -1, -1);
    }

    @SubscribeEvent
    public void onDrawScreenPost(final GuiScreenEvent.DrawScreenEvent.Post evt) {
        if ((evt.getGui() instanceof DisplayEffectsScreen || StylishEffects.CONFIG.client().generalConfig().effectsEverywhere && evt.getGui() instanceof ContainerScreen) && (!(evt.getGui() instanceof IRecipeShownListener) || !((IRecipeShownListener) evt.getGui()).getRecipeBookComponent().isVisible())) {
            int guiLeft = ((ContainerScreen<?>) evt.getGui()).getGuiLeft();
            this.drawPotionIcons(evt.getMatrixStack(), guiLeft, ((ContainerScreen<?>) evt.getGui()).getGuiTop(), evt.getMouseX(), evt.getMouseY(), Math.min(Math.max(1, guiLeft / 30), StylishEffects.CONFIG.client().inventoryEffects().maxWidth)).ifPresent(effectInstance -> {
                this.drawPlaqueTooltip(evt.getMatrixStack(), evt.getMouseX(), evt.getMouseY(), evt.getGui(), effectInstance);
            });
        }
    }

    private void drawPlaqueTooltip(MatrixStack matrixStack, int mouseX, int mouseY, Screen screen, EffectInstance effectInstance) {
        if (StylishEffects.CONFIG.client().inventoryEffects().hoveringTooltip) {
            List<ITextComponent> tooltip = Lists.newArrayList();
            String potionName = effectInstance.getEffect().getDescriptionId();
            IFormattableTextComponent textComponent = new TranslationTextComponent(potionName);
            if (effectInstance.getAmplifier() >= 1 && effectInstance.getAmplifier() <= 9) {
                textComponent.append(" ").append(new TranslationTextComponent("enchantment.level." + (effectInstance.getAmplifier() + 1)));
            }
            tooltip.add(textComponent);
            if (StylishEffects.CONFIG.client().inventoryEffects().tooltipDuration) {
                getEffectDuration(effectInstance).ifPresent(effectDuration -> {
                    tooltip.add(effectDuration.withStyle(TextFormatting.GRAY));
                });
            }
            // description may be provided by Potion Descriptions mod
            String descriptionKey = "description." + potionName;
            if (LanguageMap.getInstance().has(descriptionKey)) {
                tooltip.add(new TranslationTextComponent(descriptionKey).withStyle(TextFormatting.GRAY));
            }
            screen.renderComponentTooltip(matrixStack, tooltip, mouseX, mouseY);
        }
    }

    private Optional<EffectInstance> drawPotionIcons(MatrixStack matrixStack, int posX, int posY, int mouseX, int mouseY, final int maxWidth) {

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

                if (beneficialCounter / maxWidth + harmfulCounter / maxWidth >= StylishEffects.CONFIG.client().inventoryEffects().maxHeight) {

                    break;
                }

                // Rebind in case previous renderHUDEffect changed texture
                this.mc.getTextureManager().bind(EFFECT_BACKGROUND);
                if (maxWidth != -1 || effectinstance.shouldRenderHUD() && effectinstance.showIcon()) {

                    Effect effect = effectinstance.getEffect();
                    if (effect.isBeneficial()) {

                        if (maxWidth != -1) {

                            posY += 25 * (beneficialCounter / maxWidth);
                        }

                        beneficialCounter++;
                        posX -= 30 * (maxWidth != -1 ? (beneficialCounter - 1) % maxWidth + 1 : beneficialCounter);
                    } else {

                        posY += maxWidth != -1 ? 25 * beneficialRows + 1 : 26;
                        if (maxWidth != -1) {

                            posY += 25 * (harmfulCounter / maxWidth);
                        }

                        harmfulCounter++;
                        posX -= 30 * (maxWidth != -1 ? (harmfulCounter - 1) % maxWidth + 1 : harmfulCounter);
                    }

                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, StylishEffects.CONFIG.client().inventoryEffects().widgetAlpha);
                    // background
                    AbstractGui.blit(matrixStack, posX, posY, effectinstance.isAmbient() ? 29 : 0, 64, 29, 24, 256, 256);
                    if (mouseX >= posX && mouseX <= posX + 30 && mouseY > posY && mouseY <= posY + 26) {
                        hoveredEffect = Optional.of(effectinstance);
                    }
                    TextureAtlasSprite textureatlassprite = potionspriteuploader.get(effect);
                    float blinkingAlpha = this.getBlinkingAlpha(effectinstance);
                    effects.add(this.getEffectRenderer(matrixStack, effectinstance, textureatlassprite, posX, posY, blinkingAlpha * StylishEffects.CONFIG.client().inventoryEffects().widgetAlpha));
                    // custom forge renderer for icon
                    effectinstance.renderHUDEffect(this.mc.gui, matrixStack, posX, posY, this.mc.gui.getBlitOffset(), blinkingAlpha * StylishEffects.CONFIG.client().inventoryEffects().widgetAlpha);
                }
            }

            effects.forEach(Runnable::run);
            RenderSystem.enableDepthTest();
        }

        return hoveredEffect;
    }

    private float getBlinkingAlpha(EffectInstance effectinstance) {
        if (!effectinstance.isAmbient() && effectinstance.getDuration() <= 200) {
            int duration = 10 - effectinstance.getDuration() / 20;
            return MathHelper.clamp((float) effectinstance.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + MathHelper.cos((float) effectinstance.getDuration() * (float)Math.PI / 5.0F) * MathHelper.clamp((float) duration / 10.0F * 0.25F, 0.0F, 0.25F);
        }
        return 1.0F;
    }

    private Runnable getEffectRenderer(MatrixStack matrixStack, EffectInstance effectinstance, TextureAtlasSprite textureatlassprite, int posX, int posY, float alpha) {
        return () -> {
            RenderSystem.enableBlend();
            this.mc.getTextureManager().bind(textureatlassprite.atlas().location());
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
            AbstractGui.blit(matrixStack, posX + 5, posY + (effectinstance.isAmbient() ? 3 : 2), this.mc.gui.getBlitOffset(), 18, 18, textureatlassprite);
            if (StylishEffects.CONFIG.client().inventoryEffects().effectAmplifier != ClientConfig.EffectAmplifier.NONE)
            this.drawEffectAmplifier(matrixStack, effectinstance, posX, posY);
            if (!effectinstance.isAmbient()) {
                getEffectDuration(effectinstance).ifPresent(durationComponent -> {
                    int potionColor = getEffectColor(StylishEffects.CONFIG.client().inventoryEffects().durationColor, effectinstance);
                    AbstractGui.drawCenteredString(matrixStack, this.mc.font, durationComponent, posX + 15, posY + 14, (int) (StylishEffects.CONFIG.client().inventoryEffects().widgetAlpha * 255.0F) << 24 | potionColor);
                });
            }
        };
    }

    private void drawEffectAmplifier(MatrixStack matrixStack, EffectInstance effectinstance, int posX, int posY) {
        if (effectinstance.getAmplifier() >= 1 && effectinstance.getAmplifier() <= 9) {
            this.mc.getTextureManager().bind(TINY_NUMBERS_TEXTURE);
            int potionColor = getEffectColor(StylishEffects.CONFIG.client().inventoryEffects().amplifierColor, effectinstance);
            float red = (potionColor >> 16 & 255) / 255.0F;
            float green = (potionColor >> 8 & 255) / 255.0F;
            float blue = (potionColor >> 0 & 255) / 255.0F;
            // drop shadow
            RenderSystem.color4f(red * 0.25F, green * 0.25F, blue * 0.25F, StylishEffects.CONFIG.client().inventoryEffects().widgetAlpha);
            AbstractGui.blit(matrixStack, posX + (StylishEffects.CONFIG.client().inventoryEffects().effectAmplifier == ClientConfig.EffectAmplifier.TOP_LEFT ? 4 : 24), posY + 3, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
            // actual number
            RenderSystem.color4f(red, green, blue, StylishEffects.CONFIG.client().inventoryEffects().widgetAlpha);
            AbstractGui.blit(matrixStack, posX + (StylishEffects.CONFIG.client().inventoryEffects().effectAmplifier == ClientConfig.EffectAmplifier.TOP_LEFT ? 3 : 23), posY + 2, 5 * (effectinstance.getAmplifier() + 1), 0, 3, 5, 256, 256);
        }
    }

    private static Optional<IFormattableTextComponent> getEffectDuration(EffectInstance effectInstance) {
        String effectDuration = EffectUtils.formatDuration(effectInstance, 1.0F);
        if (effectDuration.equals("**:**")) {
            switch (StylishEffects.CONFIG.client().inventoryEffects().longDurationString) {
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

    private static int getEffectColor(TextFormatting color, EffectInstance effectinstance) {
        if (color != null) return color.getColor();
        return brightenColor(PotionUtils.getColor(Lists.newArrayList(effectinstance)));
    }

    private static int brightenColor(int potionColor) {
        int red = potionColor >> 16 & 255;
        int green = potionColor >> 8 & 255;
        int blue = potionColor & 255;
        if (red + green + blue < 128) {
            int min = Math.min(red, Math.min(green, blue));
            int increase = 128 - min;
            final int[] color = {red + increase, green + increase, blue + increase};
            redistributeColors(color);
            return color[0] << 16 | color[1] << 8 | color[0];
        }
        return potionColor;
    }

    private static void redistributeColors(int[] color) {
        int max = Math.max(color[0], Math.max(color[1], color[2]));
        if (max > 255) {
            int total = color[0] + color[1] + color[2];
            if (total > 255 * 3) {
                color[0] = color[1] = color[2] = 255;
            } else {
                int x = (3 * 255 - total) / (3 * max - total);
                int gray = 255 - x * max;
                color[0] = gray + x * color[0];
                color[1] = gray + x * color[1];
                color[2] = gray + x * color[2];
            }
        }
    }

}
