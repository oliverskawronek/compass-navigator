package vertex.compassnavigator;

import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public final class StatusDialog extends Dialog {
	private final String provider;
	private final Location location;
	private final long ageInMillis;

	private TextView lblProvider;
	private TextView lblGeoPos;
	private TextView lblAge;
	private Button btnOk;

	public StatusDialog(Context context, String provider, Location location,
			long ageInMillis) {
		super(context);
		this.provider = provider;
		this.location = location;
		this.ageInMillis = ageInMillis;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setCancelable(true);
		setCanceledOnTouchOutside(true);
		setContentView(R.layout.dialog_status);
		setTitle(R.string.action_status);

		lblProvider = (TextView) findViewById(R.id.status_lblProvider);
		lblGeoPos = (TextView) findViewById(R.id.status_lblGeoPosition);
		lblAge = (TextView) findViewById(R.id.status_lblAge);
		btnOk = (Button) findViewById(R.id.status_btnOk);

		setProvider(provider);
		setLocation(location);
		setAge(ageInMillis);

		btnOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				StatusDialog.this.cancel();
			}
		});
	}

	private void setProvider(final String provider) {
		if (provider != null) {
			lblProvider.setText(provider);
		} else {
			lblProvider.setText(R.string.not_available);
		}
	}

	private void setLocation(final Location location) {
		if (location != null) {
			final double lat = location.getLatitude();
			final double lon = location.getLongitude();
			final String geoPosTemplate = getContext().getResources()
					.getString(R.string.status_geo);
			final String geoPos = String.format(geoPosTemplate,
					Util.formatGeoCoord(lat), Util.formatGeoCoord(lon));
			lblGeoPos.setText(geoPos);
		} else {
			lblGeoPos.setText(R.string.not_available);
		}
	}

	private void setAge(final long ageInMillis) {
		if (ageInMillis >= 0) {
			final String ageTemplate = getContext().getResources().getString(
					R.string.status_age);
			final long fakeNow = Long.MAX_VALUE;
			final String age = String.format(
					ageTemplate,
					DateUtils.getRelativeTimeSpanString(fakeNow - ageInMillis,
							fakeNow, 0).toString());
			lblAge.setText(age);
		} else {
			lblAge.setText(R.string.status_age_not_available);
		}
	}
}
