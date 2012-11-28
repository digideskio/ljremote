package com.ljremote.android.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String TAG = "DatabaseHelper";
	
	private static final String DATABASE_NAME= "ljremote.db";
	private static final int DATABASE_VERSION= 1;
	
	private static final String CREATE_TABLE_FORMAT= "CREATE TABLE %s (%s);";
	private static final String DROP_TABLE_FORMAT= "DROP TABLE IF EXISTS %s";
	
	static final class Sequences{
		static final String TABLE_NAME = "SEQUENCES";
		
		static final String COL_ID= "_ID";
		private static final int NUM_COL_ID= 0;
		
		static final String COL_NAME= "NAME";
		private static final int NUM_COL_NAME= 1;
		
		static final String CREATE_TABLE = String.format(CREATE_TABLE_FORMAT,TABLE_NAME,
				COL_ID + " INTEGER PRIMARY KEY, " + COL_NAME + " TEXT NOT NULL");
	}
	
	public DatabaseHelper(Context context) {
		super(context,DATABASE_NAME,null,DATABASE_VERSION);
		Log.i(TAG, "created");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "onCreate");
		Log.i(TAG, Sequences.CREATE_TABLE);
		db.execSQL(Sequences.CREATE_TABLE);
		Log.i(TAG, String.format("Database created %s", Sequences.CREATE_TABLE));
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		
		db.execSQL(String.format(DROP_TABLE_FORMAT, Sequences.TABLE_NAME));
		
		onCreate(db);
	}

}
