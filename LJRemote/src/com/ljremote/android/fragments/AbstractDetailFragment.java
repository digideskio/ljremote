package com.ljremote.android.fragments;

import android.annotation.SuppressLint;
import android.app.ActionBar.TabListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ljremote.android.R;
import com.ljremote.android.data.AbstractDataManager;

@SuppressLint("ValidFragment")
public class AbstractDetailFragment extends Fragment {
	
	protected int str_id;
	protected AbstractDataManager dataManager;
	
	public AbstractDataManager getDataManager() {
		return dataManager;
	}

	public void setDataManager(AbstractDataManager dataManager) {
		this.dataManager = dataManager;
	}

	public AbstractDetailFragment(int str_id) {
		this.str_id= str_id;
	}
	
	public int getStr_id() {
		return str_id;
	}
	public void setStr_id(int str_id) {
		this.str_id = str_id;
	}

	private int pos=-1;
	protected Menu optionsMenu;
	
	public int getPosition() {
		return pos;
	}
	public void setPosition(int pos) {
		this.pos = pos;
	}

	public boolean hasTab() {
		return false;
	}
	
	public String[] getTabNames() {
		return new String[]{""};
	}
	
	public TabListener getTabListener(FragmentActivity mainActivity, String tag){
		return null;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		this.optionsMenu = menu;
		inflater.inflate(R.menu.common_fragment, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			getDataManager().refreshData();
			setRefreshActionButtonState(true);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void setRefreshActionButtonState(final boolean refreshing) {
	    if (isResumed() && optionsMenu != null) {
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
