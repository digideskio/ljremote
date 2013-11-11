package com.ljremote.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ljremote.android.MainActivity;
import com.ljremote.android.R;
import com.ljremote.android.adapters.SeqCursorAdapter;
import com.ljremote.android.data.DataManager.OnDatabaseUpdateListener;
import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.android.data.SequenceManager;

public class SequenceFragment extends AbstractDetailFragment implements OnDatabaseUpdateListener {

	public SequenceFragment() {
		super(R.string.sequences);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.e("SequenceFragment", "onCreateView " + savedInstanceState + ", container -> " + container);
		View mainView = inflater.inflate(R.layout.sequence_fragment, null, true);
		
		MainActivity main = (MainActivity) getActivity();
		setDataManager(main.getDataManager().getSequenceManager());
		
		getDataManager().getMainDataManager().registerDatabaseUpdateListener(this);

		ListView listView = (ListView) mainView.findViewById(R.id.seq_list);
		SeqCursorAdapter adapter = new SeqCursorAdapter((SequenceManager) getDataManager());
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		return mainView;
	}

	@Override
	public void onTableUpdateListener(Context context, TABLES table) {
		if( table == TABLES.SEQUENCES ) {
			setRefreshActionButtonState(false);
		}
	}

}
