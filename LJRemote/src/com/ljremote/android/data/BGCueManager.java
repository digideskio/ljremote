package com.ljremote.android.data;

import java.util.List;

import android.database.Cursor;
import android.util.Log;

import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.android.data.Database.BGCues;
import com.ljremote.android.data.Database.Cues;
import com.ljremote.android.json.JSonRpcTask;
import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.model.BGCue;
import com.ljremote.json.services.BGCueService;

public class BGCueManager extends AbstractDataManager {

	protected static final String TAG = "BGCueManager";

	public BGCueManager(DataManager dm) {
		super(dm);
	}

	private abstract class BGCueSyncTask<Progress, Params> extends JSonRpcTask<Params, Progress, Boolean>{
		
		@Override
		protected void onPostExecute(Boolean result) {
			if( result != null && result ) {
				fireDatabaseUpdate();
			}
		}
		
		protected BGCueService getClientProxy() throws Exception{
			return dm.getService() == null ? null : dm.getService().getClientProxy(BGCueService.class);
		}
	}
	
	private BGCues getDB(){
		return dm.getDB().bgCues();
	}
	
	public Cursor getCursor(){
		return getDB().getCursor();
	}
	
	public void updateAllDB(){
		if ( !dm.checkService() ){
			return;
		}
		BGCueSyncTask<Void, Void> task = new BGCueSyncTask<Void, Void>() {
			
			@Override
			protected Boolean doInBackground(Void... params) {
				Log.d(TAG, "Update");
				List<BGCue> cues;
				try {
					cues = getClientProxy().getBGCueList();
					if ( cues == null ){
						return false;
					}
					for(BGCue c : cues){
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

	public BGCue getBGCueFromCursor(Cursor c){
		return getDB().getDataFromCursor(c);
	}

	@Override
	public String[] getCursorColumnNames() {
		return Cues.COLUMN_NAMES;
	}

	@Override
	public void fireDatabaseUpdate() {
		dm.fireDatabaseUpdate(TABLES.BGCUES);
	}

	public void clearAll() {
		getDB().clearTable();
		fireDatabaseUpdate();
	}
}
