package de.skawronek.compassnavigator;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class EditTargetListActivity extends Activity implements
		ITargetListListener {
	private ListView targetListView;
	private ArrayAdapter<Target> targetAdapter;
	private Target selection = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_target_list);

		TargetList.getInstance().addListener(this);
		targetListView = (ListView) findViewById(R.id.targetListView);
		targetAdapter = new ArrayAdapter<Target>(getApplicationContext(),
				android.R.layout.simple_list_item_single_choice, TargetList
						.getInstance().getTargets());
		targetListView.setAdapter(targetAdapter);
		selection = TargetList.getInstance().getSelectedTarget();
		if (selection != null) {
			targetListView.setItemChecked(TargetList.getInstance().getTargets()
					.indexOf(selection), true);
		}

		targetListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				selection = TargetList.getInstance().getTargets().get(position);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_target:
			Intent intent = new Intent(this, AddTargetActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_delete_target:
			Builder builder = new Builder(this);
			builder.setMessage("Are you shure you want to delete the target?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									TargetList.getInstance().removeTarget(
											selection);
									selection = null;
									Toast.makeText(EditTargetListActivity.this,
											"Target deleted",
											Toast.LENGTH_SHORT).show();
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			final AlertDialog dialog = builder.create();
			dialog.show();
			return true;
		case R.id.menu_select_target:
			TargetList.getInstance().selectTarget(selection);
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_edit_target_list, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.getItem(1).setEnabled(selection != null);
		menu.getItem(2).setEnabled(selection != null);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void targetSelected(Target selectedTarget) {
		// TODO Auto-generated method stub

	}

	@Override
	public void targetAdded(Target newTarget) {
		targetAdapter.notifyDataSetChanged();
		if (selection != null) {
			targetListView.setItemChecked(TargetList.getInstance().getTargets()
					.indexOf(selection), true);
		}
	}

	@Override
	public void targetRemoved(Target removedTarget) {
		targetAdapter.notifyDataSetChanged();
		if (removedTarget == selection) {
			selection = null;
			targetListView.clearChoices();
		} else {
			targetListView.setItemChecked(TargetList.getInstance().getTargets()
					.indexOf(selection), true);
		}
	}
}
