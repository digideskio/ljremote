package com.ljremote.android.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ljremote.android.MainActivity;
import com.ljremote.android.R;
import com.ljremote.android.data.DataManager.OnDatabaseUpdateListener;
import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.android.data.LJFunctionManager;
import com.ljremote.json.model.LJFunction;

public class TabbedLJFunctionFragment extends AbstractTabbedDetailFragment implements OnDatabaseUpdateListener {

	private Menu optionsMenu;

	public TabbedLJFunctionFragment() {
		super(R.string.ljfunctions);
		Log.d("TabbedLJFunctionFragment", "new");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(getGroupId() == 0);
	}

	protected void fillTabHost() {
		if ( !isAdded() ) {
			return;
		}
		LJFunctionManager funcManager = ((LJFunctionManager) getDataManager());
		Cursor cursor = null;
		if (funcManager != null) {
			int group_id = getGroupId();
			cursor = funcManager.getGroupFunctionCursor(group_id);
		}
		if (cursor != null && cursor.getCount() > 0) {
			FragmentTabHost tabHost = getNewTabHost();
			cursor.moveToFirst();
			do {
				LJFunction groupFunc = funcManager
						.getLJFunctionFromCursor(cursor);
				Bundle args = new Bundle();
				args.putInt("group_id", groupFunc.getId());
				if (funcManager.isGroupOfGroupFunction(groupFunc.getId())) {
					tabHost.addTab(
							tabHost.newTabSpec(
									String.valueOf(groupFunc.getId()))
									.setIndicator(groupFunc.getName()),
							TabbedLJFunctionFragment.class, args);
				} else {
					tabHost.addTab(
							tabHost.newTabSpec(
									String.valueOf(groupFunc.getId()))
									.setIndicator(groupFunc.getName()),
							LJFunctionFragment.class, args);
				}
			} while (cursor.moveToNext());

			setTabHost(tabHost);
			switchEmptyMode(false);
		} else {
			switchEmptyMode(true);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if ( getGroupId() == 0 ) {
			this.optionsMenu = menu;
			inflater.inflate(R.menu.ljfunction, menu);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if ( getGroupId() == 0 ) {
			switch (item.getItemId()) {
			case R.id.refresh:
				((LJFunctionManager) getDataManager()).refreshData();
//				setRefreshActionButtonState(true);
				break;
			default:
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPrepareFillTabHost() {
		MainActivity main = (MainActivity) getActivity();
		setDataManager(main.getDataManager().getLJFunctionManager());
		if ( getGroupId() == 0 ) {
			getDataManager().getMainDataManager().registerDatabaseUpdateListener(this);
		}
	}

	private int getGroupId(){
		int group_id = 0;
		if ( getArguments() != null ) {
			group_id = getArguments().getInt("group_id",0);
		}
		return group_id;
	}
	@Override
	public void onTableUpdateListener(Context context, TABLES table) {
		if( table == TABLES.LJFUNCTIONS ) {
//			setRefreshActionButtonState(false);
			fillTabHost();
		}
	}

	public void setRefreshActionButtonState(final boolean refreshing) {
	    if (optionsMenu != null) {
	        final MenuItem refreshItem = optionsMenu
	            .findItem(R.id.refresh);
	        if (refreshItem != null) {
	            if (refreshing) {
	                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
	            } else {
	                refreshItem.setActionView(null);
	            }
	        }
	    }
	}
}
