package com.ljremote.android.ui;

import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.ljremote.android.MainActivity;
import com.ljremote.android.R;
import com.ljremote.android.fragments.AbstractDetailFragment;

public class MyTabListener<T> implements TabListener {

	private AbstractDetailFragment fragment;
	private final FragmentActivity activity;
	private final String tag;
	private final Class<T> clazz;
	
	public MyTabListener(FragmentActivity activity, String tag, Class<T> clazz) {
		this(null, activity, tag, clazz);
	}

	public MyTabListener(AbstractDetailFragment fragment, FragmentActivity activity, String tag, Class<T> clazz) {
		super();
		this.fragment = fragment;
		this.activity = activity;
		this.tag = tag;
		this.clazz = clazz;
		Log.d("MyTabListener", "new");
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		Log.d("MyTabListener", "onTabSelected tag : " + tag + " tabtag : " + tab.getTag() + " current fragment : " + fragment);
		android.support.v4.app.FragmentTransaction ftt = activity.getSupportFragmentManager().beginTransaction();
		if( fragment == null || tag != tab.getTag() ) {
			AbstractDetailFragment newFragment = (AbstractDetailFragment) Fragment.instantiate(activity, clazz.getName());
			ftt.replace(R.id.detail_container, newFragment, tag).commit();
			((MainActivity) activity).updateFragment(fragment,  newFragment);
			fragment = newFragment;
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
//		android.support.v4.app.FragmentTransaction ftt = activity.getSupportFragmentManager().beginTransaction();
//		if ( fragment != null ) {
//			ftt.detach(fragment).commit();
//		}
	}

}
