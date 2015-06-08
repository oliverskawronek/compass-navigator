package de.skawronek.compassnavigator;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class DeleteDestinationsActivity extends ActionBarActivity implements
		OnClickListener {
	private static final String TAG = DeleteDestinationsActivity.class
			.getName();

	private ListView lstDestinations;
	private Button btnCancel;
	private Button btnDelete;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delete_destinations);

		lstDestinations = (ListView) findViewById(R.id.delete_lstDestinations);
		btnCancel = (Button) findViewById(R.id.delete_btnCancel);
		btnDelete = (Button) findViewById(R.id.delete_btnDelete);

		btnCancel.setOnClickListener(this);
		btnDelete.setOnClickListener(this);

		final List<Destination> items = DestinationList.getInstance()
				.getList();
		final MultiSelectDestinationAdapter adapter = new MultiSelectDestinationAdapter(
				this, items);
		lstDestinations.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		lstDestinations.setAdapter(adapter);
		adapter.setCheckListener(new MultiSelectDestinationAdapter.CheckListener() {
			@Override
			public void onSelectionChanged(int position, boolean selected) {
				invalidateBtnDelete();
			}
		});

		invalidateBtnDelete();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.delete_destinations_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_select_all:
			onSelectAll();
			return true;
		case R.id.action_unselect_all:
			onUnselectAll();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void onSelectAll() {
		final int numItems = lstDestinations.getCount();
		for (int pos = 0; pos < numItems; pos++) {
			lstDestinations.setItemChecked(pos, true);
		}
		invalidateBtnDelete();
	}

	private void onUnselectAll() {
		final int numItems = lstDestinations.getCount();
		for (int pos = 0; pos < numItems; pos++) {
			lstDestinations.setItemChecked(pos, false);
		}
		invalidateBtnDelete();
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.delete_btnDelete:
			onDeleteAction();
			break;
		case R.id.delete_btnCancel:
			onCancelAction();
			break;
		default:
			throw new AssertionError();
		}
	}

	private void onDeleteAction() {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.delete_confirm_title)
				.setMessage(R.string.delete_confirm_message)
				.setPositiveButton(R.string.yes, new Dialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final int numDeletions = deleteSelectedDestinations();
						final String info = String.format(getResources()
								.getString(R.string.delete_delete_info),
								numDeletions);
						showInfo(info);
						finish();
					}
				}).setNegativeButton(R.string.cancel, null).show();
	}

	private int deleteSelectedDestinations() {
		final List<Destination> toDelete = new ArrayList<Destination>();
		final int numItems = lstDestinations.getCount();
		int deleteCount = 0;
		for (int pos = 0; pos < numItems; pos++) {
			if (lstDestinations.isItemChecked(pos)) {
				toDelete.add((Destination) lstDestinations
						.getItemAtPosition(pos));
				deleteCount++;
			}
		}

		final DestinationList list = DestinationList.getInstance();
		for (final Destination tl : toDelete) {
			list.remove(tl);
		}

		return deleteCount;
	}

	private void onCancelAction() {
		finish();
	}

	private void showInfo(final String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	private void invalidateBtnDelete() {
		final int numCheckedItems = countCheckedItems(lstDestinations);
		if (numCheckedItems > 0) {
			btnDelete.setEnabled(true);
		} else {
			btnDelete.setEnabled(false);
		}
	}

	private int countCheckedItems(final ListView lv) {
		final int numItems = lstDestinations.getCount();
		int count = 0;
		for (int pos = 0; pos < numItems; pos++) {
			if (lv.isItemChecked(pos)) {
				count++;
			}
		}
		return count;
	}
}
