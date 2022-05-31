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

package com.loohp.blockmodelrenderer.blending;

public class BlendingModes {

    public static final BlendingModes NORMAL = BlendingModes.of(BlendingMode.SRC_ALPHA, BlendingMode.ONE_MINUS_SRC_ALPHA, BlendingMode.ONE, BlendingMode.ONE_MINUS_SRC_ALPHA);
    public static final BlendingModes GLINT = BlendingModes.of(BlendingMode.SRC_COLOR, BlendingMode.ONE, BlendingMode.ZERO, BlendingMode.ONE);

    public static BlendingModes of(BlendingMode srcComposite, BlendingMode desComposite) {
        return of(srcComposite, desComposite, srcComposite, desComposite);
    }

    public static BlendingModes of(BlendingMode srcColorComposite, BlendingMode desColorComposite, BlendingMode srcAlphaComposite, BlendingMode desAlphaComposite) {
        return new BlendingModes(srcColorComposite, desColorComposite, srcAlphaComposite, desAlphaComposite);
    }

    private final BlendingMode srcColorComposite;
    private final BlendingMode desColorComposite;
    private final BlendingMode srcAlphaComposite;
    private final BlendingMode desAlphaComposite;

    private BlendingModes(BlendingMode srcColorComposite, BlendingMode desColorComposite, BlendingMode srcAlphaComposite, BlendingMode desAlphaComposite) {
        this.srcColorComposite = srcColorComposite;
        this.desColorComposite = desColorComposite;
        this.srcAlphaComposite = srcAlphaComposite;
        this.desAlphaComposite = desAlphaComposite;
    }

    public BlendingMode getSrcColorComposite() {
        return srcColorComposite;
    }

    public BlendingMode getDesColorComposite() {
        return desColorComposite;
    }

    public BlendingMode getSrcAlphaComposite() {
        return srcAlphaComposite;
    }

    public BlendingMode getDesAlphaComposite() {
        return desAlphaComposite;
    }

}
