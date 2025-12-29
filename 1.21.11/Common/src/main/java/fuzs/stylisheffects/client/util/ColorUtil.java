package fuzs.stylisheffects.client.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jspecify.annotations.Nullable;

import java.util.Collections;

public class ColorUtil {

    public static Style getMobEffectStyle(MobEffectInstance mobEffect, @Nullable ChatFormatting chatFormatting) {
        if (chatFormatting != null) {
            return Style.EMPTY.withColor(chatFormatting);
        } else {
            int color = brightenColor(getPotionColor(mobEffect));
            return Style.EMPTY.withColor(color);
        }
    }

    public static Style getMobEffectStyle(MobEffectInstance mobEffect, @Nullable DyeColor dyeColor) {
        if (dyeColor != null) {
            return Style.EMPTY.withColor(dyeColor.getTextColor());
        } else {
            int color = brightenColor(getPotionColor(mobEffect));
            return Style.EMPTY.withColor(color);
        }
    }

    private static int getPotionColor(MobEffectInstance mobEffect) {
        return PotionContents.getColorOptional(Collections.singleton(mobEffect))
                .orElse(PotionContents.BASE_POTION_COLOR);
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
        } else {
            return potionColor;
        }
    }

    /**
     * @see <a href="https://stackoverflow.com/questions/141855/programmatically-lighten-a-color">Programmatically
     *         Lighten a Color</a>
     */
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
