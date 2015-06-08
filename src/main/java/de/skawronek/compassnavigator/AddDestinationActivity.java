package de.skawronek.compassnavigator;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class AddDestinationActivity extends ActionBarActivity implements
		OnClickListener, OnEditorActionListener {
	private static final String TAG = AddDestinationActivity.class.getName();

	private EditText txtName;
	private EditText txtLatitude;
	private EditText txtLongitude;
	private Button btnCancel;
	private Button btnAdd;

	private Destination current = null;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_destination);

		txtName = (EditText) findViewById(R.id.add_txtName);
		txtLatitude = (EditText) findViewById(R.id.add_txtLatitude);
		txtLongitude = (EditText) findViewById(R.id.add_txtLongitude);
		btnCancel = (Button) findViewById(R.id.add_btnCancel);
		btnAdd = (Button) findViewById(R.id.add_btnAdd);

		txtLongitude.setOnEditorActionListener(this);

		btnCancel.setOnClickListener(this);
		btnAdd.setOnClickListener(this);
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.add_btnCancel:
			onCancelAction();
			break;
		case R.id.add_btnAdd:
			onAddAction();
			break;
		default:
			throw new AssertionError();
		}
	}

	@Override
	public boolean onEditorAction(final TextView v, final int actionId,
			final KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			onAddAction();
			return true;
		} else {
			return false;
		}
	}

	private void onCancelAction() {
		finish();
	}

	private void onAddAction() {
		if (validate()) {
			final String name = txtName.getText().toString();
			final double lat = Double.parseDouble(txtLatitude.getText()
					.toString());
			final double lon = Double.parseDouble(txtLongitude.getText()
					.toString());
			final Destination tl = new Destination(name, lat, lon);
			DestinationList.getInstance().add(tl);
			showInfo(getResources().getString(R.string.add_destination_added));
			finish();
		}
	}

	private boolean validate() {
		final String name = txtName.getText().toString();
		if (name.trim().isEmpty()) {
			txtName.setError(getResources().getString(R.string.add_error_empty));
			return false;
		}

		final String strLat = txtLatitude.getText().toString();
		if (strLat.trim().isEmpty()) {
			txtLatitude.setError(getResources().getString(
					R.string.add_error_empty));
			return false;
		}
		final double lat = Double.parseDouble(strLat);
		if (lat < -90 || lat > 90) {
			txtLatitude.setError(getResources().getString(
					R.string.add_error_latitude));
			return false;
		}

		final String strLon = txtLongitude.getText().toString();
		if (strLon.trim().isEmpty()) {
			txtLongitude.setError(getResources().getString(
					R.string.add_error_empty));
			return false;
		}
		final double lon = Double.parseDouble(strLon);
		if (lon < -180 || lon > 180) {
			txtLongitude.setError(getResources().getString(
					R.string.add_error_longitude));
			return false;
		}

		return true;
	}

	private void showInfo(final String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
}
