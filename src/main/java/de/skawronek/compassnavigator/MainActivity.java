package de.skawronek.compassnavigator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements ITargetListListener,
		ICurrentLocationListener, SensorEventListener {

	private TextView targetView;
	private NavigatorView navigationView;

	private float currentAzimuth = NavigatorView.INVALID_BEARING;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		navigationView = (NavigatorView) findViewById(R.id.navigationImageView);
		targetView = (TextView) findViewById(R.id.targetTextView);
		try {
			final FileInputStream fos = openFileInput("targetlist");
			TargetList.getInstance().load(fos);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			final Toast toast = Toast.makeText(getApplicationContext(),
					"Unable to load target list", Toast.LENGTH_LONG);
			toast.show();
		}

		// Location
		final CurrentLocation cl = CurrentLocation.getInstance();
		cl.setContext(getApplicationContext());
		cl.addListener(this);
		
		Criteria criteria = cl.getCriteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_HIGH);
		criteria.setSpeedRequired(false);
		cl.setCriteria(criteria);

		// Kompass registrieren
		final SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		final Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);

		targetSelected(TargetList.getInstance().getSelectedTarget());
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Stop GPS
		CurrentLocation.getInstance().stop();

		// Save target list
		try {
			final FileOutputStream fos = openFileOutput("targetlist",
					Context.MODE_PRIVATE);
			TargetList.getInstance().save(fos);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(),
					"Unable to load target list", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// Start GPS
		CurrentLocation.getInstance().start();
		
		targetSelected(TargetList.getInstance().getSelectedTarget());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_select_target: {
			Intent intent = new Intent(this, SelectTargetActivity.class);
			startActivity(intent);
			return true;
		}
		case R.id.menu_edit_target_list: {
			Intent intent = new Intent(this, EditTargetListActivity.class);
			startActivity(intent);
			return true;
		}
		case R.id.menu_settings:
			return true;
		case R.id.menu_status:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void targetSelected(Target selectedTarget) {
		targetView.setText(formatTarget(selectedTarget));
		synchronizeNavigationView();
	}

	private SpannableString formatTarget(final Target target) {
		final String targetCaption = getString(R.string.target_caption);
		final String text = targetCaption
				+ (target != null ? target.getName()
						: getString(R.string.unknown_target_caption));
		final SpannableString spannable = new SpannableString(text);
		spannable.setSpan(new StyleSpan(Typeface.BOLD), targetCaption.length(),
				text.length(), 0);
		spannable.setSpan(new RelativeSizeSpan(1.4f), targetCaption.length(),
				text.length(), 0);
		return spannable;
	}

	@Override
	public void targetAdded(Target newTarget) {
		// TODO Auto-generated method stub

	}

	@Override
	public void targetRemoved(Target removedTarget) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		this.currentAzimuth = event.values[0];
		synchronizeNavigationView();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	private void synchronizeNavigationView() {
		Location currentLocation = CurrentLocation.getInstance().getLocation();
		if (currentLocation == null
				|| TargetList.getInstance().getSelectedTarget() == null) {
			navigationView.setBearing(NavigatorView.INVALID_BEARING);
			navigationView.setDistance(NavigatorView.INVALID_DISTANCE);
		} else {
			final double startLat = currentLocation.getLatitude();
			final double startLong = currentLocation.getLongitude();
			final Target selTarg = TargetList.getInstance().getSelectedTarget();
			final double endLat = selTarg.getLatitude();
			final double endLong = selTarg.getLongitude();
			final float[] results = new float[2];
			Location.distanceBetween(startLat, startLong, endLat, endLong,
					results);
			navigationView.setDistance((int) Math.round(results[0]));
			if (currentAzimuth != NavigatorView.INVALID_BEARING) {
				final float startBearing = results[1];
				final float azimuth = (currentAzimuth <= 180.0f ? currentAzimuth
						: -(360.0f - currentAzimuth));
				assert startBearing >= -180.0f && startBearing <= 180.0f;
				assert azimuth >= -180.0f && azimuth <= 180.0f;
				float bearing = startBearing - azimuth;
				if (bearing < 0.0f) {
					bearing = 360.0f + bearing;
				}
				assert bearing >= 0.0f && bearing <= 360.0f;
				navigationView.setBearing(bearing);
			}
		}
	}

	@Override
	public void locationChanged(Location newLocation) {
		synchronizeNavigationView();
	}
}
