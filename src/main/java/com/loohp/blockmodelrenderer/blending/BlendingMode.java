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

package com.loohp.blockmodelrenderer.blending;

import java.util.HashMap;
import java.util.Map;

import static com.loohp.blockmodelrenderer.utils.ColorUtils.*;

public enum BlendingMode {

    ZERO((src, des) -> CompositeFactor.ZERO, 0),

    ONE((src, des) -> CompositeFactor.ONE, 1),

    SRC_COLOR((src, des) -> {
        return new CompositeFactor(getRed(src) / 255.0, getGreen(src) / 255.0, getBlue(src) / 255.0, getAlpha(src) / 255.0);
    }, 768),

    ONE_MINUS_SRC_COLOR((src, des) -> {
        return new CompositeFactor(1.0 - (getRed(src) / 255.0), 1.0 - (getGreen(src) / 255.0), 1.0 - (getBlue(src) / 255.0), 1.0 - (getAlpha(src) / 255.0));
    }, 769),

    DST_COLOR((src, des) -> {
        return new CompositeFactor(getRed(des) / 255.0, getGreen(des) / 255.0, getBlue(des) / 255.0, getAlpha(des) / 255.0);
    }, 774),

    ONE_MINUS_DST_COLOR((src, des) -> {
        return new CompositeFactor(1.0 - (getRed(des) / 255.0), 1.0 - (getGreen(des) / 255.0), 1.0 - (getBlue(des) / 255.0), 1.0 - (getAlpha(des) / 255.0));
    }, 775),

    SRC_ALPHA((src, des) -> {
        double alpha = getAlpha(src) / 255.0;
        return new CompositeFactor(alpha, alpha, alpha, alpha);
    }, 770),

    ONE_MINUS_SRC_ALPHA((src, des) -> {
        double alpha = 1.0 - (getAlpha(src) / 255.0);
        return new CompositeFactor(alpha, alpha, alpha, alpha);
    }, 771),

    DST_ALPHA((src, des) -> {
        double alpha = getAlpha(des) / 255.0;
        return new CompositeFactor(alpha, alpha, alpha, alpha);
    }, 772),

    ONE_MINUS_DST_ALPHA((src, des) -> {
        double alpha = 1.0 - (getAlpha(des) / 255.0);
        return new CompositeFactor(alpha, alpha, alpha, alpha);
    }, 773);

    private static final BlendingMode[] VALUES = values();
    private static final Map<String, BlendingMode> BY_NAME = new HashMap<>();
    private static final Map<Integer, BlendingMode> BY_VALUE = new HashMap<>();

    static {
        for (BlendingMode blendingMode : VALUES) {
            BY_NAME.put(blendingMode.getOpenGLName(), blendingMode);
            BY_VALUE.put(blendingMode.getOpenGLValue(), blendingMode);
        }
    }

    private final CompositeFactorFunction compositeFunction;
    private final String name;
    private final int openGLValue;

    BlendingMode(CompositeFactorFunction compositeFunction, int openGLValue) {
        this.compositeFunction = compositeFunction;
        this.openGLValue = openGLValue;
        this.name = "GL_" + name();
    }

    public CompositeFactor getCompositeFactor(int src, int des) {
        return compositeFunction.apply(src, des);
    }

    public int getOpenGLValue() {
        return openGLValue;
    }

    public String getOpenGLName() {
        return name;
    }

    public static BlendingMode fromOpenGL(String name) {
        return BY_NAME.get(name);
    }

    public static BlendingMode fromOpenGL(int value) {
        return BY_VALUE.get(value);
    }
}
