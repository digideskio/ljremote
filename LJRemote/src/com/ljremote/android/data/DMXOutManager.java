package com.ljremote.android.data;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.android.data.Database.DMXOuts;
import com.ljremote.android.json.JSonRpcTask;
import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.model.DMXChannel;
import com.ljremote.json.services.DMXOutOverrideService;

public class DMXOutManager extends AbstractDataManager {

	private static final int DEFAULT_VALUE = 255;
	protected static final String TAG = "StaticManager";
	protected static final int DEFAULT_INTENSITY = 100;
	protected static final boolean DEFAULT_STATE = false;

	public DMXOutManager(DataManager dm) {
		super(dm);
	}

	private abstract class DMXOutSyncTask<Progress, Params> extends
			JSonRpcTask<Params, Progress, Boolean> {

		@Override
		protected void onPostExecute(Boolean result) {
			if (result != null && result) {
				fireDatabaseUpdate();
			}
		}

		protected DMXOutOverrideService getClientProxy() throws Exception {
			return dm.getService() == null ? null : dm.getService()
					.getClientProxy(DMXOutOverrideService.class);
		}
	}

	private DMXOuts getDB() {
		return dm.getDB().dmxOuts();
	}

	public Cursor getCursor() {
		return getDB().getCursor();
	}

	// public boolean updateState(final int id, final boolean enabled){
	// if ( !dm.checkService() ){
	// return false;
	// }
	// boolean ret= getDB().update(id, -1, enabled);
	//
	// DMXOutSyncTask<Void, Void> task = new DMXOutSyncTask<Void, Void>() {
	//
	// @Override
	// protected Boolean doInBackground(Void... params) {
	// try {
	// getClientProxy().enableDMXOut(id, enabled);
	// return true;
	// } catch (LJNotFoundException e) {
	// } catch (Exception e) {
	// }
	// return false;
	// }
	// };
	// dm.getService().submit(task);
	// return ret;
	// }
	//
	// public boolean updateIntensity(final int id,final int intensity){
	// if ( !dm.checkService() ){
	// return false;
	// }
	// boolean ret = getDB().setIntensity(id, intensity);
	//
	// DMXOutSyncTask<Void, Void> task = new DMXOutSyncTask<Void, Void>() {
	//
	// @Override
	// protected Boolean doInBackground(Void... params) {
	// try {
	// getClientProxy().setDMXOutIntensity(id, intensity);
	// return true;
	// } catch (LJNotFoundException e) {
	// } catch (Exception e) {
	// }
	// return false;
	// }
	// };
	// dm.getService().submit(task);
	// return ret;
	// }

	public boolean addChannel(int channel) {
		getDB().insertOrUpdate(channel, DEFAULT_VALUE, false);
		fireDatabaseUpdate();
		return true;
	}

	@Override
	public String[] getCursorColumnNames() {
		return DMXOuts.COLUMN_NAMES;
	}

	public boolean clearAll() {
		getDB().clearTable();
		fireDatabaseUpdate();
		return true;
	}

	public void fireDatabaseUpdate() {
		dm.fireDatabaseUpdate(TABLES.DMXOUT);
	}

	public DMXChannel getDMXChannelFromCursor(Cursor cursor) {
		return getDB().getDataFromCursor(cursor);
	}

	@Override
	public void updateAllDB() {
	}

	public boolean updateValue(int id, int progress) {
		getDB().update(id, progress);
		return sendDMXOveride();
	}

	public void addOrRemoveChannels(List<Integer> chosenChannels) {
		ArrayList<Integer> toInsert = new ArrayList<Integer>(chosenChannels);
		ArrayList<Integer> toRemove = new ArrayList<Integer>();

		getMainDataManager().getDB();
		SQLiteDatabase db = Database.openDB();
		db.beginTransaction();
		try {
			Cursor cursor = getCursor();
			DMXChannel channel;
			int found;
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					channel = getDMXChannelFromCursor(cursor);
					if ((found = chosenChannels.indexOf(channel.getChannel())) != -1) {
						toInsert.remove(chosenChannels.get(found));
					} else {
						toRemove.add(channel.getChannel());
					}
				} while (cursor.moveToNext());
				cursor.close();
			}

			for (Integer ch : toRemove) {
				getDB().delete(ch);
			}
			for (Integer ch : toInsert) {
				getDB().insert(ch, DEFAULT_VALUE, false);
			}

			db.setTransactionSuccessful();
			fireDatabaseUpdate();
		} finally {
			db.endTransaction();
		}
	}

	public boolean forceDMXOut(int id, boolean force) {
		getDB().force(id, force);
		return sendDMXOveride();
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
	
	public boolean sendDMXOveride() {
		if (!dm.checkService()) {
			return false;
		}
		DMXOutSyncTask<Void, Void> task = new DMXOutSyncTask<Void, Void>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					Log.d("DMXOutManager", "sendDMXOveride");
					return getClientProxy().overRideChannels(getAllDb());
				} catch (LJNotFoundException e) {
				} catch (Exception e) {
					Log.e("DMXOutManager", "error :",e);
				}
				return false;
			}
		};
		task.execute();
		return true;
	}
}
