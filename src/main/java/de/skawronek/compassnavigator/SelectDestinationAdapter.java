package de.skawronek.compassnavigator;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class SelectDestinationAdapter extends
		ArrayAdapter<Destination> {
	private final String formatGeoString;

	public SelectDestinationAdapter(final Context context,
			final List<Destination> items) {
		super(context, 0, items);

		formatGeoString = context.getResources().getString(
				R.string.item_geo);
	}

	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {
		// Reuse if exists
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(
					R.layout.item_show_destination, parent, false);
		}
		final TextView lblName = (TextView) convertView
				.findViewById(R.id.item_select_lblName);
		final TextView lblGeo = (TextView) convertView
				.findViewById(R.id.item_select_lblGeo);

		final Destination tl = getItem(position);
		lblName.setText(tl.getName());
		final double lat = tl.getLocation().getLatitude();
		final double lon = tl.getLocation().getLongitude();

		lblGeo.setText(String.format(formatGeoString,
				Util.formatGeoCoord(lat), Util.formatGeoCoord(lon)));
		return convertView;
	}
}