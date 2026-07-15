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

package com.loohp.blockmodelrenderer.render;

import com.loohp.blockmodelrenderer.utils.DoubleBiFunction;
import com.loohp.blockmodelrenderer.utils.MathUtils;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;

public class BakeResult {

    private final BufferedImage texture;
    private final int[] textureDataArray;
    private final AffineTransform transform;
    private final AffineTransform inverseTransform;
    private final double inverseM00;
    private final double inverseM10;
    private final double inverseM01;
    private final double inverseM11;
    private final double inverseM02;
    private final double inverseM12;
    private final DoubleBiFunction depthFunction;
    private final int depthTieBreaker;
    private final boolean fullyOpaque;
    private final double maxX;
    private final double maxY;
    private final double minX;
    private final double minY;

    public BakeResult(BufferedImage texture, int[] textureDataArray, AffineTransform transform, DoubleBiFunction depthFunction, int depthTieBreaker, double maxX, double maxY, double minX, double minY) {
        this.texture = texture;
        this.textureDataArray = textureDataArray;
        this.transform = transform;
        this.maxX = maxX;
        this.maxY = maxY;
        this.minX = minX;
        this.minY = minY;
        AffineTransform inverseTransform;
        try {
            inverseTransform = MathUtils.equals(transform.getDeterminant(), 0.0) ? null : transform.createInverse();
        } catch (NoninvertibleTransformException e) {
            inverseTransform = null;
        }
        this.inverseTransform = inverseTransform;
        if (inverseTransform == null) {
            this.inverseM00 = 0.0;
            this.inverseM10 = 0.0;
            this.inverseM01 = 0.0;
            this.inverseM11 = 0.0;
            this.inverseM02 = 0.0;
            this.inverseM12 = 0.0;
        } else {
            this.inverseM00 = inverseTransform.getScaleX();
            this.inverseM10 = inverseTransform.getShearY();
            this.inverseM01 = inverseTransform.getShearX();
            this.inverseM11 = inverseTransform.getScaleY();
            this.inverseM02 = inverseTransform.getTranslateX();
            this.inverseM12 = inverseTransform.getTranslateY();
        }
        this.depthFunction = depthFunction;
        this.depthTieBreaker = depthTieBreaker;
        boolean fullyOpaque = true;
        for (int color : textureDataArray) {
            if ((color >>> 24) != 255) {
                fullyOpaque = false;
                break;
            }
        }
        this.fullyOpaque = fullyOpaque;
    }

    public BufferedImage getTexture() {
        return texture;
    }

    public int[] getTextureDataArray() {
        return textureDataArray;
    }

    public AffineTransform getTransform() {
        return transform;
    }

    public boolean hasInverseTransform() {
        return inverseTransform != null;
    }

    public AffineTransform getInverseTransform() {
        return inverseTransform;
    }

    public double getInverseTransformedX(double x, double y) {
        return inverseM00 * x + inverseM01 * y + inverseM02;
    }

    public double getInverseTransformedY(double x, double y) {
        return inverseM10 * x + inverseM11 * y + inverseM12;
    }

    public double getDepthAt(double x, double y) {
        return depthFunction.apply(x, y);
    }

    public int getDepthTieBreaker() {
        return depthTieBreaker;
    }

    public boolean isFullyOpaque() {
        return fullyOpaque;
    }

    public boolean isOutOfBound(double x, double y) {
        return !MathUtils.greaterThanOrEquals(x, minX) || !MathUtils.greaterThanOrEquals(y, minY) || !MathUtils.lessThanOrEquals(x, maxX) || !MathUtils.lessThanOrEquals(y, maxY);
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }
}
