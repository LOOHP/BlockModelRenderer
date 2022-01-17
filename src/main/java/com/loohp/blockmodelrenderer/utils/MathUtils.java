package com.loohp.blockmodelrenderer.utils;

public class MathUtils {
	
	private final static double EPSILON = 0.000001;

	public static boolean equals(double a, double b){
	    return a == b ? true : Math.abs(a - b) < EPSILON;
	}

	public static boolean equals(double a, double b, double epsilon){
	    return a == b ? true : Math.abs(a - b) < epsilon;
	}

	public static boolean greaterThan(double a, double b){
	    return greaterThan(a, b, EPSILON);
	}

	public static boolean greaterThan(double a, double b, double epsilon){
	    return a - b > epsilon;
	}
	
	public static boolean greaterThanOrEquals(double a, double b){
	    return greaterThanOrEquals(a, b, EPSILON);
	}

	public static boolean greaterThanOrEquals(double a, double b, double epsilon){
	    return a - b > epsilon || equals(a, b, epsilon);
	}

	public static boolean lessThan(double a, double b){
	    return lessThan(a, b, EPSILON);
	}

	public static boolean lessThan(double a, double b, double epsilon){
	    return b - a > epsilon;
	}
	
	public static boolean lessThanOrEquals(double a, double b){
	    return lessThanOrEquals(a, b, EPSILON);
	}

	public static boolean lessThanOrEquals(double a, double b, double epsilon){
	    return b - a > epsilon || equals(a, b, epsilon);
	}

}
