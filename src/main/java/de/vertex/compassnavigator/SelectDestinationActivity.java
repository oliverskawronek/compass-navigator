package vertex.compassnavigator;

import java.util.List;

import vertex.compassnavigator.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SelectDestinationActivity extends ActionBarActivity implements
		OnItemClickListener {
	private ListView lstDestinations;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_destination);

		lstDestinations = (ListView) findViewById(R.id.select_lstDestinations);
		final List<Destination> items = DestinationList.getInstance().getList();
		final SelectDestinationAdapter adapter = new SelectDestinationAdapter(
				this, items);
		lstDestinations.setAdapter(adapter);
		lstDestinations.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view,
			final int position, final long id) {
		final Destination tl = (Destination) parent.getAdapter().getItem(
				position);
		onDestinationSelected(tl);
	}

	private void onDestinationSelected(final Destination tl) {
		final Intent intent = new Intent();
		intent.putExtra(NavigatorActivity.EXTRA_SELECTED_DESTINATION, tl);
		setResult(RESULT_OK, intent);
		finish();
	}
}
