package com.ljremote.android.data;

import java.util.List;

import android.database.Cursor;
import android.util.Log;

import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.android.data.Database.CueLists;
import com.ljremote.android.json.JSonRpcTask;
import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.model.CueList;
import com.ljremote.json.services.CueListService;

public class CueListManager extends AbstractDataManager {

	protected static final String TAG = "CueListManager";

	public CueListManager(DataManager dm) {
		super(dm);
	}

	private abstract class CueListSyncTask<Progress, Params> extends JSonRpcTask<Params, Progress, Boolean>{
		
		protected CueListService getClientProxy() throws Exception{
			return dm.getService() == null ? null : dm.getService().getClientProxy(CueListService.class);
		}
	}
	
	private abstract class CueListDataSyncTask<Progress, Params> extends CueListSyncTask<Params, Progress>{
		
		@Override
		protected void onPostExecute(Boolean result) {
			if( result != null && result ) {
				fireDatabaseUpdate();
			}
		}
	}
	
	private CueLists getDB(){
		return dm.getDB().cueLists();
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
		CueListDataSyncTask<Void, Void> task = new CueListDataSyncTask<Void, Void>() {
			
			@Override
			protected Boolean doInBackground(Void... params) {
				Log.d(TAG, "Update");
				List<CueList> cueLists;
				try {
					cueLists = getClientProxy().getCueListList();
					if ( cueLists == null ){
						return false;
					}
					getDB().clearTable();
					for(CueList cueList : cueLists){
						getDB().insertOrUpdate(cueList.getId(), cueList.getLabel());
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

	public CueList getCueListFromCursor(Cursor c){
		return getDB().getDataFromCursor(c);
	}

	@Override
	public String[] getCursorColumnNames() {
		return CueLists.COLUMN_NAMES;
	}

	public void fireDatabaseUpdate(){
		dm.fireDatabaseUpdate(TABLES.CUELISTS);
	}

	public void clearAll() {
		getDB().clearTable();
		fireDatabaseUpdate();
	}

	public void loadCueList(final int id) {
		if ( !dm.checkService() ){
			return;
		}
		CueListSyncTask<Void, Void> task = new CueListSyncTask<Void, Void>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				Log.d(TAG, "Update");
				try {
					return getClientProxy().loadCueList(id);
				} catch (LJNotFoundException e) {
				} catch (Exception e) {
				}
				return false;
			}
		};
		dm.getService().submit(task);
	}
	
}
