package com.ljremote.android.fragments;

import java.lang.reflect.Field;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ljremote.android.R;

public class TabbedCueFragment extends AbstractDetailFragment {
	private FragmentTabHost mTabHost;

	public TabbedCueFragment() {
		super(R.string.cues);
		Log.d("TabbedCueFragment", "new");
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("TabbedCueFragment", "onCreateView tag=" +getTag() + ", " + getArguments());
		mTabHost = new FragmentTabHost(getActivity());
		mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.frameLayout1);
		
		mTabHost.addTab(mTabHost.newTabSpec("current").setIndicator("Current Cue"), CueFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("list").setIndicator("Cue list"), CueFragment.class, null);
		return mTabHost;
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mTabHost =null;
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
//	private void updateCurrentCueTittle() {
//		if(title == null){
//			return;
//		}
//		CueSyncTask<Void, Void, String> task = ((CueManager) getDataManager()).new CueSyncTask<Void, Void, String>() {
//
//			@Override
//			protected void onPostExecute(String result) {
//				if ( result == null ){
//					result = "NO Current Cue";
//				}
//				title.setText(result);
//			}
//
//			@Override
//			protected String doInBackground(Void... params) {
//				if ( !getDataManager().getMainDataManager().checkService() ){
//					return null;
//				}
//				try {
//					int id = getClientProxy().getCurrentCueId();
//					Log.d("CueFragment","current id " + id  );
//					Cue cue = ((CueManager) getDataManager()).getCue(id);
//					if( cue != null) {
//						return cue.getLabel();
//					}
//				} catch (Exception e) {
//					Log.e("CueFragment","error while getting current cue id ", e  );
//				}
//				return null;
//			}
//		};
//		getDataManager().getMainDataManager().getService().submit(task);
//	}
//
//
//	@Override
//	public void onClick(View v) {
//		Log.d("cue.update_all", "onClick : " + v.getId() + " ( " + R.id.up_all + " ) ");
//		switch (v.getId()) {
//		case R.id.up_all:
//			((CueManager) getDataManager()).updateAllDB();
//			break;
//		case R.id.clear_all:
//			((CueManager) getDataManager()).clearAll();
//		default:
//			break;
//		}
//	}
//
//
//	@Override
//	public void onTableUpdateListener(Context context, TABLES table) {
//		if( table == TABLES.CUES ){
//			updateCurrentCueTittle();
//		}
//	}
//
//	@Override
//	public boolean hasTab() {
//		return true;
//	}
//
//	@Override
//	public String[] getTabNames() {
//		return new String[]{CURRENT_CUE,CUE_LIST};
//	}
//
//	@Override
//	public TabListener getTabListener(FragmentActivity activity,String tag) {
//		return tabListener == null || tag != getTag() ? tabListener = new MyTabListener<TabbedCueFragment>(this,activity, tag, TabbedCueFragment.class) : tabListener;
//	}
}
