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

public class BGCueFragment extends AbstractDetailFragment {

	public BGCueFragment() {
		super(R.string.bgcues);
	}
	
	private final static String[][] bgcues= {
		{"1","BG Cues"},
		{"2","BG Cues"},
		{"3","BG Cues"},
		{"4","BG Cues"},
		{"5","BG Cues"},
		{"6","BG Cues"},
		{"7","BG Cues"},
		{"8","BG Cues"},
		{"9","BG Cues"},
		{"10","BG Cues"},
		{"11","BG Cues"},
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.bgcue_fragment, null, true);
		ListView listView = new ListView(getActivity());
		listView.setAdapter(new SimpleAdapter(getActivity(), ArrayHelper.convertToListMap(bgcues), R.layout.seq_item, new String[]{"text1","text2"}, new int[]{R.id.id,R.id.label}));
		((ViewGroup) mainView).addView(listView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		return mainView;
	}
}
