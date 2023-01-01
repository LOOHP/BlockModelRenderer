/*
 * This file is part of BlockModelRenderer.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.blockmodelrenderer.utils;

import com.loohp.blockmodelrenderer.render.Point2D;
import com.loohp.blockmodelrenderer.render.Point3D;

public class PointConversionUtils {

    public static Point2D convert(Point3D point3d, boolean flipY) {
        return new Point2D(point3d.x, flipY ? -point3d.y : point3d.y);
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

    public static void flipAboutPlane(Point3D p, boolean x, boolean y, boolean z) {
        if (y && z) {
            p.x = -p.x;
        }
        if (x && z) {
            p.y = -p.y;
        }
        if (x && y) {
            p.z = -p.z;
        }
    }

}
