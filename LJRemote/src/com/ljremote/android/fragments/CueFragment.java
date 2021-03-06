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
import android.widget.TextView;

import com.ljremote.android.MainActivity;
import com.ljremote.android.R;
import com.ljremote.android.adapters.CueCursorAdapter;
import com.ljremote.android.data.CueManager;
import com.ljremote.android.data.CueManager.CueSyncTask;
import com.ljremote.android.data.DataManager.OnDatabaseUpdateListener;
import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.json.model.Cue;

public class CueFragment extends AbstractDetailFragment implements OnDatabaseUpdateListener {

	private static final String CUE_LIST = "Cue list";
	private static final String CURRENT_CUE = "Current Cue";
	private ListView listView;
	private MODE currentMode;
	private TextView title;

	public CueFragment() {
		super(R.string.cues);
		currentMode = MODE.LIST;
		Log.d("CueFragment", "new");
	}
	
	public enum MODE {
		LIST, CURRENT;
		
		public static MODE tagToMode(String tag){
			if (tag == null) {
				return LIST;
			}
			if( tag.equals(CURRENT_CUE) || tag.equals("current") ) {
				return CURRENT;
			}
			return LIST;
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("CueFragment", "onCreateView tag=" +getTag() + ", " + getArguments());
		currentMode = MODE.tagToMode(getTag());
		
		MainActivity main = (MainActivity) getActivity();
		setDataManager(main.getDataManager().getCueManager());

		getDataManager().getMainDataManager().registerDatabaseUpdateListener(this);
		
		View mainView;
		switch (currentMode) {
		case CURRENT:
			mainView = inflater.inflate(R.layout.current_cue_fragment, null, true);
			title = ((TextView) mainView.findViewById(R.id.current_cue_title));
			updateCurrentCueTittle();
			break;
		case LIST:
		default:
			currentMode = MODE.LIST;
			mainView = inflater.inflate(R.layout.cue_list_fragment, null, true);
			
			listView = (ListView) mainView.findViewById(R.id.cue_list);
			CueCursorAdapter adapter = new CueCursorAdapter((CueManager) getDataManager());
			listView.setAdapter(adapter);
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			listView.setMultiChoiceModeListener(adapter);
//			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//			listView.setOnItemSelectedListener(new OnItemSelectedListener() {
//
//				@Override
//				public void onItemSelected(AdapterView<?> arg0, View arg1,
//						int arg2, long arg3) {
//					ListView lv = (ListView) arg0;
//					lv.setItemChecked(arg2, true);
//				}
//
//				@Override
//				public void onNothingSelected(AdapterView<?> arg0) {
//					// TODO Auto-generated method stub
//					
//				}
//			});
			
			break;
		}
		return mainView;
	}


	private void updateCurrentCueTittle() {
		if(title == null){
			return;
		}
		CueSyncTask<Void, Void, String> task = ((CueManager) getDataManager()).new CueSyncTask<Void, Void, String>() {

			@Override
			protected void onPostExecute(String result) {
				if ( result == null ){
					result = "NO Current Cue";
				}
				title.setText(result);
			}

			@Override
			protected String doInBackground(Void... params) {
				if ( !getDataManager().getMainDataManager().checkService() ){
					return null;
				}
				try {
					int id = getClientProxy().getCurrentCueId();
					Log.d("CueFragment","current id " + id  );
					Cue cue = ((CueManager) getDataManager()).getCue(id);
					if( cue != null) {
						return cue.getLabel();
					}
				} catch (Exception e) {
					Log.e("CueFragment","error while getting current cue id ", e  );
				}
				return null;
			}
		};
		getDataManager().getMainDataManager().getService().submit(task);
	}

	@Override
	public void onTableUpdateListener(Context context, TABLES table) {
		if( table == TABLES.CUES ){
			switch (currentMode) {
			case CURRENT:
				updateCurrentCueTittle();
				break;
			case LIST:
			default:
			}
			setRefreshActionButtonState(false);
		}
	}

}
