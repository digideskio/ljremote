package com.ljremote.android.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ljremote.android.types.Static;

public class Database {
	private static final String TAG = "Database";
	private static final String DATABASE_NAME = "ljremote.db";
	private static final int DATABASE_VERSION = 1;

	private static final String CREATE_TABLE_FORMAT = "CREATE TABLE %s (%s);";
	private static final String DROP_TABLE_FORMAT = "DROP TABLE IF EXISTS %s";

	final class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			Log.v(TAG, "created");
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(Sequences.CREATE_TABLE);
			Log.i(TAG, String.format("Database created: %s",
					Sequences.CREATE_TABLE));
			db.execSQL(Statics.CREATE_TABLE);
			Log.i(TAG,
					String.format("Database created: %s", Statics.CREATE_TABLE));
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");

			db.execSQL(String.format(DROP_TABLE_FORMAT, Sequences.TABLE_NAME));
			db.execSQL(String.format(DROP_TABLE_FORMAT, Statics.TABLE_NAME));

			onCreate(db);
		}

	}

	static DatabaseHelper dbh;
	private static SQLiteDatabase db;

	static final class Sequences {
		static final String TABLE_NAME = "SEQUENCES";

		static final String COL_ID = "_id";
		private static final int NUM_COL_ID = 0;

		static final String COL_LABEL = "LABEL";
		private static final int NUM_COL_LABEL = 1;

		static final String CREATE_TABLE = String.format(CREATE_TABLE_FORMAT,
				TABLE_NAME, COL_ID + " INTEGER PRIMARY KEY, " + COL_LABEL
						+ " TEXT NOT NULL");

		public static void insert(int id, final String name) {
			ContentValues values = new ContentValues();
			values.put(COL_ID, id);
			values.put(COL_LABEL, name);
			openDB().insert(TABLE_NAME, null, values);
			closeDB();
			Log.d(TAG, "Sequence inserted");
		}
	}

	public static final class Statics {
		static final String TABLE_NAME = "STATICS";

		public static final String COL_ID = "_id";
		public static final int NUM_COL_ID = 0;

		public static final String COL_LABEL = "LABEL";
		public static final int NUM_COL_LABEL = 1;

		public static final String COL_INTENSITY = "INTENSITY";
		public static final int NUM_COL_INTENSITY = 2;

		public static final String COL_ENABLED = "ENABLED";
		public static final int NUM_COL_ENABLED = 3;

		public static final String[] COLUMN_NAMES = {
			COL_ID,COL_LABEL,COL_INTENSITY,COL_ENABLED
		};
		static final String CREATE_TABLE = String.format(CREATE_TABLE_FORMAT,
				TABLE_NAME, COL_ID + " INTEGER PRIMARY KEY, " + COL_LABEL
						+ " TEXT NOT NULL, " + COL_INTENSITY + " INTEGER, "
						+ COL_ENABLED + " INTEGER");

		private static final String SELECT_ALL = "SELECT * FROM " + TABLE_NAME;

		public boolean insert(int id, final String label, int intensity,
				boolean enable) {
			ContentValues contentValues = new ContentValues();
			contentValues.put(COL_ID, id);
			contentValues.put(COL_LABEL, label);
			contentValues.put(COL_INTENSITY, intensity);
			contentValues.put(COL_ENABLED, enable);
			long ret = openDB().insert(TABLE_NAME, null, contentValues);
			closeDB();
			Log.d(TAG, String.format("Static %d insered", id));
			return ret != -1;
		}

		public boolean update(int id, final String label, int intensity,
				boolean enable) {
			ContentValues contentValues = new ContentValues();
			if (label != null)
				contentValues.put(COL_LABEL, label);
			if (intensity >= 0)
				contentValues.put(COL_INTENSITY, intensity);
			contentValues.put(COL_ENABLED, enable);
			int ret = openDB().update(TABLE_NAME, contentValues,
					COL_ID + " = ?", new String[] { String.valueOf(id) });
			closeDB();
			Log.d(TAG, String.format("Static %d updated: %s: %s, %s: %d, %s: %s", id,COL_LABEL,label,COL_INTENSITY,intensity,COL_ENABLED,enable));
			return ret > 0;
		}

		public boolean update(int id, final String label, int intensity) {
			ContentValues contentValues = new ContentValues();
			if (label != null)
				contentValues.put(COL_LABEL, label);
			if (intensity >= 0)
				contentValues.put(COL_INTENSITY, intensity);
			int ret = openDB().update(TABLE_NAME, contentValues,
					COL_ID + " = ?", new String[] { String.valueOf(id) });
			closeDB();
			Log.d(TAG, String.format("Static %d updated: %s: %s, %s: %d", id,COL_LABEL,label,COL_INTENSITY,intensity));
			return ret > 0;
		}

		public boolean update(int id, int intensity, boolean enable) {
			return update(id, null, intensity, enable);
		}

		public boolean enable(int id, boolean enable) {
			return update(id, null, -1, enable);
		}

		public boolean setIntensity(int id, int intensity) {
			return update(id, null, intensity);
		}

		public Cursor getCursor(){
			Cursor c = readDB().rawQuery(SELECT_ALL, null);
//			closeDB();
			return c;
		}
		
		public List<Static> getStaticList() {
			Cursor c = openDB().rawQuery(SELECT_ALL, null);
			List<Static> statics = new ArrayList<Static>(c.getCount());
			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				statics.add(new Static(c.getInt(NUM_COL_ID), c
						.getString(NUM_COL_LABEL), c.getInt(NUM_COL_INTENSITY),
						c.getInt(NUM_COL_ENABLED) > 0));
			}
			closeDB();
			return statics;
		}
		
	}

	public Database(Context context) {
		dbh = new DatabaseHelper(context);
	}
	
	public Sequences sequences() {
		return new Sequences();
	}

	public Statics statics() {
		return new Statics();
	}

	public static SQLiteDatabase openDB() {
		if (db != null) {
			if (db.isOpen()) {
				return db;
			}
			db.close();
		}
		return db = dbh.getWritableDatabase();
	}

	public static SQLiteDatabase readDB() {
		if (db != null) {
			if (db.isReadOnly()) {
				return db;
			}
			db.close();
		}
		return db = dbh.getWritableDatabase();
	}

	public static void closeDB() {
		if (db != null) {
			db.close();
		}
	}
	
}
