package com.ljremote.android.fragments;

import java.lang.reflect.Field;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TabWidget;
import android.widget.TextView;

import com.ljremote.android.R;

public abstract class AbstractTabbedDetailFragment extends AbstractDetailFragment {

	protected FragmentTabHost mTabHost;
	protected TextView emptyTextView;
	protected ViewGroup tabHostContainer;
	protected boolean emptyMode;
	
	public AbstractTabbedDetailFragment(int str_id) {
		super(str_id);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.tabbed_fragment, null, true);
		emptyTextView = (TextView) mainView.findViewById(R.id.emptyTextView);
		tabHostContainer = (ViewGroup) mainView.findViewById(R.id.trueTabHostContainer);
		switchEmptyMode(true);
		onPrepareFillTabHost();
		fillTabHost();
		return mainView;
	}
	
	protected abstract void onPrepareFillTabHost();
	protected abstract void fillTabHost();
	
	protected FragmentTabHost getNewTabHost() {
		FragmentTabHost tabHost = new FragmentTabHost(getActivity());
		tabHost.setup(getActivity(), getChildFragmentManager(),
				R.id.frameLayout1);
		TabWidget tw = (TabWidget) tabHost.findViewById(android.R.id.tabs);
		LinearLayout ll = (LinearLayout) tw.getParent();
		HorizontalScrollView hs = new HorizontalScrollView(getActivity());
		hs.setLayoutParams(new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT));
		ll.addView(hs, 0);
		ll.removeView(tw);
		hs.addView(tw);
		hs.setHorizontalScrollBarEnabled(false);
		return tabHost;
	}
	
	protected void setTabHost(FragmentTabHost tabHost){
		tabHostContainer.removeAllViews();
		tabHostContainer.addView(tabHost);
	}

	protected void switchEmptyMode(boolean empty) {
		if (empty) {
			emptyTextView.setVisibility(View.VISIBLE);
			tabHostContainer.setVisibility(View.INVISIBLE);
		} else {
			emptyTextView.setVisibility(View.INVISIBLE);
			tabHostContainer.setVisibility(View.VISIBLE);
		}
		emptyMode = empty;
	}
	
	@Override
	public void onDetach() {
	    super.onDetach();

	    try {
	        Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
	        childFragmentManager.setAccessible(true);
	        childFragmentManager.set(this, null);

	    } catch (NoSuchFieldException e) {
	        throw new RuntimeException(e);
	    } catch (IllegalAccessException e) {
	        throw new RuntimeException(e);
	    }
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mTabHost =null;
	}
}
