package com.ljremote.android.data;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.ljremote.android.types.Static;

public class DataManager {

	private Database db;
	
	public enum TABLES{
		STATICS,SEQUENCES
	}
	
	public DataManager(Context context) {
//		dbh = new DatabaseHelper(context);
//		SQLiteDatabase db = dbh.getWritableDatabase();
//		ContentValues contentValues = new ContentValues();
//		contentValues.put(Sequences.COL_ID, 0);
//		contentValues.put(Sequences.COL_NAME, "A sequence");
//		db.insert(Sequences.TABLE_NAME, null, contentValues);
//		db.close();
//		Log.i("coco", "done");
		db= new Database(context);
		fillDummyDatabase();
	}

	private void fillDummyDatabase() {
		if(db != null){
			for(int i=0; i < 20; i++){
				db.statics().insert(i, "Static " + i, 100, true);
			}
		}
	}
	
	public boolean updateStaticState(int id, boolean enabled){
		return db.statics().update(id, -1, enabled);
	}
	
	public boolean updateStaticIntensity(int id,int intensity){
		return db.statics().setIntensity(id, intensity);
	}
	
	public Cursor getCursor(TABLES table){
		switch (table) {
		case STATICS:
			return db.statics().getCursor();
		default:
			return null;
		}
	}
	
	
	public List<Static> getStaticList(){
		return db == null ? null : db.statics().getStaticList();
	}

}
