package com.loohp.blockmodelrenderer.render;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import com.loohp.blockmodelrenderer.utils.ColorUtils;
import com.loohp.blockmodelrenderer.utils.ImageUtils;
import com.loohp.blockmodelrenderer.utils.MathUtils;
import com.loohp.blockmodelrenderer.utils.PlaneUtils;
import com.loohp.blockmodelrenderer.utils.PointConversionUtils;

public class Face implements ITransformable {
	
	public static final Comparator<Face> AVERAGE_DEPTH_COMPARATOR = Comparator.comparing(face -> face.getAverageZ());
	
	public BufferedImage image;
	public BufferedImage overlay;
	public double overlayAdditionFactor;
	protected Face oppositeFace;
	private double lightRatio;
	private Point3D[] points;
	private Vector[][] axis;
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
		this.overlay = null;
		this.overlayAdditionFactor = 1;
		this.points = new Point3D[points.length];
		this.axis = new Vector[points.length][];
		for (int i = 0; i < points.length; i++) {
			this.points[i] = points[i].clone();
			this.axis[i] = new Vector[] {new Vector(1, 0, 0), new Vector(0, 1, 0), new Vector(0, 0, 1)};
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

	public boolean pointsEquals(Face other) {
		Set<Point3D> ourPoints = new HashSet<>(Arrays.asList(this.points));
		for (Point3D point : other.points) {
			if (ourPoints.stream().noneMatch(each -> MathUtils.equals(each.x, point.x) && MathUtils.equals(each.y, point.y) && MathUtils.equals(each.z, point.z))) {
				return false;
			}
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
	
	public void render(Graphics2D g) {
		render(g, null, null);
	}

	public void render(Graphics2D masterG, BufferedImage masterImage, ZBuffer zBuffer) {
		if (image != null) {
			if (!ignoreZFight) {
				if (oppositeFace != null && AVERAGE_DEPTH_COMPARATOR.compare(this, oppositeFace) <= 0) {
					if (oppositeFace.priority > this.priority || oppositeFace.getAverageZ() - this.getAverageZ() > 0.1) {
						return;
					}
				}
			}
			
			Point2D[] points2d = new Point2D[points.length];
			for (int i = 0; i < points.length; i++) {
				points2d[i] = PointConversionUtils.convert(points[i], true);
			}
			
			BufferedImage image = ImageUtils.multiply(ImageUtils.copyImage(this.image), lightRatio);
			if (overlay != null) {
				image = ImageUtils.additionNonTransparent(image, overlay, overlayAdditionFactor);
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
			
			AffineTransform orginalTransform = (AffineTransform) masterG.getTransform().clone();
			
			Graphics2D g;
			BufferedImage temp = null;
			if (zBuffer == null || ignoreZFight) {
				g = masterG;
			} else {
				temp = new BufferedImage(masterImage.getWidth(), masterImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
				g = temp.createGraphics();
				g.setTransform(orginalTransform);
			}
			
			AffineTransform transform = AffineTransform.getTranslateInstance(points2d[0].x, points2d[0].y);
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
			g.transform(transform);
			g.drawImage(image, 0, 0, null);
			
			if (temp != null) {
				int xDiff = zBuffer.getCenterX();
				int yDiff = zBuffer.getCenterY();
				for (int y = zBuffer.getMinY(); y < zBuffer.getMaxY(); y++) {
					for (int x = zBuffer.getMinX(); x < zBuffer.getMaxX(); x++) {
						int fixedX = x + xDiff;
						int fixedY = y + yDiff;
						int masterColor = masterImage.getRGB(fixedX, fixedY);
						int masterAlpha = ColorUtils.getAlpha(masterColor);
						int color = temp.getRGB(fixedX, fixedY);
						int alpha = ColorUtils.getAlpha(color);
						boolean isCloser = zBuffer.compareAndSet(x, y, getDepthAt(x / orginalTransform.getScaleX(), -y / orginalTransform.getScaleY()), () -> alpha > 0);
						if (isCloser || masterAlpha < 255) {
							if (!isCloser) {
								masterImage.setRGB(fixedX, fixedY, ColorUtils.composite(masterColor, color));
							} else if (alpha >= 255) {
								masterImage.setRGB(fixedX, fixedY, color);
							} else if (alpha > 0) {
								masterImage.setRGB(fixedX, fixedY, ColorUtils.composite(color, masterColor));
							}
						}
					}
				}
			}
			
			if (temp == null) {
				g.setTransform(orginalTransform);
			} else {
				g.dispose();
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
