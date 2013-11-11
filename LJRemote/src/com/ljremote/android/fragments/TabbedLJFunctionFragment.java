package com.ljremote.android.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;

import com.ljremote.android.MainActivity;
import com.ljremote.android.R;
import com.ljremote.android.data.DataManager.OnDatabaseUpdateListener;
import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.android.data.LJFunctionManager;
import com.ljremote.json.model.LJFunction;

public class TabbedLJFunctionFragment extends AbstractTabbedDetailFragment implements OnDatabaseUpdateListener {

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
			SlidingTabHelper tabHelper = newSlidingTabHelper();
			cursor.moveToFirst();
			do {
				LJFunction groupFunc = funcManager
						.getLJFunctionFromCursor(cursor);
				Bundle args = new Bundle();
				args.putInt("group_id", groupFunc.getId());
				if (funcManager.isGroupOfGroupFunction(groupFunc.getId())) {
					
					tabHelper.addTab(
							tabHelper.newTabSpec(
									String.valueOf(groupFunc.getId()))
									.setIndicator(groupFunc.getName()),
							TabbedLJFunctionFragment.class, args,groupFunc.getName());
				} else {
					tabHelper.addTab(
							tabHelper.newTabSpec(
									String.valueOf(groupFunc.getId()))
									.setIndicator(groupFunc.getName()),
							LJFunctionFragment.class, args,groupFunc.getName());
				}
			} while (cursor.moveToNext());

			setTabHelper(tabHelper);
			switchEmptyMode(false);
		} else {
			switchEmptyMode(true);
		}
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
			setRefreshActionButtonState(false);
			fillTabHost();
		}
	}

}
