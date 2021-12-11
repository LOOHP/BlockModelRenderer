package com.loohp.blockmodelrenderer.render;

public class Point2D {
	
	public double x;
	public double y;
	
	public Point2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public Point2D clone() {
		return new Point2D(this.x, this.y);
	}
	
	public double distance(Point2D other) {
		return Math.sqrt((this.x - other.x) * (this.x - other.x) + (this.y - other.y) * (this.y - other.y));
	}
	
}
