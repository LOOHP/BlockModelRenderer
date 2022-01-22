package com.loohp.blockmodelrenderer.render;

public class Point2D implements Cloneable {

    public double x;
    public double y;

    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public Point2D clone() {
        try {
            return (Point2D) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    public double distanceSquared(Point2D other) {
        return (this.x - other.x) * (this.x - other.x) + (this.y - other.y) * (this.y - other.y);
    }

    public double distance(Point2D other) {
        return Math.sqrt(distanceSquared(other));
    }

    public Point2D multiply(double m) {
        this.x *= m;
        this.y *= m;
        return this;
    }

    public Point2D multiply(double originX, double originY, double m) {
        this.x = (this.x - originX) * m + originX;
        this.x = (this.y - originY) * m + originY;
        return this;
    }

    @Override
    public String toString() {
        return "[x=" + x + ", y=" + y + "]";
    }

}
