package com.ljremote.android.fragments;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devsmart.android.ui.HorizontalListView;
import com.ljremote.android.R;
import com.ljremote.android.adapters.MasterIntListAdapter;
import com.ljremote.android.types.MasterInt;

public class MasterIntFragment extends AbstractDetailFragment {
	
	public MasterIntFragment() {
		super(R.string.mstr_int);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.mstr_int_fragment, null, true);
		HorizontalListView masterList = (HorizontalListView) mainView.findViewById(R.id.mstr_int_list);
		List<MasterInt> masters= MasterInt.buildMasters(8);
		MasterIntListAdapter adapter= new MasterIntListAdapter(getActivity(), masters);
		masterList.setAdapter(adapter);
		return mainView;
	}
}
