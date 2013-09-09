package com.ljremote.android.data;

import java.util.List;

import android.database.Cursor;
import android.util.Log;

import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.android.data.Database.LJFunctions;
import com.ljremote.android.json.JSonRpcTask;
import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.model.LJFunction;
import com.ljremote.json.services.LJFunctionService;

public class LJFunctionManager extends AbstractDataManager {

	protected static final String TAG = "LJFunctionManager";

	public LJFunctionManager(DataManager dm) {
		super(dm);
	}

	private abstract class LJFunctionSyncTask<Progress, Params> extends JSonRpcTask<Params, Progress, Boolean>{
		
		protected LJFunctionService getClientProxy() throws Exception{
			return dm.getService() == null ? null : dm.getService().getClientProxy(LJFunctionService.class);
		}
	}
	
	private abstract class LJFunctionDBSyncTask<Progress, Params> extends LJFunctionSyncTask<Params, Progress>{
		
		@Override
		protected void onPostExecute(Boolean result) {
			if( result != null && result ) {
				fireDatabaseUpdate();
			}
		}
		
		protected LJFunctionService getClientProxy() throws Exception{
			return dm.getService() == null ? null : dm.getService().getClientProxy(LJFunctionService.class);
		}
	}
	
	private LJFunctions getDB(){
		return dm.getDB().lJFunctions();
	}
	
	public Cursor getCursor(){
		return getDB().getCursor();
	}
	
	public boolean execute(final int id){
		if ( !dm.checkService() ){
			return false;
		}
		
		LJFunctionSyncTask<Void, Void> task = new LJFunctionSyncTask<Void, Void>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					getClientProxy().executeFunction(id);
					return true;
				} catch (LJNotFoundException e) {
				} catch (Exception e) {
				}
				return false;
			}
		};
		dm.getService().submit(task);
		return true;
	}
	
	private void updateLJFunctionDB(LJFunction function){
		if (function.isGroup()) {
			int groupeId = getDB().insertOrUpdate(0, function.getName(), true, function.getParent_id());
			for( LJFunction ssfunc : function.getFunctions() ){
				ssfunc.setParent_id(groupeId);
				updateLJFunctionDB(ssfunc);
			}
		} else {
			getDB().insertOrUpdate(function.getId(), function.getName(), false, function.getParent_id());
//			Log.d(TAG, "Adding : " + function);
		}
	}
	
	@Override
	public String[] getCursorColumnNames() {
		return LJFunctions.COLUMN_NAMES;
	}

	public boolean clearAll() {
		getDB().clearTable();
		fireDatabaseUpdate();
		return true;
	}

	public void fireDatabaseUpdate(){
		dm.fireDatabaseUpdate(TABLES.LJFUNCTIONS);
	}

	public LJFunction getLJFunctionFromCursor(Cursor cursor) {
		return getDB().getDataFromCursor(cursor);
	}

	public boolean isGroupOfGroupFunction(int func_id) {
		return getDB().isGroupOfGroup(func_id);
	}
	
	public Cursor getFunctionCursor(int parent_id){
		return getDB().getFunctionCursor(parent_id);
	}
	
	public Cursor getGroupFunctionCursor(int parent_id){
		return getDB().getGroupFunctionCursor(parent_id);
	}

	public void refreshData() {
		if ( !dm.checkService() ){
			return;
		}
		LJFunctionDBSyncTask<Void, Void> task = new LJFunctionDBSyncTask<Void, Void>() {
			
			@Override
			protected Boolean doInBackground(Void... params) {
				Log.d(TAG, "Refresh");
				List<LJFunction> ljFunctions;
				try {
					ljFunctions = getClientProxy().getLJFunctions();
					getDB().clearTable();
					Log.e(TAG, "Size : " + ljFunctions.size());
					for(LJFunction group : ljFunctions){
						updateLJFunctionDB(group);
					}
					Log.d(TAG, "Done");
					return true;
				} catch (LJNotFoundException e) {
					Log.e(TAG, "", e);
				} catch (Exception e) {
					Log.e(TAG, "", e);
				}
				return false;
			}
			
		};
		dm.getService().submit(task);
	}

	@Override
	public void updateAllDB() {
	}
}
