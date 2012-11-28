package com.ljremote.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SimpleAdapter;

import com.ljremote.android.R;
import com.ljremote.android.util.ArrayHelper;

public class SequenceFragment extends AbstractDetailFragment {

	public SequenceFragment() {
		super(R.string.sequences);
	}
	
	private final static String[][] sequences= {
		{"1","Sequences"},
		{"2","Sequences"},
		{"3","Sequences"},
		{"4","Sequences"},
		{"5","Sequences"},
		{"6","Sequences"},
		{"7","Sequences"},
		{"8","Sequences"},
		{"9","Sequences"},
		{"10","Sequences"},
		{"11","Sequences"},
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.sequence_fragment, null, true);
		
		ListView listView = new ListView(getActivity());
		listView.setAdapter(new SimpleAdapter(getActivity(), ArrayHelper.convertToListMap(sequences), R.layout.seq_item, new String[]{"text1","text2"}, new int[]{R.id.id,R.id.label}));
		((ViewGroup) mainView).addView(listView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listView.setSelector(R.drawable.static_list_selector);
		return mainView;
	}
}
