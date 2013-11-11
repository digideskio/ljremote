package com.ljremote.android.fragments;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.ljremote.android.R;

public abstract class AbstractTabbedDetailFragment extends
		AbstractDetailFragment implements OnTabChangeListener,
		OnPageChangeListener {

	protected TabHost mTabHost;
	protected TextView emptyTextView;
	protected ViewGroup tabHostContainer;
	protected ViewPager mViewPager;
	protected boolean emptyMode;
	protected HorizontalScrollView mHorizontalScrollView;
	protected SlidingTabHelper mHelper;

	public AbstractTabbedDetailFragment(int str_id) {
		super(str_id);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.tabbed_fragment, null, true);
		emptyTextView = (TextView) mainView.findViewById(R.id.emptyTextView);
		tabHostContainer = (ViewGroup) mainView
				.findViewById(R.id.trueTabHostContainer);
		mHorizontalScrollView = (HorizontalScrollView) mainView
				.findViewById(R.id.horizontalScrollView1);
		mTabHost = (TabHost) mainView.findViewById(android.R.id.tabhost);
		mViewPager = (ViewPager) mainView.findViewById(R.id.viewpager);

		mTabHost.setup();

		switchEmptyMode(true);

		onPrepareFillTabHost();

		fillTabHost();

		return mainView;
	}

	protected abstract void onPrepareFillTabHost();

	protected abstract void fillTabHost();

	protected SlidingTabHelper newSlidingTabHelper() {
		SlidingTabHelper tabHelper = new SlidingTabHelper(getActivity(),
				getChildFragmentManager());
		return tabHelper;
	}

	protected void setTabHelper(SlidingTabHelper helper) {
		mHelper = helper;
		mViewPager.setAdapter(mHelper);
		mHelper.attachListener(this);
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
			Field childFragmentManager = Fragment.class
					.getDeclaredField("mChildFragmentManager");
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
		mTabHost = null;
		mViewPager = null;
	}

	@Override
	public void onTabChanged(String tabId) {
		int pos = mTabHost.getCurrentTab();
		mViewPager.setCurrentItem(pos);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		mTabHost.setCurrentTab(position);
		View tabView = mTabHost.getCurrentTabView();
		if (tabView != null) {
			final int width = mHorizontalScrollView.getWidth();
			final int scrollPos = tabView.getLeft()
					- (width - tabView.getWidth()) / 2;
			mHorizontalScrollView.scrollTo(scrollPos, 0);
		} else {
			mHorizontalScrollView.scrollBy(positionOffsetPixels,0);
		}
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub

	}

	public class SlidingTabHelper extends FragmentStatePagerAdapter {
		protected List<Fragment> mFragments;
		protected List<TabInfo> tabInfos;
		protected Context mActivity;
		protected FragmentManager mFm;

		class TabInfo {
			TabSpec _tabSpec;
			String _title;
			String _clazz;
			Bundle _args;
			Fragment _fragment;
			
			public TabInfo(TabSpec tabSpec, String clazz, Bundle args, String title) {
				super();
				this._tabSpec = tabSpec;
				this._clazz = clazz;
				this._args = args;
				this._title = title;
			}
			
			
		}
		
		public SlidingTabHelper(Context context, FragmentManager fragmentManager) {
			this(context, fragmentManager, null, null);
		}

		public SlidingTabHelper(Context context,
				FragmentManager fragmentManager, List<Fragment> fragments,
				List<TabSpec> specs) {
			super(fragmentManager);
			this.mActivity = context;
			this.mFm = fragmentManager;
			mFragments = fragments == null ? new ArrayList<Fragment>()
					: fragments;
			tabInfos = new ArrayList<AbstractTabbedDetailFragment.SlidingTabHelper.TabInfo>();
		}

		public void addTab(TabSpec tabSpec, Class<?> clazz, Bundle args) {
			addTab(tabSpec, clazz, args, null);
		}
		
		public void addTab(TabSpec tabSpec, Class<?> clazz, Bundle args, String title) {
			tabSpec.setContent(new DummyTabFactory(mActivity));
			mTabHost.addTab(tabSpec);
			tabInfos.add(new TabInfo(tabSpec, clazz.getName(), args,title));
		}

		@Override
		public Fragment getItem(int pos) {
			if ( pos < mFragments.size() && mFragments.get(pos) != null ) {
				return mFragments.get(pos);
			}
			TabInfo tabInfo = tabInfos.get(pos);
			return Fragment.instantiate(mActivity, tabInfo._clazz, tabInfo._args);
		}

		@Override
		public int getCount() {
			return tabInfos.size();
		}

		public void attachListener(AbstractTabbedDetailFragment fragment) {
			notifyDataSetChanged();
			mTabHost.setOnTabChangedListener(fragment);
			mViewPager.setOnPageChangeListener(fragment);
		}

		public TabSpec newTabSpec(String tag) {
			return mTabHost.newTabSpec(tag);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			String title = tabInfos.get(position)._title;
			return title == null ? "TAB " + position : title;
		}
		
		

	}

	static class DummyTabFactory implements TabHost.TabContentFactory {
		private final Context mContext;

		public DummyTabFactory(Context context) {
			mContext = context;
		}

		@Override
		public View createTabContent(String tag) {
			View v = new View(mContext);
			v.setTag(tag);
			v.setMinimumWidth(0);
			v.setMinimumHeight(0);
			return v;
		}
	}
}
