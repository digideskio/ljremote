package com.ljremote.android.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ljremote.android.MainActivity;
import com.ljremote.android.R;
import com.ljremote.android.adapters.SimpleLJFunctionCursorAdapter;
import com.ljremote.android.data.LJFunctionManager;

public class LJFunctionFragment extends AbstractDetailFragment implements OnClickListener {
	
	public LJFunctionFragment() {
		super(R.string.ljfunctions);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("LJFunctionFragment", "onCreateView " + savedInstanceState);
		View mainView = inflater.inflate(R.layout.ljfunction_fragment, null, true);
		MainActivity main = (MainActivity) getActivity();
		setDataManager(main.getDataManager().getLJFunctionManager());
		
		if ( getArguments() != null ) {
			int groupe_id = getArguments().getInt("group_id");
			ListView list = (ListView) mainView.findViewById(R.id.func_list);
			SimpleLJFunctionCursorAdapter adapter = new SimpleLJFunctionCursorAdapter((LJFunctionManager) getDataManager(), groupe_id);
			list.setAdapter(adapter);
		}
		return mainView;
	}

	@Override
	public void onClick(View v) {
		Log.d("cvfdvfv", "onClick : " + v.getId() + " ( " + R.id.up_all + " ) ");
		switch (v.getId()) {
		case R.id.up_all:
			((LJFunctionManager) getDataManager()).updateAllDB();
			break;
		case R.id.clear_all:
			((LJFunctionManager) getDataManager()).clearAll();
			break;
		default:
			break;
		}
	}

}
