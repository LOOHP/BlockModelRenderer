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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;

public class DataSerializationUtils {

    public static void writeImage(BufferedImage image, DataOutputStream out) throws IOException {
        ImageIO.write(image, "png", out);
    }

    public static BufferedImage readImage(DataInputStream in) throws IOException {
        return ImageIO.read(in);
    }

    public static <T> void writeArray(T[] array, DataOutputStream out, boolean nullable, IOBiConsumer<T, DataOutputStream> writeFunction) throws IOException {
        out.writeInt(array.length);
        for (T t : array) {
            if (nullable && t == null) {
                out.writeBoolean(false);
            } else {
                if (nullable) {
                    out.writeBoolean(true);
                }
                writeFunction.accept(t, out);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] readArray(Class<T> clazz, DataInputStream in, boolean nullable, IOFunction<DataInputStream, T> readFunction) throws IOException {
        int length = in.readInt();
        T[] array = (T[]) Array.newInstance(clazz, length);
        for (int i = 0; i < length; i++) {
            if (nullable && !in.readBoolean()) {
                continue;
            }
            array[i] = readFunction.apply(in);
        }
        return array;
    }

    public static <T> void writeNullable(T t, DataOutputStream out, IOBiConsumer<T, DataOutputStream> writeFunction) throws IOException {
        if (t == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            writeFunction.accept(t, out);
        }
    }

    public static <T> T readNullable(Class<T> clazz, DataInputStream in, IOFunction<DataInputStream, T> readFunction) throws IOException {
        if (in.readBoolean()) {
            return readFunction.apply(in);
        } else {
            return null;
        }
    }

    @FunctionalInterface
    public interface IOBiConsumer<T, U> {

        void accept(T t, U u) throws IOException;

    }

    @FunctionalInterface
    public interface IOFunction<T, R> {

        R apply(T t) throws IOException;

    }
}
