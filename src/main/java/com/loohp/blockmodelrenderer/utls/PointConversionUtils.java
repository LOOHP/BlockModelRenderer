package com.loohp.blockmodelrenderer.utls;

import com.loohp.blockmodelrenderer.render.Point2D;
import com.loohp.blockmodelrenderer.render.Point3D;

public class PointConversionUtils {
	
	public static Point2D convert(Point3D point3d) {
		return new Point2D(point3d.x, -point3d.y);
	}
    
	public static void rotateAxisX(Point3D p, double degrees) {
        double radius = Math.sqrt(p.y * p.y + p.z * p.z);
        double theta = Math.atan2(p.z, p.y);
        theta += 2 * Math.PI / 360 * degrees;
        p.y = radius * Math.cos(theta);
        p.z = radius * Math.sin(theta);
    }
    
    public static void rotateAxisY(Point3D p, double degrees) {
        double radius = Math.sqrt(p.x * p.x + p.z * p.z);
        double theta = Math.atan2(p.x, p.z);
        theta += 2 * Math.PI / 360 * degrees;
        p.z = radius * Math.cos(theta);
        p.x = radius * Math.sin(theta);
    }
        
    public static void rotateAxisZ(Point3D p, double degrees) {
        double radius = Math.sqrt(p.x * p.x + p.y * p.y);
        double theta = Math.atan2(p.y, p.x);
        theta += 2 * Math.PI / 360 * degrees;
        p.x = radius * Math.cos(theta);
        p.y = radius * Math.sin(theta);
    }
    
    public static void translate(Point3D p, double x, double y, double z) {
    	p.x += x;
    	p.y += y;
    	p.z += z;
    }
    
    public static void scale(Point3D p, double x, double y, double z) {
    	p.x *= x;
    	p.y *= y;
    	p.z *= z;
    }

}
