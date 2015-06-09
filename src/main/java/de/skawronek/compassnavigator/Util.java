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

	private static final long ONE_MINUTE = 60 * 1000; // in ms

	// Source:
	// http://developer.android.com/guide/topics/location/strategies.html
	public static boolean isBetterLocation(final Location location,
			final Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		final long timeDelta = location.getTime()
				- currentBestLocation.getTime();
		final boolean isSignificantlyNewer = timeDelta > ONE_MINUTE;
		final boolean isSignificantlyOlder = timeDelta < -ONE_MINUTE;
		final boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		final int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		final boolean isLessAccurate = accuracyDelta > 0;
		final boolean isMoreAccurate = accuracyDelta < 0;
		final boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		final boolean isFromSameProvider = isSameProvider(
				location.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	private static boolean isSameProvider(final String provider1,
			final String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
}
