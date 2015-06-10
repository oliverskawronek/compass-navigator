package vertex.compassnavigator;

import vertex.compassnavigator.DataSmoother.Smoothing;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public final class LocationCompassHandler implements LocationListener,
		SensorEventListener {
	private static final String TAG = LocationCompassHandler.class.getName();

	public static interface ChangeListener {
		public void onLocationChanged(Location newLocation);

		public void onAzimuthChanged(float azimuth);
	}

	public final static Location UNKNOWN_LOCATION = null;
	public final static float UNKNOWN_AZIMUTH = Float.NaN;

	private ChangeListener listener = null;
	// Geo location
	private final LocationManager locationManager;
	private String locationProvider;
	private Location currentLocation = UNKNOWN_LOCATION;

	// Compass: Accelelerometer, Magnetic Field
	private static final int NUM_SMOOTH_SAMPLES = 4;
	private static final Smoothing SMOOTHING = Smoothing.AVERAGE;

	private final SensorManager sensorManager;
	private final Sensor accelerometer;
	private final Sensor magnetometer;
	private final float[] gravity = new float[3];
	private float[] magneticField = new float[3];
	private float currentAzimuth = UNKNOWN_AZIMUTH;
	private final DataSmoother gravitySmoother = new DataSmoother(
			NUM_SMOOTH_SAMPLES, 3);
	private final DataSmoother magneticFieldSmoother = new DataSmoother(
			NUM_SMOOTH_SAMPLES, 3);

	public LocationCompassHandler(final Context context) {
		if (context == null) {
			throw new NullPointerException();
		}

		// Geo location
		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		locationProvider = LocationManager.GPS_PROVIDER;

		// Compass
		sensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	public void setListener(final ChangeListener listener) {
		this.listener = listener;
	}

	public void start() {
		// Geo location
		locationManager.requestLocationUpdates(locationProvider, 0, 0, this);
		final Location lastKnownLocation = locationManager
				.getLastKnownLocation(locationProvider);
		onLocationChanged(lastKnownLocation);
		// Compass
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(this, magnetometer,
				SensorManager.SENSOR_DELAY_FASTEST);

		Log.i(TAG, "Started");
	}

	public void pause() {
		// Geo location
		locationManager.removeUpdates(this);
		// Compass
		sensorManager.unregisterListener(this);
		gravitySmoother.clear();
		magneticFieldSmoother.clear();

		Log.i(TAG, "Paused");
	}

	public Location getLocation() {
		return currentLocation;
	}

	/**
	 * Returns azimuth angle in degrees between -180 and 180 where 0 indicates
	 * the geographical noth.
	 * 
	 * @return azimuth between -180..180
	 */
	public float getAzimuth() {
		return currentAzimuth;
	}

	@Override
	public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
		Log.i(TAG, "Accuracy changed for " + sensor.getName() + " to "
				+ accuracy);
	}

	private static final float RAD_TO_DEGREE = (float) (360 / (2 * Math.PI));

	@Override
	public void onSensorChanged(final SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			gravitySmoother.put(event.values);
			gravitySmoother.getSmoothed(gravity, SMOOTHING);
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			magneticFieldSmoother.put(event.values);
			magneticFieldSmoother.getSmoothed(magneticField, SMOOTHING);
			break;
		}

		if (gravity != null && magneticField != null) {
			final float rotMat[] = new float[9];
			final float inclinationMat[] = new float[9];
			final boolean success = SensorManager.getRotationMatrix(rotMat,
					inclinationMat, gravity, magneticField);
			if (success) {
				final float orientation[] = new float[3];
				SensorManager.getOrientation(rotMat, orientation);
				currentAzimuth = RAD_TO_DEGREE * orientation[0];
				notifyAzimuthChanged(currentAzimuth);
			}
		}
	}

	@Override
	public void onLocationChanged(final Location location) {
		if (isBetterLocation(location, currentLocation)) {
			currentLocation = location;
			notifyLocationChanged(currentLocation);
		}
	}

	@Override
	public void onProviderDisabled(final String provider) {
		Log.i(TAG, "Provider disabled " + provider);
	}

	@Override
	public void onProviderEnabled(final String provider) {
		Log.i(TAG, "Provider enabled " + provider);
	}

	@Override
	public void onStatusChanged(final String provider, final int status,
			final Bundle extras) {
		Log.i(TAG, "Status changed of " + provider + " to " + status);
	}

	private void notifyAzimuthChanged(final float azimuth) {
		if (listener != null) {
			listener.onAzimuthChanged(azimuth);
		}
	}

	private void notifyLocationChanged(final Location location) {
		if (listener != null) {
			listener.onLocationChanged(location);
		}
	}

	private static final long ONE_MINUTE = 60 * 1000; // in ms

	// Source:
	// http://developer.android.com/guide/topics/location/strategies.html
	private static boolean isBetterLocation(final Location location,
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
