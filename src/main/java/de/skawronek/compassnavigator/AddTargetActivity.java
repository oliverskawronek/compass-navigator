package de.skawronek.compassnavigator;

import android.location.Location;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddTargetActivity extends Activity implements OnClickListener {
	private EditText nameEditText;
	private EditText latitudeEditText;
	private EditText longitudeEditText;
	private Button setCurrentLocation;
	private Button finishButton;
	private Button cancelButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_target);

		nameEditText = (EditText) findViewById(R.id.nameEditText);
		latitudeEditText = (EditText) findViewById(R.id.latitudeEditText);
		longitudeEditText = (EditText) findViewById(R.id.longitudeEditText);
		setCurrentLocation = (Button) findViewById(R.id.setCurrentLocationButton);
		setCurrentLocation.setOnClickListener(this);
		finishButton = (Button) findViewById(R.id.finishButton);
		finishButton.setOnClickListener(this);
		cancelButton = (Button) findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setCurrentLocation.setEnabled(CurrentLocation.getInstance()
				.getLocation() != null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_add_target, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		if (v == setCurrentLocation) {
			final Location currentLocation = CurrentLocation.getInstance()
					.getLocation();
			latitudeEditText.setText(String.valueOf(currentLocation
					.getLatitude()));
			longitudeEditText.setText(String.valueOf(currentLocation
					.getLongitude()));
		} else if (v == finishButton) {
			if (nameEditText.getText().toString().length() == 0) {
				Toast.makeText(this, "Please set name", Toast.LENGTH_LONG)
						.show();
			} else if (nameEditText.getText().toString().contains("\n")) {
				Toast.makeText(this, "Please do not use newline in the name",
						Toast.LENGTH_LONG).show();
			} else if (latitudeEditText.getText().toString().length() == 0) {
				Toast.makeText(this, "Please set latitude", Toast.LENGTH_LONG)
						.show();
			} else if (longitudeEditText.getText().toString().length() == 0) {
				Toast.makeText(this, "Please set longitude", Toast.LENGTH_LONG)
						.show();
			} else {
				final Target newTarget = new Target();
				newTarget.setName(nameEditText.getText().toString());
				newTarget.setLatitude(Double.parseDouble(latitudeEditText
						.getText().toString().replace(',', '.')));
				newTarget.setLongitude(Double.parseDouble(longitudeEditText
						.getText().toString().replace(',', '.')));

				if (TargetList.getInstance().getTargets().contains(newTarget)) {
					Toast.makeText(this, "Target allready exists",
							Toast.LENGTH_LONG).show();
				} else {
					TargetList.getInstance().addTarget(newTarget);
					Toast.makeText(this, "Target added", Toast.LENGTH_SHORT)
							.show();
					finish();
				}
			}
		} else if (v == cancelButton) {
			finish();
		}
	}
}
