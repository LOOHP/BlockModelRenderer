package com.loohp.blockmodelrenderer.render;

public class Point3D {
	
	public double x;
	public double y;
	public double z;
	
	public Point3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public Point3D clone() {
		return new Point3D(this.x, this.y, this.z);
	}
	
	public double distance(Point3D other) {
		return Math.sqrt((this.x - other.x) * (this.x - other.x) + (this.y - other.y) * (this.y - other.y) + (this.z - other.z) * (this.z - other.z));
	}
	
	@Override
	public String toString() {
		return "[x=" + x + ", y=" + y + ", z=" + z + "]";
	}
	
}
