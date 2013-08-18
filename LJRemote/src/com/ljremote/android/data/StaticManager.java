package com.ljremote.android.data;

import java.util.List;

import android.database.Cursor;
import android.util.Log;

import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.android.data.Database.Statics;
import com.ljremote.android.json.JSonRpcTask;
import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.model.Static;
import com.ljremote.json.services.StaticService;

public class StaticManager extends AbstractDataManager {

	protected static final String TAG = "StaticManager";
	protected static final int DEFAULT_INTENSITY = 100;
	protected static final boolean DEFAULT_STATE = false;

	public StaticManager(DataManager dm) {
		super(dm);
	}

	private abstract class StaticSyncTask<Progress, Params> extends JSonRpcTask<Params, Progress, Boolean>{
		
		@Override
		protected void onPostExecute(Boolean result) {
			if( result != null && result ) {
				fireDatabaseUpdate();
			}
		}
		
		protected StaticService getClientProxy() throws Exception{
			return dm.getService() == null ? null : dm.getService().getClientProxy(StaticService.class);
		}
	}
	
	private Statics getDB(){
		return dm.getDB().statics();
	}
	
	public Cursor getCursor(){
		return getDB().getCursor();
	}
	
	public boolean updateState(final int id, final boolean enabled){
		if ( !dm.checkService() ){
			return false;
		}
		boolean ret= getDB().update(id, -1, enabled);
		
		StaticSyncTask<Void, Void> task = new StaticSyncTask<Void, Void>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					getClientProxy().enableStatic(id, enabled);
					return true;
				} catch (LJNotFoundException e) {
				} catch (Exception e) {
				}
				return false;
			}
		};
		dm.getService().submit(task);
		return ret;
	}
	
	public boolean updateIntensity(final int id,final int intensity){
		if ( !dm.checkService() ){
			return false;
		}
		boolean ret = getDB().setIntensity(id, intensity);
		
		StaticSyncTask<Void, Void> task = new StaticSyncTask<Void, Void>() {
			
			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					getClientProxy().setStaticIntensity(id, intensity);
					return true;
				} catch (LJNotFoundException e) {
				} catch (Exception e) {
				}
				return false;
			}
		};
		dm.getService().submit(task);
		return ret;
	}
	
	public void updateAllDB(){
		if ( !dm.checkService() ){
			return;
		}
		StaticSyncTask<Void, Void> task = new StaticSyncTask<Void, Void>() {
			
			@Override
			protected Boolean doInBackground(Void... params) {
				Log.d(TAG, "Update");
				List<Static> statics;
				try {
					statics = getClientProxy().getstatics();
					if ( statics == null ){
						return false;
					}
					Log.d(TAG, "Coucou");
					for(Static s : statics){
						s.setIntensity(DEFAULT_INTENSITY);
						s.setEnable(DEFAULT_STATE);
						getDB().insertOrUpdate(s.getId(), s.getLabel(), s.getIntensity(), s.isEnable());
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

	@Override
	public String[] getCursorColumnNames() {
		return Statics.COLUMN_NAMES;
	}

	public boolean clearAll() {
		getDB().clearTable();
		fireDatabaseUpdate();
		return true;
	}

	public void fireDatabaseUpdate(){
		dm.fireDatabaseUpdate(TABLES.STATICS);
	}

	public Static getStaticFromCursor(Cursor cursor) {
		return getDB().getDataFromCursor(cursor);
	}

}
