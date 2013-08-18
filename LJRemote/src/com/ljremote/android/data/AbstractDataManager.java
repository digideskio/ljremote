package com.ljremote.android.data;

import android.content.Context;
import android.database.Cursor;

public abstract class AbstractDataManager {
	
	protected DataManager dm;
	protected Context context;
	
	public AbstractDataManager(DataManager dm) {
		this.dm = dm;
		this.context = dm.getContext();
	}
	
	public abstract Cursor getCursor();

	public abstract void updateAllDB();
	
	public Context getContext(){
		return context;
	}
	
	public DataManager getMainDataManager(){
		return dm;
	}

	public abstract String[] getCursorColumnNames();
	
	public abstract void fireDatabaseUpdate();
}
