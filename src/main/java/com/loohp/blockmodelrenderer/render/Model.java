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
import com.loohp.blockmodelrenderer.serialize.Serializable;
import com.loohp.blockmodelrenderer.utils.ColorUtils;
import com.loohp.blockmodelrenderer.utils.MathUtils;
import com.loohp.blockmodelrenderer.utils.TaskCompletion;
import org.tinspin.index.rtree.RTree;
import org.tinspin.index.rtree.RTreeIterator;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Model implements ITransformable, Serializable {

    public static final int PIXEL_PER_THREAD = 256;

    private final List<Hexahedron> components;
    private final List<Face> faces;

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

    public Model(InputStream inputStream) throws IOException {
        DataInputStream in = new DataInputStream(inputStream);
        int size = in.readInt();
        this.components = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            components.add(new Hexahedron(in));
        }
        this.faces = new ArrayList<>();
        for (Hexahedron hexahedron : components) {
            this.faces.addAll(hexahedron.getFacesByAverageZ());
        }
        sortFaces();
    }

    @Override
    public void serialize(OutputStream outputStream) throws IOException {
        DataOutputStream out = new DataOutputStream(outputStream);
        out.writeInt(components.size());
        for (Hexahedron hexahedron : components) {
            hexahedron.serialize(out);
        }
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
        RTree<BakeResult> bakes = RTree.createRStar(2);
        for (Face face : faces) {
            BakeResult result = face.bake(baseTransform);
            if (result != null && result.hasInverseTransform()) {
                bakes.insert(new double[] {result.getMinX(), result.getMinY()}, new double[] {result.getMaxX(), result.getMaxY()}, result);
            }
        }
        int w = source.getWidth();
        int h = source.getHeight();
        double baseTranslateX = baseTransform.getTranslateX();
        double baseTranslateY = baseTransform.getTranslateY();
        double baseScaleX = baseTransform.getScaleX();
        double baseScaleY = -baseTransform.getScaleY();
        DataBuffer dataBuffer = source.getRaster().getDataBuffer();
        if (!(dataBuffer instanceof DataBufferInt)) {
            throw new RuntimeException("This image is not compatible for rendering: Raster DataBuffer of BufferedImage is not a DataBufferInt");
        }
        int[] sourceColors = ((DataBufferInt) dataBuffer).getData();
        int pixelCount = w * h;
        int targetTaskCount = Math.max(1, Runtime.getRuntime().availableProcessors() * 4);
        int pixelsPerTask = Math.max(PIXEL_PER_THREAD, (pixelCount + targetTaskCount - 1) / targetTaskCount);
        List<Future<?>> futures = new ArrayList<>((pixelCount + pixelsPerTask - 1) / pixelsPerTask);
        for (int i = 0; i < pixelCount; i += pixelsPerTask) {
            int currentI = i;
            int endI = Math.min(pixelCount, i + pixelsPerTask);
            futures.add(service.submit(() -> {
                double[] transformedPos = new double[2];
                RTreeIterator<BakeResult> itr = bakes.queryIntersect(transformedPos, transformedPos);
                int y = currentI / w;
                int x = currentI - y * w;
                for (int position = currentI; position < endI; position++) {
                    int sourceColor = sourceColors[position];
                    double reverseTransformedX = (x - baseTranslateX) / baseScaleX;
                    double reverseTransformedY = (y - baseTranslateY) / baseScaleY;
                    transformedPos[0] = reverseTransformedX;
                    transformedPos[1] = reverseTransformedY;
                    int newColor = sourceColor;
                    double z = MathUtils.NEGATIVE_MAX_DOUBLE;
                    int depthTieBreaker = Integer.MIN_VALUE;
                    itr.reset(transformedPos, transformedPos);
                    while (itr.hasNext()) {
                        BakeResult bake = itr.next().value();
                        double textureX = bake.getInverseTransformedX(x, y);
                        double textureY = bake.getInverseTransformedY(x, y);
                        BufferedImage image = bake.getTexture();
                        if (!MathUtils.greaterThanOrEquals(textureX, 0.0) || !MathUtils.greaterThanOrEquals(textureY, 0.0) || !MathUtils.lessThan(textureX, image.getWidth()) || !MathUtils.lessThan(textureY, image.getHeight())) {
                            continue;
                        }
                        int imageColor = bake.getTextureDataArray()[(int) textureX + ((int) textureY * image.getWidth())];
                        if (useZBuffer) {
                            int imageAlpha = bake.isFullyOpaque() ? 255 : ColorUtils.getAlpha(imageColor);
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
                            } else if (ColorUtils.getAlpha(sourceColor) < 255) {
                                newColor = ColorUtils.composite(newColor, imageColor, blendingMode);
                            }
                        } else if (bake.isFullyOpaque() && blendingMode == BlendingModes.NORMAL) {
                            newColor = imageColor;
                        } else {
                            newColor = ColorUtils.composite(imageColor, newColor, blendingMode);
                        }
                    }
                    if (newColor != sourceColor) {
                        sourceColors[position] = newColor;
                    }
                    x++;
                    if (x >= w) {
                        x = 0;
                        y++;
                    }
                }
            }));
        }
        return new TaskCompletion(futures);
    }
}
