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
import com.loohp.blockmodelrenderer.utils.DoubleBiPredicate;
import com.loohp.blockmodelrenderer.utils.MathUtils;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;

public class BakeResult {

    private BufferedImage texture;
    private AffineTransform transform;
    private AffineTransform inverseTransform;
    private DoubleBiFunction depthFunction;
    private int depthTieBreaker;
    private DoubleBiPredicate outOfBoundPredicate;

    public BakeResult(BufferedImage texture, AffineTransform transform, DoubleBiFunction depthFunction, int depthTieBreaker, DoubleBiPredicate outOfBoundPredicate, boolean ignoreZFight) {
        this.texture = texture;
        this.transform = transform;
        try {
            this.inverseTransform = MathUtils.equals(transform.getDeterminant(), 0.0) ? null : transform.createInverse();
        } catch (NoninvertibleTransformException e) {
            this.inverseTransform = null;
        }
        this.depthFunction = depthFunction;
        this.depthTieBreaker = depthTieBreaker;
        this.outOfBoundPredicate = outOfBoundPredicate;
    }

    public BufferedImage getTexture() {
        return texture;
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

    public double getDepthAt(double x, double y) {
        return depthFunction.apply(x, y);
    }

    public int getDepthTieBreaker() {
        return depthTieBreaker;
    }

    public boolean isOutOfBound(double x, double y) {
        return outOfBoundPredicate.test(x, y);
    }

}
