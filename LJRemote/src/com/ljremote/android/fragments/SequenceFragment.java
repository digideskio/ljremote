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
import com.ljremote.android.adapters.SeqCursorAdapter;
import com.ljremote.android.data.SequenceManager;

public class SequenceFragment extends AbstractDetailFragment implements OnClickListener {

	public SequenceFragment() {
		super(R.string.sequences);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.e("SequenceFragment", "onCreateView " + savedInstanceState + ", container -> " + container);
		View mainView = inflater.inflate(R.layout.sequence_fragment, null, true);
		MainActivity main = (MainActivity) getActivity();
		setDataManager(main.getDataManager().getSequenceManager());
		
		ListView listView = (ListView) mainView.findViewById(R.id.seq_list);
		SeqCursorAdapter adapter = new SeqCursorAdapter((SequenceManager) getDataManager());
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		((Button) mainView.findViewById(R.id.up_all)).setOnClickListener(this);
		((Button) mainView.findViewById(R.id.clear_all)).setOnClickListener(this);
		return mainView;
	}

	@Override
	public void onClick(View v) {
		Log.d("seq.update_all", "onClick : " + v.getId() + " ( " + R.id.up_all + " ) ");
		switch (v.getId()) {
		case R.id.up_all:
			((SequenceManager) getDataManager()).updateAllDB();
			break;
		case R.id.clear_all:
			((SequenceManager) getDataManager()).clearAll();
		default:
			break;
		}
	}
}
