package com.ljremote.android.ui;

import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;

import com.ljremote.android.R;

public class MyTabListener<T> implements TabListener {

	private Fragment fragment;
	private final FragmentActivity activity;
	private final String tag;
	private final Class<T> clazz;
	
	public MyTabListener(FragmentActivity activity, String tag, Class<T> clazz) {
		super();
		this.activity = activity;
		this.tag = tag;
		this.clazz = clazz;
	}

	public MyTabListener(Fragment fragment, FragmentActivity activity, String tag, Class<T> clazz) {
		super();
		this.fragment = fragment;
		this.activity = activity;
		this.tag = tag;
		this.clazz = clazz;
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if( fragment == null ) {
			fragment = Fragment.instantiate(activity, clazz.getName());
			ft.add(R.id.detail_container, fragment, tag);
		} else {
			ft.attach(fragment);
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if ( fragment != null ) {
			ft.detach(fragment);
		}
		
	}

}
