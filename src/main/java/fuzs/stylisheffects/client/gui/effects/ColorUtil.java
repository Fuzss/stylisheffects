package fuzs.stylisheffects.client.gui.effects;

import com.google.common.collect.Lists;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.text.TextFormatting;

public class ColorUtil {
    public static int getEffectColor(TextFormatting color, EffectInstance effectinstance) {
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
