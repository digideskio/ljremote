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

public class CueFragment extends AbstractDetailFragment {

	public CueFragment() {
		super(R.string.cues);
	}
	
	private final static String[][] cues= {
		{"1","Cues"},
		{"2","Cues"},
		{"3","Cues"},
		{"4","Cues"},
		{"5","Cues"},
		{"6","Cues"},
		{"7","Cues"},
		{"8","Cues"},
		{"9","Cues"},
		{"10","Cues"},
		{"11","Cues"},
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.cue_fragment, null, true);
		ListView listView = new ListView(getActivity());
		listView.setAdapter(new SimpleAdapter(getActivity(), ArrayHelper.convertToListMap(cues), R.layout.seq_item, new String[]{"text1","text2"}, new int[]{R.id.id,R.id.label}));
		((ViewGroup) mainView).addView(listView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		return mainView;
	}
}
