package com.ljremote.android.data;

import java.util.List;

import android.database.Cursor;
import android.util.Log;

import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.android.data.Database.Sequences;
import com.ljremote.android.json.JSonRpcTask;
import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.model.Seq;
import com.ljremote.json.services.SequenceService;

public class SequenceManager extends AbstractDataManager {

	protected static final String TAG = "SequenceManager";

	public SequenceManager(DataManager dm) {
		super(dm);
	}

	private abstract class SequenceSyncTask<Progress, Params> extends JSonRpcTask<Params, Progress, Boolean>{
		
		@Override
		protected void onPostExecute(Boolean result) {
			if( result != null && result ) {
				fireDatabaseUpdate();
			}
		}
		
		protected SequenceService getClientProxy() throws Exception{
			return dm.getService() == null ? null : dm.getService().getClientProxy(SequenceService.class);
		}
	}
	
	private Sequences getDB(){
		return dm.getDB().sequences();
	}
	
	public Cursor getCursor(){
		return getDB().getCursor();
	}
	
	public void updateAllDB(){
		if ( !dm.checkService() ){
			return;
		}
		SequenceSyncTask<Void, Void> task = new SequenceSyncTask<Void, Void>() {
			
			@Override
			protected Boolean doInBackground(Void... params) {
				Log.d(TAG, "Update");
				List<Seq> sequences;
				try {
					sequences = getClientProxy().getSequenceList();
					if ( sequences == null ){
						return false;
					}
					for(Seq seq : sequences){
						getDB().insertOrUpdate(seq.getId(), seq.getLabel());
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

	public boolean clearAll(){
		getDB().clearTable();
		dm.fireDatabaseUpdate(TABLES.SEQUENCES);
		return true;
	}
	
	public Seq getCueFromCursor(Cursor c){
		return getDB().getDataFromCursor(c);
	}

	@Override
	public String[] getCursorColumnNames() {
		return Sequences.COLUMN_NAMES;
	}

	@Override
	public void fireDatabaseUpdate() {
		dm.fireDatabaseUpdate(TABLES.SEQUENCES);
	}
}
