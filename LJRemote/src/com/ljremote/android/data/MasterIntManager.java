package com.ljremote.android.data;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.util.Log;

import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.android.data.Database.MasterInts;
import com.ljremote.android.json.JSonRpcTask;
import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.model.DMXChannel;
import com.ljremote.json.services.MasterIntService;

public class MasterIntManager extends AbstractDataManager {

	private static final int DEFAULT_VALUE = 255;
	protected static final String TAG = "MasterIntManager";

	public MasterIntManager(DataManager dm) {
		super(dm);
		
		Cursor c = getCursor();
		if ( c == null || c.getCount() == 0 ) {
			updateAllDB();
		}
	}

	private abstract class MasterIntSyncTask<Progress, Params> extends
			JSonRpcTask<Params, Progress, Boolean> {

		@Override
		protected void onPostExecute(Boolean result) {
			if (result != null && result) {
				fireDatabaseUpdate();
			}
		}

		protected MasterIntService getClientProxy() throws Exception {
			return dm.getService() == null ? null : dm.getService()
					.getClientProxy(MasterIntService.class);
		}
	}

	private MasterInts getDB() {
		return dm.getDB().masterInts();
	}

	public Cursor getCursor() {
		return getDB().getCursor();
	}

	@Override
	public String[] getCursorColumnNames() {
		return MasterInts.COLUMN_NAMES;
	}

	public boolean clearAll() {
		getDB().clearTable();
		fireDatabaseUpdate();
		return true;
	}

	public void fireDatabaseUpdate() {
		dm.fireDatabaseUpdate(TABLES.MASTER_INT);
	}

	public DMXChannel getDMXChannelFromCursor(Cursor cursor) {
		return getDB().getDataFromCursor(cursor);
	}

	@Override
	public void updateAllDB() {
		for( int id = 0; id < 9; id++ ) {
			getDB().insertOrUpdate(id, DEFAULT_VALUE);
		}
		fireDatabaseUpdate();
	}

	public boolean updateValue(final int id, final int progress) {
		getDB().update(id, progress);

		MasterIntSyncTask<Void, Void> task = new MasterIntSyncTask<Void, Void>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					Log.d(TAG, "updateValue");
					getClientProxy().setIntensity(id, progress);
				} catch (LJNotFoundException e) {
				} catch (Exception e) {
					Log.e("DMXOutManager", "error :",e);
				}
				return false;
			}
		};
		task.execute();
		return sendMasterInt();
	}

	public List<DMXChannel> getAllDb(){
		Cursor cursor = getCursor();
		List<DMXChannel> channels = new ArrayList<DMXChannel>(cursor.getCount());
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				channels.add(getDMXChannelFromCursor(cursor));
			} while (cursor.moveToNext());
			cursor.close();
		}
		return channels;
	}
	
	public boolean sendMasterInt() {
		if (!dm.checkService()) {
			return false;
		}
		MasterIntSyncTask<Void, Void> task = new MasterIntSyncTask<Void, Void>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					Log.d(TAG, "sendMasterInt");
				} catch (LJNotFoundException e) {
				} catch (Exception e) {
					Log.e(TAG, "error :",e);
				}
				return false;
			}
		};
		task.execute();
		return true;
	}
}
