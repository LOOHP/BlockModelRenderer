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

import com.loohp.blockmodelrenderer.blending.BlendingModes;
import com.loohp.blockmodelrenderer.utils.ColorUtils;
import com.loohp.blockmodelrenderer.utils.MathUtils;
import com.loohp.blockmodelrenderer.utils.TaskCompletion;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Model implements ITransformable {

    public static final int PIXEL_PER_THREAD = 256;

    private List<Hexahedron> components;
    private List<Face> faces;

    public Model(List<Hexahedron> components) {
        this.components = components;
        this.faces = new ArrayList<>();
        for (Hexahedron hexahedron : components) {
            this.faces.addAll(hexahedron.getFacesByAverageZ());
        }
        sortFaces();
    }

    public Model(Hexahedron... components) {
        this(new ArrayList<>(Arrays.asList(components)));
    }

    public void append(Model model) {
        for (Hexahedron hexahedron : model.components) {
            this.components.add(hexahedron);
            this.faces.addAll(hexahedron.getFacesByAverageZ());
        }
        sortFaces();
    }

    public List<Hexahedron> getComponents() {
        return components;
    }

    public void rotate(double x, double y, double z, boolean saveAxis) {
        for (Hexahedron hexahedron : components) {
            hexahedron.rotate(x, y, z, saveAxis);
        }
        sortFaces();
    }

    public void translate(double x, double y, double z) {
        for (Hexahedron hexahedron : components) {
            hexahedron.translate(x, y, z);
        }
    }

    public void scale(double x, double y, double z) {
        for (Hexahedron hexahedron : components) {
            hexahedron.scale(x, y, z);
        }
    }

    public void flipAboutPlane(boolean x, boolean y, boolean z) {
        for (Hexahedron hexahedron : components) {
            hexahedron.flipAboutPlane(x, y, z);
        }
    }

    @Override
    public void updateLighting(Vector direction, double ambient, double max) {
        for (Hexahedron hexahedron : components) {
            hexahedron.updateLighting(direction, ambient, max);
        }
    }

    public void sortFaces() {
        faces.sort(Face.AVERAGE_DEPTH_COMPARATOR);
    }

    public TaskCompletion render(BufferedImage source, boolean useZBuffer, AffineTransform baseTransform, BlendingModes blendingMode, ExecutorService service) {
        List<BakeResult> bakes = new LinkedList<>();
        for (Face face : faces) {
            BakeResult result = face.bake(baseTransform);
            if (result != null && result.hasInverseTransform()) {
                bakes.add(result);
            }
        }
        int w = source.getWidth();
        int h = source.getHeight();
        double baseTranslateX = baseTransform.getTranslateX();
        double baseTranslateY = baseTransform.getTranslateY();
        double baseScaleX = baseTransform.getScaleX();
        double baseScaleY = -baseTransform.getScaleY();
        int[] sourceColors = source.getRGB(0, 0, w, h, null, 0, w);
        int pixelCount = w * h;
        List<Future<?>> futures = new ArrayList<>((pixelCount / PIXEL_PER_THREAD) + 1);
        for (int i = 0; i < pixelCount; i += PIXEL_PER_THREAD) {
            int currentI = i;
            futures.add(service.submit(() -> {
                double[] pointSrc = new double[2];
                double[] pointDes = new double[2];
                for (int u = 0; u < PIXEL_PER_THREAD; u++) {
                    int position = currentI + u;
                    if (position >= pixelCount) {
                        break;
                    }
                    int sourceColor = sourceColors[position];
                    int x = position % w;
                    int y = position / w;
                    double reverseTransformedX = (x - baseTranslateX) / baseScaleX;
                    double reverseTransformedY = (y - baseTranslateY) / baseScaleY;
                    int newColor = sourceColor;
                    double z = MathUtils.NEGATIVE_MAX_DOUBLE;
                    int depthTieBreaker = Integer.MIN_VALUE;
                    pointSrc[0] = x;
                    pointSrc[1] = y;
                    for (BakeResult bake : bakes) {
                        if (bake.isOutOfBound(reverseTransformedX, reverseTransformedY)) {
                            continue;
                        }
                        bake.getInverseTransform().transform(pointSrc, 0, pointDes, 0, 1);
                        BufferedImage image = bake.getTexture();
                        if (!MathUtils.greaterThanOrEquals(pointDes[0], 0.0) || !MathUtils.greaterThanOrEquals(pointDes[1], 0.0) || !MathUtils.lessThan(pointDes[0], image.getWidth()) || !MathUtils.lessThan(pointDes[1], image.getHeight())) {
                            continue;
                        }
                        int imageColor = image.getRGB((int) pointDes[0], (int) pointDes[1]);
                        if (useZBuffer) {
                            int imageAlpha = ColorUtils.getAlpha(imageColor);
                            int sourceAlpha = ColorUtils.getAlpha(sourceColor);
                            if (imageAlpha > 0) {
                                double depth = bake.getDepthAt(reverseTransformedX, reverseTransformedY);
                                int tieBreak = bake.getDepthTieBreaker();
                                if (MathUtils.greaterThan(depth, z) || (MathUtils.equals(depth, z) && tieBreak > depthTieBreaker)) {
                                    depthTieBreaker = tieBreak;
                                    if (depth > z) {
                                        z = depth;
                                    }
                                    if (imageAlpha >= 255) {
                                        newColor = imageColor;
                                    } else {
                                        newColor = ColorUtils.composite(imageColor, newColor, blendingMode);
                                    }
                                }
                            } else if (sourceAlpha < 255) {
                                newColor = ColorUtils.composite(newColor, imageColor, blendingMode);
                            }
                        } else {
                            newColor = ColorUtils.composite(imageColor, newColor, blendingMode);
                        }
                    }
                    if (newColor != sourceColor) {
                        source.setRGB(x, y, newColor);
                    }
                }
            }));
        }
        return new TaskCompletion(futures);
    }

}
