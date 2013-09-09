package com.ljremote.android.fragments;

import com.ljremote.android.MainActivity;
import com.ljremote.android.data.AbstractDataManager;

import android.annotation.SuppressLint;
import android.app.ActionBar.TabListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

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
}
