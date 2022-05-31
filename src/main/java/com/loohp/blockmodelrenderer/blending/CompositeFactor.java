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

public class CompositeFactor {

    public static final CompositeFactor ZERO = new CompositeFactor(0.0, 0.0, 0.0, 0.0);
    public static final CompositeFactor ONE = new CompositeFactor(1.0, 1.0, 1.0, 1.0);

    private double red;
    private double green;
    private double blue;
    private double alpha;

    public CompositeFactor(double red, double green, double blue, double alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public double getRed() {
        return red;
    }

    public double getGreen() {
        return green;
    }

    public double getBlue() {
        return blue;
    }

    public double getAlpha() {
        return alpha;
    }

    @Override
    public String toString() {
        return "CompositeFactor{" +
            "red=" + red +
            ", green=" + green +
            ", blue=" + blue +
            ", alpha=" + alpha +
            '}';
    }

}
