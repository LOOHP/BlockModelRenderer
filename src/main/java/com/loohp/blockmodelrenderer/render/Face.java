package com.loohp.blockmodelrenderer.render;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

import com.loohp.blockmodelrenderer.utls.ImageUtils;
import com.loohp.blockmodelrenderer.utls.PlaneUtils;
import com.loohp.blockmodelrenderer.utls.PointConversionUtils;

public class Face implements ITransformable {
	
	public static final Comparator<Face> AVERAGE_DEPTH_COMPARATOR = Comparator.comparing(face -> face.getAverageZ());
	
	public BufferedImage image;
	protected Face oppositeFace;
	private double lightRatio;
	private Point3D[] points;
	protected byte priority;
	private boolean ignoreZFight;
	
	public Face(BufferedImage image, boolean ignoreZFight, Point3D... points) {
		if (points.length != 4) {
			throw new RuntimeException("points must have a length of 4.");
		}
		this.lightRatio = 1;
		this.oppositeFace = null;
		this.priority = 1;
		this.ignoreZFight = ignoreZFight;
		this.image = image == null ? null : ImageUtils.copyImage(image);
		this.points = new Point3D[points.length];
		for (int i = 0; i < points.length; i++) {
			this.points[i] = points[i].clone();
		}
	}
	
	public Face(boolean ignoreZFight, Point3D... points) {
		this(null, ignoreZFight, points);
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
		if (!Arrays.equals(points, other.points)) {
			return false;
		}
		return true;
	}
	
	public Face getOppositeFace() {
		return oppositeFace;
	}
	
	public boolean hasOppositeFace() {
		return oppositeFace != null;
	}

	public boolean isIgnoreZFight() {
		return ignoreZFight;
	}

	public void setIgnoreZFight(boolean ignoreZFight) {
		this.ignoreZFight = ignoreZFight;
	}

	public Point3D[] getPoints() {
		return points;
	}
	
	public double getDepthAt(double x, double y) {
		Point2D[] points2d = new Point2D[points.length];
		for (int i = 0; i < points.length; i++) {
			points2d[i] = PointConversionUtils.convert(points[i], false);
		}
		
		if (!PlaneUtils.contains(x, y, points2d)) {
			return -Double.MAX_VALUE;
		}
		
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
	
	public void rotate(double x, double y, double z) {
		for (Point3D point : points) {
			PointConversionUtils.rotateAxisY(point, y);
			PointConversionUtils.rotateAxisX(point, x);
			PointConversionUtils.rotateAxisZ(point, z);
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

	public void render(Graphics2D g, ZBuffer zBuffer) {
		if (ignoreZFight) {
			if (zBuffer != null) {
				zBuffer.setIgnoreBuffer(true);
			}
		} else {
			if (oppositeFace != null && AVERAGE_DEPTH_COMPARATOR.compare(this, oppositeFace) <= 0) {
				if (oppositeFace.priority > this.priority || oppositeFace.getAverageZ() - this.getAverageZ() > 0.1) {
					return;
				}
			}
		}
		if (image != null) {
			Point2D[] points2d = new Point2D[points.length];
			for (int i = 0; i < points.length; i++) {
				points2d[i] = PointConversionUtils.convert(points[i], true);
			}
			
			BufferedImage image = ImageUtils.multiply(ImageUtils.copyImage(this.image), lightRatio);
			
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
			
			AffineTransform orginalTransform = (AffineTransform) g.getTransform().clone();
			
			AffineTransform transform = AffineTransform.getTranslateInstance(points2d[0].x, points2d[0].y);
			double dX1 = points2d[3].x - points2d[0].x;
			double dY1 = points2d[1].y - points2d[0].y;
			
			double dY2 = points2d[3].y - points2d[0].y;
			double dX2 = points2d[1].x - points2d[0].x;
			
			double dropOffX = dX1 / dY2;
			double dropOffY = dY1 / dX2;
			
			transform.concatenate(AffineTransform.getShearInstance(dropOffX, dropOffY));
			if ((points2d[1].x - points2d[0].x) < 0) {
				transform.concatenate(AffineTransform.getScaleInstance(-1, 1));
			}
			if ((points2d[3].y - points2d[0].y) < 0) {
				transform.concatenate(AffineTransform.getScaleInstance(1, -1));
			}
			transform.concatenate(AffineTransform.getScaleInstance(scaleX, scaleY));
			g.transform(transform);
			g.drawImage(image, 0, 0, null);
			
			g.setTransform(orginalTransform);
			
			if (zBuffer != null) {
				for (int y = zBuffer.getMinY(); y < zBuffer.getMaxY(); y++) {
					for (int x = zBuffer.getMinX(); x < zBuffer.getMaxX(); x++) {
						zBuffer.set(x, y, getDepthAt(x / orginalTransform.getScaleX(), -y / orginalTransform.getScaleY()));
					}
				}
			}
		}
	}

	public double getLightRatio() {
		return lightRatio;
	}

	public void setLightRatio(double lightRatio) {
		this.lightRatio = lightRatio;
	}

}
