package de.skawronek.compassnavigator;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class NavigatorActivity extends ActionBarActivity implements
		LocationListener {
	public static final String EXTRA_SELECTED_DESTINATION = "select";
	private static final int REQUEST_SELECT_DESTINATION = 1;

	private static final String TAG = NavigatorActivity.class.getName();

	private Destination currentDestination = null;
	private Location currentLocation = null;

	private NavigatorView navigatorView;
	private TextView lblDestination;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigator);

		addMockContent();

		navigatorView = (NavigatorView) findViewById(R.id.navigator_navigatorView);
		lblDestination = (TextView) findViewById(R.id.navigator_lblDestination);

		final LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, this);
		final Location lastKnownLocation = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		onLocationChanged(lastKnownLocation);
	}

	// TODO remove
	private void addMockContent() {
		DestinationList.getInstance().add(
				new Destination("Berlin, Fernsehturm", 52.52160034123976,
						13.406238555908203));
		DestinationList.getInstance().add(
				new Destination("Leipzig, Völkerschlachtdenkmal", -40.123,
						-10.456));
		for (int i = 0; i < 50; i++) {
			DestinationList.getInstance().add(
					new Destination("" + System.currentTimeMillis(), -40.123,
							Math.random() * 20));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.navigator_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_select_destination:
			onSelectDestinationAction();
			return true;
		case R.id.action_add_destination:
			onAddDestinationAction();
			return true;
		case R.id.action_delete_destinations:
			onDeleteDestinationsAction();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void onSelectDestinationAction() {
		final Intent intent = new Intent(this, SelectDestinationActivity.class);
		startActivityForResult(intent, REQUEST_SELECT_DESTINATION);
	}

	private void onAddDestinationAction() {
		final Intent intent = new Intent(this, AddDestinationActivity.class);
		startActivity(intent);
	}

	private void onDeleteDestinationsAction() {
		final Intent intent = new Intent(this, DeleteDestinationsActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_SELECT_DESTINATION) {
			if (resultCode == RESULT_OK) {
				final Destination tl = (Destination) data
						.getSerializableExtra(EXTRA_SELECTED_DESTINATION);
				setCurrentDestination(tl);
			}
		}
	}

	private void setCurrentDestination(final Destination dest) {
		currentDestination = dest;
		Log.i(TAG, "Current: " + currentDestination);
		lblDestination.setText(currentDestination + "");
		invalidateNavigation();
	}

	@Override
	public void onLocationChanged(final Location location) {
		if (Util.isBetterLocation(location, currentLocation)) {
			setCurrentLocation(location);
		}
	}

	private void setCurrentLocation(final Location currentLocation) {
		this.currentLocation = currentLocation;
		invalidateNavigation();
	}

	private void invalidateNavigation() {
		if (currentLocation != null && currentDestination != null) {
			final int distance = Math.round(currentLocation
					.distanceTo(currentDestination.getLocation())); // in meter
			final float bearing = currentLocation.bearingTo(currentDestination
					.getLocation());
			navigatorView.setDistance(distance);
			// TODO use compass
			navigatorView.setBearing(bearing + 180);
		} else {
			navigatorView.setDistance(NavigatorView.UNKNOWN_DISTANCE);
			navigatorView.setBearing(NavigatorView.UNKNOWN_BEARING);
		}

		lblDestination.setText(formatDestination(currentDestination));
	}

	private SpannableString formatDestination(final Destination dest) {
		final String destinationTemplate = getString(R.string.navigator_destination);

		final String strDest;
		if (dest != null) {
			strDest = dest.getName();
		} else {
			final String unknown = getString(R.string.unknown_destination);
			strDest = unknown;
		}
		final String text = String.format(destinationTemplate, strDest);

		final int spanStart = text.length() - strDest.length(); // incl.
		final int spanEnd = text.length(); // excl.
		final SpannableString spannable = new SpannableString(text);
		spannable.setSpan(new StyleSpan(Typeface.BOLD), spanStart, spanEnd, 0);
		spannable.setSpan(new RelativeSizeSpan(1.4f), spanStart, spanEnd, 0);
		return spannable;
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
