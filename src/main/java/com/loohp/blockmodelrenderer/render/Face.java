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

import com.loohp.blockmodelrenderer.blending.BlendingMode;
import com.loohp.blockmodelrenderer.blending.BlendingModes;
import com.loohp.blockmodelrenderer.serialize.Serializable;
import com.loohp.blockmodelrenderer.utils.ColorUtils;
import com.loohp.blockmodelrenderer.utils.DataSerializationUtils;
import com.loohp.blockmodelrenderer.utils.ImageUtils;
import com.loohp.blockmodelrenderer.utils.MathUtils;
import com.loohp.blockmodelrenderer.utils.PlaneUtils;
import com.loohp.blockmodelrenderer.utils.PointConversionUtils;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class Face implements ITransformable, Serializable {

    public static final Comparator<Face> AVERAGE_DEPTH_COMPARATOR = Comparator.comparing(face -> face.getAverageZ());

    protected BufferedImage image;
    protected BufferedImage[] overlay;
    protected BlendingModes[] overlayBlendingMode;
    protected double overlayAdditionFactor;
    protected Face oppositeFace;
    protected byte priority;
    private double lightRatio;
    private Point3D[] points;
    private Vector[][] axis;
    private Face cullface;

    public Face(BufferedImage image, Point3D... points) {
        if (points.length != 4) {
            throw new RuntimeException("points must have a length of 4.");
        }
        this.lightRatio = 1;
        this.oppositeFace = null;
        this.priority = 1;
        this.cullface = null;
        this.image = image;
        this.overlay = null;
        this.overlayBlendingMode = null;
        this.overlayAdditionFactor = 1;
        this.points = new Point3D[points.length];
        this.axis = new Vector[points.length][];
        for (int i = 0; i < points.length; i++) {
            this.points[i] = points[i].clone();
            this.axis[i] = new Vector[] {new Vector(1, 0, 0), new Vector(0, 1, 0), new Vector(0, 0, 1)};
        }
    }

    public Face(Point3D... points) {
        this(null, points);
    }

    public Face(InputStream inputStream) throws IOException {
        DataInputStream in = new DataInputStream(inputStream);
        this.image = DataSerializationUtils.readImage(in);
        this.overlay = DataSerializationUtils.readArray(BufferedImage.class, in, true, i -> DataSerializationUtils.readImage(i));
        this.overlayBlendingMode = DataSerializationUtils.readArray(BlendingModes.class, in, true, i -> {
            BlendingMode[] modes = BlendingMode.values();
            return BlendingModes.of(modes[i.readInt()], modes[i.readInt()], modes[i.readInt()], modes[i.readInt()]);
        });
        this.overlayAdditionFactor = in.readDouble();
        this.oppositeFace = DataSerializationUtils.readNullable(Face.class, in, i -> new Face(i));
        this.priority = in.readByte();
        this.lightRatio = in.readDouble();
        this.points = DataSerializationUtils.readArray(Point3D.class, in, false, i -> new Point3D(i.readInt(), i.readInt(), i.readInt()));
        this.axis = DataSerializationUtils.readArray(Vector[].class, in, false, i -> {
            return DataSerializationUtils.readArray(Vector.class, i, false, i1 -> new Vector(i1.readDouble(), i1.readDouble(), i1.readDouble()));
        });
        this.cullface = DataSerializationUtils.readNullable(Face.class, in, i -> new Face(i));
    }

    @Override
    public void serialize(OutputStream outputStream) throws IOException {
        DataOutputStream out = new DataOutputStream(outputStream);
        DataSerializationUtils.writeImage(image, out);
        DataSerializationUtils.writeArray(overlay, out, true, (i, o) -> DataSerializationUtils.writeImage(i, o));
        DataSerializationUtils.writeArray(overlayBlendingMode, out, true, (m, o) -> {
            o.writeInt(m.getSrcColorComposite().ordinal());
            o.writeInt(m.getDesColorComposite().ordinal());
            o.writeInt(m.getSrcAlphaComposite().ordinal());
            o.writeInt(m.getDesAlphaComposite().ordinal());
        });
        out.writeDouble(overlayAdditionFactor);
        DataSerializationUtils.writeNullable(oppositeFace, out, (f, o) -> f.serialize(o));
        out.writeByte(priority);
        out.writeDouble(lightRatio);
        DataSerializationUtils.writeArray(points, out, false, (p, o) -> {
            o.writeDouble(p.x);
            o.writeDouble(p.y);
            o.writeDouble(p.z);
        });
        DataSerializationUtils.writeArray(axis, out, false, (a, o) -> {
            DataSerializationUtils.writeArray(a, o, false, (v, o1) -> {
                o1.writeDouble(v.getX());
                o1.writeDouble(v.getY());
                o1.writeDouble(v.getZ());
            });
        });
        DataSerializationUtils.writeNullable(cullface, out, (f, o) -> f.serialize(o));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((image == null) ? 0 : image.hashCode());
        result = prime * result + Arrays.hashCode(points);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Face)) {
            return false;
        }
        Face other = (Face) obj;
        if (image == null) {
            if (other.image != null) {
                return false;
            }
        } else if (!image.equals(other.image)) {
            return false;
        }
        return Arrays.equals(points, other.points);
    }

    public boolean pointsEquals(Face other) {
        Set<Point3D> ourPoints = new HashSet<>(Arrays.asList(this.points));
        for (Point3D point : other.points) {
            if (ourPoints.stream().noneMatch(each -> MathUtils.equals(each.x, point.x) && MathUtils.equals(each.y, point.y) && MathUtils.equals(each.z, point.z))) {
                return false;
            }
        }
        return true;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage[] getOverlay() {
        return overlay;
    }

    public void setOverlay(BufferedImage[] overlay) {
        this.overlay = overlay;
    }

    public BlendingModes[] getOverlayBlendingMode() {
        return overlayBlendingMode;
    }

    public void setOverlayBlendingMode(BlendingModes[] overlayBlendingMode) {
        this.overlayBlendingMode = overlayBlendingMode;
    }

    public Face getOppositeFace() {
        return oppositeFace;
    }

    public boolean hasOppositeFace() {
        return oppositeFace != null;
    }

    public Face getCullface() {
        return cullface;
    }

    public void setCullface(Face cullface) {
        this.cullface = cullface;
    }

    public Point3D[] getPoints() {
        return points;
    }

    public boolean isWithin(double x, double y) {
        Point2D[] points2d = new Point2D[points.length];
        for (int i = 0; i < points.length; i++) {
            points2d[i] = PointConversionUtils.convert(points[i], false);
        }
        return PlaneUtils.contains(new Point2D(x, y), points2d);
    }

    public double getDepthAt(double x, double y) {
        Vector rayVector = new Vector(0, 0, 1);
        Vector rayPoint = new Vector(x, y, 0);
        Vector planeNormal = new Vector(this.points[3], this.points[0]).cross(new Vector(this.points[1], this.points[0])).normalize();
        Vector planePoint = new Vector(this.points[0].x, this.points[0].y, this.points[0].z);

        return intersectPoint(rayVector, rayPoint, planeNormal, planePoint).getZ();
    }

    private Vector intersectPoint(Vector rayVector, Vector rayPoint, Vector planeNormal, Vector planePoint) {
        Vector diff = rayPoint.clone().subtract(planePoint);
        double prod1 = diff.dot(planeNormal);
        double prod2 = rayVector.dot(planeNormal);
        double prod3 = prod1 / prod2;
        return rayPoint.clone().subtract(rayVector.clone().multiply(prod3));
    }

    public double getAverageX() {
        return Stream.of(points).mapToDouble(point -> point.x).average().getAsDouble();
    }

    public double getAverageY() {
        return Stream.of(points).mapToDouble(point -> point.y).average().getAsDouble();
    }

    public double getAverageZ() {
        return Stream.of(points).mapToDouble(point -> point.z).average().getAsDouble();
    }

    public double getMaxX() {
        return Stream.of(points).mapToDouble(point -> point.x).max().getAsDouble();
    }

    public double getMaxY() {
        return Stream.of(points).mapToDouble(point -> point.y).max().getAsDouble();
    }

    public double getMaxZ() {
        return Stream.of(points).mapToDouble(point -> point.z).max().getAsDouble();
    }

    public double getMinX() {
        return Stream.of(points).mapToDouble(point -> point.x).min().getAsDouble();
    }

    public double getMinY() {
        return Stream.of(points).mapToDouble(point -> point.y).min().getAsDouble();
    }

    public double getMinZ() {
        return Stream.of(points).mapToDouble(point -> point.z).min().getAsDouble();
    }

    public Point3D getCenterPoint() {
        return new Point3D(getAverageX(), getAverageY(), getAverageZ());
    }

    public Vector getCenterVector() {
        return new Vector(getAverageX(), getAverageY(), getAverageZ());
    }

    public double getLightRatio() {
        return lightRatio;
    }

    public void rotate(double x, double y, double z, boolean saveAxis) {
        x = Math.toRadians(x);
        y = Math.toRadians(y);
        z = Math.toRadians(z);
        Vector origin = new Vector(0, 0, 0);
        for (int i = 0; i < points.length; i++) {
            Point3D point = points[i];
            Vector xAxis = axis[i][0];
            Vector yAxis = axis[i][1];
            Vector zAxis = axis[i][2];
            if (!saveAxis) {
                xAxis = xAxis.clone();
                yAxis = yAxis.clone();
                zAxis = zAxis.clone();
            }
            Vector v = new Vector(point.x, point.y, point.z).subtract(origin);
            v.rotateAroundAxis(xAxis, x);
            yAxis.rotateAroundAxis(xAxis, x);
            zAxis.rotateAroundAxis(xAxis, x);
            v.rotateAroundAxis(yAxis, y);
            xAxis.rotateAroundAxis(yAxis, y);
            zAxis.rotateAroundAxis(yAxis, y);
            v.rotateAroundAxis(zAxis, z);
            xAxis.rotateAroundAxis(zAxis, z);
            yAxis.rotateAroundAxis(zAxis, z);
            point.x = v.getX();
            point.y = v.getY();
            point.z = v.getZ();
        }
    }

    public void translate(double x, double y, double z) {
        for (Point3D point : points) {
            PointConversionUtils.translate(point, x, y, z);
        }
    }

    public void scale(double x, double y, double z) {
        for (Point3D point : points) {
            PointConversionUtils.scale(point, x, y, z);
        }
    }

    @Override
    public void flipAboutPlane(boolean x, boolean y, boolean z) {
        for (Point3D point : points) {
            PointConversionUtils.flipAboutPlane(point, x, y, z);
        }
    }

    @Override
    public void updateLighting(Vector direction, double ambient, double max) {
        Vector normal = new Vector(this.points[1], this.points[2]).cross(new Vector(this.points[0], this.points[1])).normalize();
        double dot = normal.dot(direction);
        double sign = Math.signum(dot);
        dot = sign * dot * dot;
        dot = (dot + 1.0) / 2.0 * (1.0 - ambient);

        lightRatio = Math.min(Math.min(max, 1.0), Math.max(0.0, ambient + dot));
    }

    public BakeResult bake(AffineTransform baseTransform) {
        if (image == null) {
            return null;
        } else {
            if (cullface != null) {
                if (AVERAGE_DEPTH_COMPARATOR.compare(this, cullface) <= 0) {
                    if (cullface.priority > this.priority || cullface.getAverageZ() - this.getAverageZ() > 0.1) {
                        return null;
                    }
                }
            }

            Point2D[] points2d = new Point2D[points.length];
            for (int i = 0; i < points.length; i++) {
                points2d[i] = PointConversionUtils.convert(points[i], true);
            }

            BufferedImage image = ImageUtils.multiply(ImageUtils.copyImage(this.image), lightRatio);
            if (overlay != null) {
                for (int i = 0; i < overlay.length; i++) {
                    BufferedImage overlayLayer = overlay[i];
                    BlendingModes blendingModes = overlayBlendingMode == null || i >= overlayBlendingMode.length || overlayBlendingMode[i] == null ? BlendingModes.GLINT : overlayBlendingMode[i];
                    image = ImageUtils.transformRGB(image, (x, y, colorValue) -> {
                        return ColorUtils.composite(overlayLayer.getRGB(x, y), colorValue, blendingModes);
                    });
                }
            }

            double w = 0;
            double h = 0;
            double scaleX;
            double scaleY;
            boolean first = true;

            do {
                if (!first) {
                    points2d[0].x += 0.0001;
                    points2d[0].y += 0.0001;
                    points2d[1].x -= 0.0001;
                    points2d[1].y -= 0.0001;
                    points2d[2].x += 0.0001;
                    points2d[2].y += 0.0001;
                    points2d[3].x -= 0.0001;
                    points2d[3].y -= 0.0001;
                }
                w = Math.abs(points2d[1].x - points2d[0].x);
                h = Math.abs(points2d[2].y - points2d[1].y);
                scaleX = w / image.getWidth();
                scaleY = h / image.getHeight();
                first = false;
            } while (w < 0.0000000001 || h < 0.0000000001);

            AffineTransform transform = (AffineTransform) baseTransform.clone();
            transform.concatenate(AffineTransform.getTranslateInstance(points2d[0].x, points2d[0].y));
            double dX1 = points2d[3].x - points2d[0].x;
            double dY1 = points2d[1].y - points2d[0].y;

            double dY2 = points2d[3].y - points2d[0].y;
            double dX2 = points2d[1].x - points2d[0].x;

            double dropOffX = dX1 / dY2;
            double dropOffY = dY1 / dX2;

            transform.concatenate(AffineTransform.getShearInstance(dropOffX, dropOffY));
            if ((points2d[1].x - points2d[0].x) < 0) {
                scaleX = -scaleX;
            }
            if ((points2d[3].y - points2d[0].y) < 0) {
                scaleY = -scaleY;
            }
            transform.concatenate(AffineTransform.getScaleInstance(scaleX, scaleY));

            double maxX = getMaxX();
            double maxY = getMaxY();
            double minX = getMinX();
            double minY = getMinY();

            return new BakeResult(image, transform, (x, y) -> {
                return getDepthAt(x, y);
            }, priority, (x, y) -> {
                return !MathUtils.greaterThanOrEquals(x, minX) || !MathUtils.greaterThanOrEquals(y, minY) || !MathUtils.lessThanOrEquals(x, maxX) || !MathUtils.lessThanOrEquals(y, maxY);
            });
        }
    }

}
