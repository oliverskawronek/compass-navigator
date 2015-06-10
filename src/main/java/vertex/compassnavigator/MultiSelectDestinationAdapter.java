package vertex.compassnavigator;

import java.util.List;

import vertex.compassnavigator.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

class MultiSelectDestinationAdapter extends ArrayAdapter<Destination> {
	private final String formatGeoString;

	private CheckListener checkListener = null;

	public interface CheckListener {
		public void onSelectionChanged(int position, boolean selected);
	}

	public MultiSelectDestinationAdapter(final Context context,
			final List<Destination> items) {
		super(context, 0, items);

		formatGeoString = context.getResources().getString(R.string.item_geo);
	}

	public void setCheckListener(final CheckListener checkListener) {
		this.checkListener = checkListener;
	}

	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {
		// Reuse if exists
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(
					R.layout.item_select_destination, parent, false);
		}
		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				// Toggle selection
				final boolean selected = ((ListView) parent)
						.isItemChecked(position);
				((ListView) parent).setItemChecked(position, !selected);
				notifySelectionChanged(position, selected);
			}
		});

		final CheckBox chkSelected = (CheckBox) convertView
				.findViewById(R.id.item_select_chkSelected);
		final TextView lblName = (TextView) convertView
				.findViewById(R.id.item_select_lblName);
		final TextView lblGeo = (TextView) convertView
				.findViewById(R.id.item_select_lblGeo);

		final boolean selected = ((ListView) parent).isItemChecked(position);

		final Destination tl = getItem(position);

		chkSelected.setChecked(selected);
		lblName.setText(tl.getName());
		final double lat = tl.getLocation().getLatitude();
		final double lon = tl.getLocation().getLongitude();
		lblGeo.setText(String.format(formatGeoString, Util.formatGeoCoord(lat),
				Util.formatGeoCoord(lon)));

		return convertView;
	}

	private void notifySelectionChanged(final int position,
			final boolean selected) {
		if (checkListener != null) {
			checkListener.onSelectionChanged(position, !selected);
		}
	}
}