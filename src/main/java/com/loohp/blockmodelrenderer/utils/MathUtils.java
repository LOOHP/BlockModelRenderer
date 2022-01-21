package com.loohp.blockmodelrenderer.utils;

public class MathUtils {
	
	public static final double NEGATIVE_MAX_DOUBLE = -Double.MAX_VALUE;
	
	private final static float EPSILON_FLOAT = 0.000001f;
	private final static double EPSILON_DOUBLE = 0.000001;

	public static boolean equals(float a, float b) {
	    return a == b ? true : Math.abs(a - b) < EPSILON_FLOAT;
	}

	public static boolean equals(float a, float b, float epsilon) {
	    return a == b ? true : Math.abs(a - b) < epsilon;
	}

	public static boolean greaterThan(float a, float b) {
	    return greaterThan(a, b, EPSILON_FLOAT);
	}

	public static boolean greaterThan(float a, float b, float epsilon) {
	    return a - b > epsilon;
	}
	
	public static boolean greaterThanOrEquals(float a, float b) {
	    return greaterThanOrEquals(a, b, EPSILON_FLOAT);
	}

	public static boolean greaterThanOrEquals(float a, float b, float epsilon) {
	    return a - b > epsilon || equals(a, b, epsilon);
	}

	public static boolean lessThan(float a, float b) {
	    return lessThan(a, b, EPSILON_FLOAT);
	}

	public static boolean lessThan(float a, float b, float epsilon) {
	    return b - a > epsilon;
	}
	
	public static boolean lessThanOrEquals(float a, float b) {
	    return lessThanOrEquals(a, b, EPSILON_FLOAT);
	}

	public static boolean lessThanOrEquals(float a, float b, float epsilon) {
	    return b - a > epsilon || equals(a, b, epsilon);
	}

	public static boolean equals(double a, double b) {
	    return a == b ? true : Math.abs(a - b) < EPSILON_DOUBLE;
	}

	public static boolean equals(double a, double b, double epsilon) {
	    return a == b ? true : Math.abs(a - b) < epsilon;
	}

	public static boolean greaterThan(double a, double b) {
	    return greaterThan(a, b, EPSILON_DOUBLE);
	}

	public static boolean greaterThan(double a, double b, double epsilon) {
	    return a - b > epsilon;
	}
	
	public static boolean greaterThanOrEquals(double a, double b) {
	    return greaterThanOrEquals(a, b, EPSILON_DOUBLE);
	}

	public static boolean greaterThanOrEquals(double a, double b, double epsilon) {
	    return a - b > epsilon || equals(a, b, epsilon);
	}

	public static boolean lessThan(double a, double b) {
	    return lessThan(a, b, EPSILON_DOUBLE);
	}

	public static boolean lessThan(double a, double b, double epsilon) {
	    return b - a > epsilon;
	}
	
	public static boolean lessThanOrEquals(double a, double b) {
	    return lessThanOrEquals(a, b, EPSILON_DOUBLE);
	}

	public static boolean lessThanOrEquals(double a, double b, double epsilon) {
	    return b - a > epsilon || equals(a, b, epsilon);
	}

}
