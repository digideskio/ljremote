package com.ljremote.android.data;

import java.util.List;

import android.database.Cursor;
import android.util.Log;

import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.android.data.Database.Cues;
import com.ljremote.android.json.JSonRpcTask;
import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.model.Cue;
import com.ljremote.json.services.CueService;

public class CueManager extends AbstractDataManager {

	protected static final String TAG = "CueManager";

	public CueManager(DataManager dm) {
		super(dm);
	}

	public abstract class CueSyncTask<Progress, Params, Result> extends JSonRpcTask<Params, Progress, Result>{
		
		protected CueService getClientProxy() throws Exception{
			return dm.getService() == null ? null : dm.getService().getClientProxy(CueService.class);
		}
	}
	
	private abstract class CueDataSyncTask<Progress, Params> extends CueSyncTask<Params, Progress, Boolean>{
		
		@Override
		protected void onPostExecute(Boolean result) {
			if( result != null && result ) {
				fireDatabaseUpdate();
			}
		}
	}
	
	private Cues getDB(){
		return dm.getDB().cues();
	}
	
	public Cursor getCursor(){
		return getDB().getCursor();
	}
	
	public void updateAllDB(){
	}

	
	
	@Override
	public void refreshData() {
		if ( !dm.checkService() ){
			return;
		}
		CueDataSyncTask<Void, Void> task = new CueDataSyncTask<Void, Void>() {
			
			@Override
			protected Boolean doInBackground(Void... params) {
				Log.d(TAG, "Update");
				List<Cue> cues;
				try {
					cues = getClientProxy().getCueList();
					if ( cues == null ){
						return false;
					}
					getDB().clearTable();
					Log.d(TAG, "Coucou");
					for(Cue c : cues){
						getDB().insertOrUpdate(c.getId(), c.getLabel());
					}
					Log.d(TAG, "Done");
					return true;
				} catch (LJNotFoundException e) {
				} catch (Exception e) {
				}
				return false;
			}
			
		};
		dm.getService().submit(task);
	}

	public Cue getCueFromCursor(Cursor c){
		return getDB().getDataFromCursor(c);
	}

	@Override
	public String[] getCursorColumnNames() {
		return Cues.COLUMN_NAMES;
	}

	public boolean clearAll() {
		getDB().clearTable();
		fireDatabaseUpdate();
		return true;
	}
	
	public void fireDatabaseUpdate(){
		dm.fireDatabaseUpdate(TABLES.CUES);
	}

	public void loadCue(final int id) {
		if ( !dm.checkService() ){
			return;
		}
		CueDataSyncTask<Void, Void> task = new CueDataSyncTask<Void, Void>() {
			
			@Override
			protected Boolean doInBackground(Void... params) {
				Log.d(TAG, "Update");
				try {
					return getClientProxy().LoadCue(id);
				} catch (LJNotFoundException e) {
				} catch (Exception e) {
				}
				return false;
			}
			
		};
		dm.getService().submit(task);
	}

	public Cue getCurrentCue() {
		return null;
	}

	public Cue getCue(int id) {
		return getDB().getCue(id);
	}
}
