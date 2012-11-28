package com.ljremote.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.RelativeLayout.LayoutParams;

import com.ljremote.android.R;
import com.ljremote.android.util.ArrayHelper;

public class CueListFragment extends AbstractDetailFragment {

	public CueListFragment() {
		super(R.string.cuelists);
	}
	
	private final static String[][] cueLists= {
		{"1","Cue list"},
		{"2","Cue list"},
		{"3","Cue list"},
		{"4","Cue list"},
		{"5","Cue list"},
		{"6","Cue list"},
		{"7","Cue list"},
		{"8","Cue list"},
		{"9","Cue list"},
		{"10","Cue list"},
		{"11","Cue list"},
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.cuelist_fragment, null, true);
		ListView listView = new ListView(getActivity());
		listView.setAdapter(new SimpleAdapter(getActivity(), ArrayHelper.convertToListMap(cueLists), R.layout.seq_item, new String[]{"text1","text2"}, new int[]{R.id.id,R.id.label}));
		((ViewGroup) mainView).addView(listView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		return mainView;
	}
}
