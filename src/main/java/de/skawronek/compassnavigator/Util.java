package de.skawronek.compassnavigator;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import android.location.Location;

public final class Util {
	private Util() {
	}

	private static final NumberFormat NUMBER_FORMAT;
	static {
		final DecimalFormat format = new DecimalFormat("0.000000");
		DecimalFormatSymbols dot = new DecimalFormatSymbols();
		dot.setDecimalSeparator('.');
		format.setDecimalFormatSymbols(dot);
		NUMBER_FORMAT = format;
	}

	public static String formatGeoCoord(final double coord) {
		return NUMBER_FORMAT.format(coord);
	}

	public static String formatLocation(final Location loc) {
		final double lat = loc.getLatitude();
		final double lon = loc.getLongitude();
		return String
				.format("%s, %s", formatGeoCoord(lat), formatGeoCoord(lon));
	}

	public static int positiveModulo(final int a, final int b) {
		return (a % b + b) % b;
	}

	public static float positiveModulo(final float a, final float b) {
		return (a % b + b) % b;
	}

	public static double positiveModulo(final double a, final double b) {
		return (a % b + b) % b;
	}


	public static float average(final float[] x) {
		if (x == null) {
			throw new NullPointerException();
		} else if (x.length == 0) {
			throw new IllegalArgumentException("x is empty");
		}
		
		float avg = 0;

		for (int i = 0; i < x.length; i++) {
			avg += x[i];
		}
		avg /= x.length;
		return avg;
	}
	
	// Source: http://ndevilla.free.fr/median/median/src/torben.c
	public static float median(final float[] x) {
		if (x == null) {
			throw new NullPointerException();
		} else if (x.length == 0) {
			throw new IllegalArgumentException("x is empty");
		}

		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		for (int i = 0; i < x.length; i++) {
			if (x[i] < min) {
				min = x[i];
			}
			if (x[i] > max) {
				max = x[i];
			}
		}

		int lessCount;
		int equalCount;
		int greaterCount;
		float maxLtGuess;
		float minGtGuess;

		while (true) {
			final float guess = (min + max) / 2f;

			lessCount = 0;
			equalCount = 0;
			greaterCount = 0;

			maxLtGuess = min;
			minGtGuess = max;
			for (int i = 0; i < x.length; i++) {
				if (x[i] < guess) {
					lessCount++;
					if (x[i] > maxLtGuess) {
						maxLtGuess = x[i];
					}
				} else if (x[i] > guess) {
					greaterCount++;
					if (x[i] < minGtGuess) {
						minGtGuess = x[i];
					}
				} else {
					equalCount++;
				}
			}

			if (lessCount <= (x.length + 1) / 2
					&& greaterCount <= (x.length + 1) / 2) {
				// End
				if (lessCount >= (x.length + 1) / 2) {
					return maxLtGuess;
				} else if (lessCount + equalCount >= (x.length + 1) / 2) {
					return guess;
				} else {
					return minGtGuess;
				}
			} else if (lessCount > greaterCount) {
				max = maxLtGuess;
			} else {
				min = minGtGuess;
			}
		}
	}
}
