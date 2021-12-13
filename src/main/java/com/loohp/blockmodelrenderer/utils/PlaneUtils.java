package com.loohp.blockmodelrenderer.utils;

import com.loohp.blockmodelrenderer.render.Point2D;

public class PlaneUtils {
	
	public static boolean contains(double x, double y, Point2D[] points) {
        int hits = 0;

        double lastx = points[points.length - 1].x;
        double lasty = points[points.length - 1].y;
        double curx;
		double cury;

        // Walk the edges of the polygon
        for (int i = 0; i < points.length; lastx = curx, lasty = cury, i++) {
            curx = points[i].x;
            cury = points[i].y;

            if (cury == lasty) {
                continue;
            }

            double leftx;
            if (curx < lastx) {
                if (x >= lastx) {
                    continue;
                }
                leftx = curx;
            } else {
                if (x >= curx) {
                    continue;
                }
                leftx = lastx;
            }

            double test1, test2;
            if (cury < lasty) {
                if (y < cury || y >= lasty) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - curx;
                test2 = y - cury;
            } else {
                if (y < lasty || y >= cury) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - lastx;
                test2 = y - lasty;
            }

            if (test1 < (test2 / (lasty - cury) * (lastx - curx))) {
                hits++;
            }
        }

        return ((hits & 1) != 0);
    }

}
