package com.loohp.blockmodelrenderer.utils;

public class ColorUtils {
	
	public static int composite(int foregroundInt, int backgroundInt) {
		//Ar = As + Ad*(1-As)
		//Cr = Cs + Cd*(1-As)
		int backgroundAlpha = getAlpha(backgroundInt);
		int foregroundAlpha = getAlpha(foregroundInt);
		
		double factor = 1.0 - foregroundAlpha / 255.0;
		double alpha = foregroundAlpha + backgroundAlpha * factor;
		int red = (int) Math.round((getRed(foregroundInt) * foregroundAlpha + getRed(backgroundInt) * backgroundAlpha * factor) / alpha);
		int green = (int) Math.round((getGreen(foregroundInt) * foregroundAlpha + getGreen(backgroundInt) * backgroundAlpha * factor) / alpha);
		int blue = (int) Math.round((getBlue(foregroundInt) * foregroundAlpha + getBlue(backgroundInt) * backgroundAlpha * factor) / alpha);
		return getIntFromColor(red, green, blue, (int) Math.round(alpha));
	}
	
	public static int getRed(int color) {
		return (color >> 16) & 0xFF;
	}
	
	public static int getGreen(int color) {
		return (color >> 8) & 0xFF;
	}
	
	public static int getBlue(int color) {
		return color & 0xFF;
	}
	
	public static int getAlpha(int color) {
		return (color >> 24) & 0xFF;
	}
	
	public static int getIntFromColor(int r, int g, int b, int a){
	    int red = (r << 16) & 0x00FF0000;
	    int green = (g << 8) & 0x0000FF00;
	    int blue = b & 0x000000FF;
	    int alpha = (a << 24) & 0xFF000000;

	    return alpha | red | green | blue;
	}

}
