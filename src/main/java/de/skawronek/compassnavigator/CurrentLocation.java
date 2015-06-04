package de.skawronek.compassnavigator;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class CurrentLocation implements LocationListener {
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	private final String LOG_TAG = "de.skawronek.compassnavigator";

	private static CurrentLocation instance;

	private final List<ICurrentLocationListener> listeners = new ArrayList<ICurrentLocationListener>();

	private Context context = null;
	private LocationManager locationManager = null;
	private String provider = null;
	private boolean started = false;

	private Criteria criteria = new Criteria();
	private Location location = null;

	private CurrentLocation() {
	}

	public static synchronized CurrentLocation getInstance() {
		if (instance == null) {
			instance = new CurrentLocation();
		}
		return instance;
	}

	public Criteria getCriteria() {
		return new Criteria(this.criteria);
	}

	public void setCriteria(final Criteria criteria) {
		this.criteria = new Criteria(criteria);
	}

	public void setContext(Context context) {
		if (this.context != context) {
			if (started) {
				stop();
			}
			this.context = context;
			this.locationManager = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);
			if (this.locationManager == null) {
				throw new RuntimeException("unable to request LocationManager");
			}
			if (!started) {
				start();
			}
		}
	}

	public void start() {
		if (started) {
			stop();
		}

		if (this.context == null) {
			throw new IllegalStateException("please call setContext before");
		} else {
			handleLocationProvider();
			started = true;
		}
		Log.d(LOG_TAG, "started");
	}

	public void stop() {
		if (!started) {
			throw new IllegalStateException("not started");
		} else {
			locationManager.removeUpdates(this);
			started = false;
			Log.d(LOG_TAG, "stopped");
		}
	}

	public Location getLocation() {
		if (location == null) {
			return null;
		} else {
			return new Location(location);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if (isBetterLocation(location)) {
			this.location = location;
			Log.d(LOG_TAG, "new location: (lat=" + location.getLatitude()
					+ ", long=" + location.getLongitude());
			notifyLocationChanged();
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		handleLocationProvider();
	}

	@Override
	public void onProviderEnabled(String provider) {
		handleLocationProvider();
	}

	@Override
	public void onProviderDisabled(String provider) {
		handleLocationProvider();
	}

	private void handleLocationProvider() {
		final String bestProvider = locationManager.getBestProvider(criteria,
				true);
		if (bestProvider != null) {
			if (!isSameProvider(bestProvider, this.provider)) {
				Log.d(LOG_TAG, "provider changed, old provider: "
						+ this.provider + ", new provider: " + bestProvider);
				Toast.makeText(
						context,
						"provider changed, old provider: " + this.provider
								+ ", new provider: " + bestProvider,
						Toast.LENGTH_LONG).show();
				locationManager.removeUpdates(this);
				this.provider = bestProvider;
				locationManager.requestLocationUpdates(this.provider, 0, 0.0f,
						this);

				Location newLocation = locationManager
						.getLastKnownLocation(this.provider);
				if (newLocation != null) {
					if (isBetterLocation(newLocation)) {
						this.location = newLocation;
						notifyLocationChanged();
					}
				}
			}
		} else {
			stop();
		}
	}

	// source:
	// http://developer.android.com/guide/topics/location/strategies.html
	private boolean isBetterLocation(Location location) {
		if (this.location == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - this.location.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

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
		int accuracyDelta = (int) (location.getAccuracy() - this.location
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				this.location.getProvider());

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

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	public void addListener(final ICurrentLocationListener listener) {
		listeners.add(listener);
	}

	public void removeListener(final ICurrentLocationListener listener) {
		listeners.remove(listener);
	}

	private void notifyLocationChanged() {
		for (ICurrentLocationListener listener : listeners) {
			listener.locationChanged(new Location(location));
		}
	}
}
