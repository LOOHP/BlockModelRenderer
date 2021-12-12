package com.loohp.blockmodelrenderer.render;

public class Vector {
	
	private double x;
	private double y;
	private double z;
	
	public Vector() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}
	
	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector(Point3D p1, Point3D p2) {
		this.x = p2.x - p1.x;
		this.y = p2.y - p1.y;
		this.z = p2.z - p1.z;
	}
	
	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	@Override
	public Vector clone() {
		return new Vector(x, y, z);
	}
	
	public double dot(Vector other) {
        return x * other.x + y * other.y + z * other.z;
    }
	
	public Vector cross(Vector o) {
        double newX = y * o.z - o.y * z;
        double newY = z * o.x - o.z * x;
        double newZ = x * o.y - o.x * y;

        x = newX;
        y = newY;
        z = newZ;
        return this;
    }
	
	public Vector normalize() {
		double magnitude = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
		this.x = this.x / magnitude;
		this.y = this.y / magnitude;
		this.z = this.z / magnitude;
		return this;
	}
	
	 public Vector add(Vector v) {
         this.x = x + v.x;
         this.y = y + v.y;
         this.z = z + v.z;
         return this;
     }

	 public Vector subtract(Vector v) {
         this.x = x - v.x;
         this.y = y - v.y;
         this.z = z - v.z;
         return this;
     }

	 public Vector multiply(double s) {
         this.x = x * s;
         this.y = y * s;
         this.z = z * s;
         return this;
     }
	 
	 public double distance(Vector other) {
		 return Math.sqrt((this.x - other.x) * (this.x - other.x) + (this.y - other.y) * (this.y - other.y) + (this.z - other.z) * (this.z - other.z));
	 }

}