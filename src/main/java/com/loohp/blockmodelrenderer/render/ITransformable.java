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

package com.loohp.blockmodelrenderer.render;

public interface ITransformable {

    void scale(double x, double y, double z);

    void translate(double x, double y, double z);

    void rotate(double x, double y, double z, boolean saveAxis);

    void flipAboutPlane(boolean x, boolean y, boolean z);

    void updateLighting(Vector direction, double ambient, double max);

}
