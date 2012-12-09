package com.ljremote.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ljremote.android.MainActivity;
import com.ljremote.android.R;
import com.ljremote.android.adapters.StaticCursorAdapter;
import com.ljremote.android.adapters.StaticListAdapter;

public class StaticFragment extends AbstractDetailFragment {
	
	StaticListAdapter adapter;
	
	public StaticFragment() {
		super(R.string.statics);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.static_fragment, null, true);
		MainActivity main = (MainActivity) getActivity();
		
		ListView staticList = (ListView) mainView.findViewById(R.id.static_list);
//		List<Static> statics= main.getDataManager().getStaticList();
//		adapter= new StaticListAdapter(getActivity(), statics);
		StaticCursorAdapter adapter = new StaticCursorAdapter(main);
		staticList.setAdapter(adapter);
		return mainView;
	}
}
