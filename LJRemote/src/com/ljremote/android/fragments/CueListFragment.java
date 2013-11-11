package com.ljremote.android.fragments;

import android.content.Context;
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
import com.ljremote.android.adapters.CueListCursorAdapter;
import com.ljremote.android.data.CueListManager;
import com.ljremote.android.data.DataManager.OnDatabaseUpdateListener;
import com.ljremote.android.data.DataManager.TABLES;

public class CueListFragment extends AbstractDetailFragment implements OnDatabaseUpdateListener {

	public CueListFragment() {
		super(R.string.cuelists);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.cuelist_list_fragment, null, true);
		MainActivity main = (MainActivity) getActivity();
		setDataManager(main.getDataManager().getCueListManager());
		
		getDataManager().getMainDataManager().registerDatabaseUpdateListener(this);
		
		ListView listView = (ListView) mainView.findViewById(R.id.cuelist_list);
		CueListCursorAdapter adapter = new CueListCursorAdapter((CueListManager) getDataManager());
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(adapter);

		return mainView;
	}

	@Override
	public void onTableUpdateListener(Context context, TABLES table) {
		if(table.equals(TABLES.CUELISTS)) {
			setRefreshActionButtonState(false);
		}
	}
	
}
