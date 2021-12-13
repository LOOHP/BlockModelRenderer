package com.loohp.blockmodelrenderer.utils;

import com.loohp.blockmodelrenderer.render.Point2D;
import com.loohp.blockmodelrenderer.render.Point3D;

public class PointConversionUtils {
	
	public static Point2D convert(Point3D point3d, boolean flipY) {
		return new Point2D(point3d.x, point3d.y * (flipY ? -1 : 1));
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
