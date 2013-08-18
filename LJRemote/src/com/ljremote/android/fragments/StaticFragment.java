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
import com.ljremote.android.adapters.StaticCursorAdapter;
import com.ljremote.android.adapters.StaticListAdapter;
import com.ljremote.android.data.StaticManager;

public class StaticFragment extends AbstractDetailFragment implements OnClickListener {
	
	StaticListAdapter adapter;
	
	public StaticFragment() {
		super(R.string.statics);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.static_fragment, null, true);
		MainActivity main = (MainActivity) getActivity();
		setDataManager(main.getDataManager().getStaticManager());
		
		ListView staticList = (ListView) mainView.findViewById(R.id.static_list);
		StaticCursorAdapter adapter = new StaticCursorAdapter((StaticManager) getDataManager());
		staticList.setAdapter(adapter);
		
		((Button) mainView.findViewById(R.id.up_all)).setOnClickListener(this);
		((Button) mainView.findViewById(R.id.clear_all)).setOnClickListener(this);
		return mainView;
	}

	@Override
	public void onClick(View v) {
		Log.d("cvfdvfv", "onClick : " + v.getId() + " ( " + R.id.up_all + " ) ");
		switch (v.getId()) {
		case R.id.up_all:
			((StaticManager) getDataManager()).updateAllDB();
			break;
		case R.id.clear_all:
			((StaticManager) getDataManager()).clearAll();
			break;
		default:
			break;
		}
	}

}
