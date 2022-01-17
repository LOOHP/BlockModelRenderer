package com.loohp.blockmodelrenderer.render;

import java.util.Arrays;
import java.util.function.Supplier;

import com.loohp.blockmodelrenderer.utils.MathUtils;

public class ZBuffer {
	
	private int centerX;
	private int centerY;
	private int w;
	private int h;
	private double[][] zbuffer;

	public ZBuffer(int w, int h, int centerX, int centerY) {
		this.centerX = centerX;
		this.centerY = centerY;
		this.w = w;
		this.h = h;
		this.zbuffer = new double[w][h];
		for (int x = 0; x < w; x++) {
			Arrays.fill(zbuffer[x], -Double.MAX_VALUE);
		}
	}

	public double get(int x, int y) {
		return this.zbuffer[x + this.centerX][y + this.centerY];
	}
	
	public void set(int x, int y, double value) {
		this.zbuffer[x + this.centerX][y + this.centerY] = value;
	}
	
	public boolean compareAndSet(int x, int y, double value) {
		return compareAndSet(x, y, value, () -> true);
	}
	
	public boolean compareAndSet(int x, int y, double value, Supplier<Boolean> predicate) {
		double originalValue = this.zbuffer[x + this.centerX][y + this.centerY];
		if (MathUtils.greaterThanOrEquals(value, originalValue) && predicate.get()) {
			this.zbuffer[x + this.centerX][y + this.centerY] = Math.max(value, originalValue);
			return true;
		}
		return false;
	}

	public int getCenterX() {
		return centerX;
	}

	public int getCenterY() {
		return centerY;
	}

	public void setCenter(int centerX, int centerY) {
		this.centerX = centerX;
		this.centerY = centerY;
	}
	
	public int getMinX() {
		return -centerX;
	}
	
	public int getMinY() {
		return -centerY;
	}

	public int getMaxX() {
		return w - centerX;
	}

	public int getMaxY() {
		return h - centerY;
	}
	
}
