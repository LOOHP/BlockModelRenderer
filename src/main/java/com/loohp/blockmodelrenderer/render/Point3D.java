package com.loohp.blockmodelrenderer.render;

public class Point3D implements Cloneable {

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
        try {
            return (Point3D) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    public double distanceSquared(Point3D other) {
        return (this.x - other.x) * (this.x - other.x) + (this.y - other.y) * (this.y - other.y) + (this.z - other.z) * (this.z - other.z);
    }

    public double distance(Point3D other) {
        return Math.sqrt(distanceSquared(other));
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }

    @Override
    public String toString() {
        return "[x=" + x + ", y=" + y + ", z=" + z + "]";
    }

}
