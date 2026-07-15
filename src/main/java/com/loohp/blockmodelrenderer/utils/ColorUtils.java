/*
 * This file is part of BlockModelRenderer.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.blockmodelrenderer.utils;

import com.loohp.blockmodelrenderer.blending.BlendingModes;
import com.loohp.blockmodelrenderer.blending.ColorCompositeFactor;
import com.loohp.blockmodelrenderer.blending.BlendingMode;

public class ColorUtils {

    public static int composite(int srcColor, int desColor, BlendingModes blendingModes) {
        if (blendingModes == BlendingModes.NORMAL) {
            return compositeNormal(srcColor, desColor);
        }
        if (blendingModes == BlendingModes.GLINT) {
            return compositeGlint(srcColor, desColor);
        }
        return composite(srcColor, desColor, blendingModes.getSrcColorComposite(), blendingModes.getDesColorComposite(), blendingModes.getSrcAlphaComposite(), blendingModes.getDesAlphaComposite());
    }

    private static int compositeNormal(int srcColor, int desColor) {
        double srcAlphaFactor = getAlpha(srcColor) / 255.0;
        double desFactor = 1.0 - srcAlphaFactor;
        int red = Math.min(255, (int) (getRed(srcColor) * srcAlphaFactor + getRed(desColor) * desFactor));
        int green = Math.min(255, (int) (getGreen(srcColor) * srcAlphaFactor + getGreen(desColor) * desFactor));
        int blue = Math.min(255, (int) (getBlue(srcColor) * srcAlphaFactor + getBlue(desColor) * desFactor));
        int alpha = Math.min(255, (int) (getAlpha(srcColor) + getAlpha(desColor) * desFactor));
        return getIntFromColor(red, green, blue, alpha);
    }

    private static int compositeGlint(int srcColor, int desColor) {
        int srcRed = getRed(srcColor);
        int srcGreen = getGreen(srcColor);
        int srcBlue = getBlue(srcColor);
        int red = Math.min(255, (int) (srcRed * (srcRed / 255.0) + getRed(desColor)));
        int green = Math.min(255, (int) (srcGreen * (srcGreen / 255.0) + getGreen(desColor)));
        int blue = Math.min(255, (int) (srcBlue * (srcBlue / 255.0) + getBlue(desColor)));
        return getIntFromColor(red, green, blue, getAlpha(desColor));
    }

    public static int composite(int srcColor, int desColor, BlendingMode srcComposite, BlendingMode desComposite) {
        return composite(srcColor, desColor, srcComposite, desComposite, srcComposite, desComposite);
    }

    public static int composite(int srcColor, int desColor, BlendingMode srcColorComposite, BlendingMode desColorComposite, BlendingMode srcAlphaComposite, BlendingMode desAlphaComposite) {
        ColorCompositeFactor srcFactor = srcColorComposite.getColorCompositeFactor(srcColor, desColor);
        ColorCompositeFactor desFactor = desColorComposite.getColorCompositeFactor(srcColor, desColor);
        double srcAlphaFactor = srcAlphaComposite.getAlphaCompositeFactor(srcColor, desColor);
        double desAlphaFactor = desAlphaComposite.getAlphaCompositeFactor(srcColor, desColor);
        return composite(srcColor, desColor, srcFactor, desFactor, srcAlphaFactor, desAlphaFactor);
    }

    public static int composite(int srcColor, int desColor, ColorCompositeFactor srcFactor, ColorCompositeFactor desFactor, double srcAlphaFactor, double desAlphaFactor) {
        int red = Math.min(255, (int) (getRed(srcColor) * srcFactor.getRed() + getRed(desColor) * desFactor.getRed()));
        int green = Math.min(255, (int) (getGreen(srcColor) * srcFactor.getGreen() + getGreen(desColor) * desFactor.getGreen()));
        int blue = Math.min(255, (int) (getBlue(srcColor) * srcFactor.getBlue() + getBlue(desColor) * desFactor.getBlue()));
        int alpha = Math.min(255, (int) (getAlpha(srcColor) * srcAlphaFactor + getAlpha(desColor) * desAlphaFactor));

        return getIntFromColor(red, green, blue, alpha);
    }

    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int getBlue(int color) {
        return color & 0xFF;
    }

    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }

    public static int getIntFromColor(int r, int g, int b, int a) {
        int red = (r << 16) & 0x00FF0000;
        int green = (g << 8) & 0x0000FF00;
        int blue = b & 0x000000FF;
        int alpha = (a << 24) & 0xFF000000;

        return alpha | red | green | blue;
    }

}
