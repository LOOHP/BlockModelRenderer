package com.loohp.blockmodelrenderer.utils;

import com.loohp.blockmodelrenderer.render.Point2D;

public class PlaneUtils {

	public static boolean contains(Point2D test, Point2D[] points) {
		int i;
		int j;
		boolean result = false;
		for (i = 0, j = points.length - 1; i < points.length; j = i++) {
			if (MathUtils.greaterThanOrEquals(points[i].y, test.y) != MathUtils.greaterThanOrEquals(points[j].y, test.y) && MathUtils.lessThan(test.x, (points[j].x - points[i].x) * (test.y - points[i].y) / (points[j].y - points[i].y) + points[i].x)) {
				result = !result;
			}
		}
		return result;
	}

}
