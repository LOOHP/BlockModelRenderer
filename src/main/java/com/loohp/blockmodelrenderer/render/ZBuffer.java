package com.loohp.blockmodelrenderer.render;

import java.util.Arrays;

public class ZBuffer {
	
	private int centerX;
	private int centerY;
	private int w;
	private int h;
	private double[][] zbuffer;
	private boolean ignoreBuffer;

	public ZBuffer(int w, int h, int centerX, int centerY) {
		this.centerX = centerX;
		this.centerY = centerY;
		this.w = w;
		this.h = h;
		this.zbuffer = new double[w][h];
		for (int x = 0; x < w; x++) {
			Arrays.fill(zbuffer[x], -Double.MAX_VALUE);
		}
		this.ignoreBuffer = false;
	}
	
	public boolean ignoreBuffer() {
		return ignoreBuffer;
	}

	public void setIgnoreBuffer(boolean ignoreBuffer) {
		this.ignoreBuffer = ignoreBuffer;
	}

	public double get(int x, int y) {
		return this.zbuffer[x + this.centerX][y + this.centerY];
	}
	
	public void set(int x, int y, double value) {
		this.zbuffer[x + this.centerX][y + this.centerY] = value;
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
