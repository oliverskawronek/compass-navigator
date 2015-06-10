package vertex.compassnavigator;

import vertex.compassnavigator.R;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
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
		LocationCompassHandler.ChangeListener {
	public static final String EXTRA_SELECTED_DESTINATION = "select";
	private static final int REQUEST_SELECT_DESTINATION = 1;

	private static final String TAG = NavigatorActivity.class.getName();

	private LocationCompassHandler locationCompassHandler;
	private Destination currentDestination = null;
	private Location currentLocation = LocationCompassHandler.UNKNOWN_LOCATION;
	private float currentAzimuth = LocationCompassHandler.UNKNOWN_AZIMUTH;

	private NavigatorView navigatorView;
	private TextView lblDestination;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigator);

		addMockContent();

		locationCompassHandler = new LocationCompassHandler(this);
		locationCompassHandler.setListener(this);

		navigatorView = (NavigatorView) findViewById(R.id.navigator_navigatorView);
		lblDestination = (TextView) findViewById(R.id.navigator_lblDestination);
	}

	@Override
	protected void onResume() {
		super.onResume();
		locationCompassHandler.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		locationCompassHandler.pause();
	}

	// TODO remove
	private void addMockContent() {
		final Destination fernsehturm = new Destination("Berlin, Fernsehturm",
				52.52160034123976, 13.406238555908203);
		final DestinationList list = DestinationList.getInstance();
		if (!list.contains(fernsehturm)) {
			list.add(fernsehturm);
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

	@Override
	public void onLocationChanged(final Location newLocation) {
		Log.i(TAG, "Loc: " + newLocation);
		this.currentLocation = newLocation;
		invalidateNavigation();
	}

	@Override
	public void onAzimuthChanged(final float azimuth) {
		this.currentAzimuth = azimuth;
		invalidateNavigation();
	}

	private void setCurrentDestination(final Destination dest) {
		currentDestination = dest;
		Log.i(TAG, "Current: " + currentDestination);
		lblDestination.setText(currentDestination + "");
		invalidateNavigation();
	}

	private void invalidateNavigation() {
		//@formatter:off
		final boolean distanceAvailable =
				currentLocation != LocationCompassHandler.UNKNOWN_LOCATION
				&& currentDestination != null;
		//@formatter:on
		if (distanceAvailable) {
			final int distance = Math.round(currentLocation
					.distanceTo(currentDestination.getLocation())); // in meter
			navigatorView.setDistance(distance);
		} else {
			navigatorView.setDistance(NavigatorView.UNKNOWN_DISTANCE);
		}

		//@formatter:off
		final boolean bearingAvailable =
				currentLocation != LocationCompassHandler.UNKNOWN_LOCATION
				&& Float.compare(currentAzimuth, LocationCompassHandler.UNKNOWN_AZIMUTH) != 0
				&& currentDestination != null;
		//@formatter:on
		if (bearingAvailable) {
			final float startBearing = currentDestination.getLocation()
					.bearingTo(currentLocation);
			float bearing = Util.positiveModulo(startBearing - currentAzimuth,
					360);
			// Log.i(TAG, bearing + "°");
			navigatorView.setBearing(bearing);
		} else {
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
}
