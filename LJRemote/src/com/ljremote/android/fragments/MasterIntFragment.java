package com.ljremote.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devsmart.android.ui.HorizontalListView;
import com.ljremote.android.MainActivity;
import com.ljremote.android.R;
import com.ljremote.android.adapters.MasterIntCursorAdapter;
import com.ljremote.android.data.MasterIntManager;

public class MasterIntFragment extends AbstractDetailFragment {
	
	public MasterIntFragment() {
		super(R.string.mstr_int);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.mstr_int_fragment, null, true);
		MainActivity main = (MainActivity) getActivity();
		setDataManager(main.getDataManager().getMasterIntManager());
		
		HorizontalListView masterList = (HorizontalListView) mainView.findViewById(R.id.mstr_int_list);
		MasterIntCursorAdapter adapter= new MasterIntCursorAdapter((MasterIntManager) getDataManager());
		masterList.setAdapter(adapter);
		return mainView;
	}
}
