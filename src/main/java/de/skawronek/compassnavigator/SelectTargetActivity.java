package de.skawronek.compassnavigator;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SelectTargetActivity extends Activity implements
		ITargetListListener {
	private ArrayAdapter<Target> targetAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_target);

		final ListView targetListView = (ListView) findViewById(R.id.targetListView);
		targetAdapter = new ArrayAdapter<Target>(getApplicationContext(),
				android.R.layout.simple_list_item_1, TargetList.getInstance()
						.getTargets());
		targetListView.setAdapter(targetAdapter);
		TargetList.getInstance().addListener(this);
		targetListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final Target target = targetAdapter.getItem(position);
				TargetList.getInstance().selectTarget(target);
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		targetAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_select_target, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_target: {
			Intent intent = new Intent(this, AddTargetActivity.class);
			startActivity(intent);
			return true;
		}
		case R.id.menu_edit_target_list: {
			Intent intent = new Intent(this, EditTargetListActivity.class);
			startActivity(intent);
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void targetSelected(Target selectedTarget) {
		// TODO Auto-generated method stub

	}

	@Override
	public void targetAdded(Target newTarget) {
		targetAdapter.notifyDataSetChanged();
	}

	@Override
	public void targetRemoved(Target removedTarget) {
		targetAdapter.notifyDataSetChanged();
	}
}
