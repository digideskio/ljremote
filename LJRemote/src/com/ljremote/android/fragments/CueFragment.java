package com.ljremote.android.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.ljremote.android.MainActivity;
import com.ljremote.android.R;
import com.ljremote.android.adapters.CueCursorAdapter;
import com.ljremote.android.data.CueManager;

public class CueFragment extends AbstractDetailFragment implements OnClickListener {

	public CueFragment() {
		super(R.string.cues);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.cue_fragment, null, true);
		MainActivity main = (MainActivity) getActivity();
		setDataManager(main.getDataManager().getCueManager());
		
		ListView staticList = (ListView) mainView.findViewById(R.id.cue_list);
		CueCursorAdapter adapter = new CueCursorAdapter((CueManager) getDataManager());
		staticList.setAdapter(adapter);
		
		((Button) mainView.findViewById(R.id.up_all)).setOnClickListener(this);
		((Button) mainView.findViewById(R.id.clear_all)).setOnClickListener(this);
		return mainView;
	}

	@Override
	public void onClick(View v) {
		Log.d("cue.update_all", "onClick : " + v.getId() + " ( " + R.id.up_all + " ) ");
		switch (v.getId()) {
		case R.id.up_all:
			((CueManager) getDataManager()).updateAllDB();
			break;
		case R.id.clear_all:
			((CueManager) getDataManager()).clearAll();
		default:
			break;
		}
	}

//	@Override
//	public boolean hasTab() {
//		return true;
//	}
//
//	@Override
//	public String[] getTabNames() {
//		return new String[]{"Current Cue","Cue list"};
//	}
//
//	@Override
//	public TabListener getTabListener(String tag) {
//		return new MyTabListener<CueFragment>((Fragment)this,getActivity(), tag, CueFragment.class);
//	}
}
