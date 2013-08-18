package com.ljremote.android.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ljremote.json.model.BGCue;
import com.ljremote.json.model.Cue;
import com.ljremote.json.model.CueList;
import com.ljremote.json.model.Seq;
import com.ljremote.json.model.Static;


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
			createTable(db, Sequences.CREATE_TABLE);
			createTable(db, Statics.CREATE_TABLE);
			createTable(db, Cues.CREATE_TABLE);
			createTable(db, BGCues.CREATE_TABLE);
			createTable(db, CueLists.CREATE_TABLE);
		}
		
		public void createTable(SQLiteDatabase db, String createSql){
			db.execSQL(createSql);
			Log.i(TAG, String.format("Database created: %s", createSql));
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");

			db.execSQL(String.format(DROP_TABLE_FORMAT, Sequences.TABLE_NAME));
			db.execSQL(String.format(DROP_TABLE_FORMAT, Statics.TABLE_NAME));
			db.execSQL(String.format(DROP_TABLE_FORMAT, Cues.TABLE_NAME));
			db.execSQL(String.format(DROP_TABLE_FORMAT, BGCues.TABLE_NAME));
			db.execSQL(String.format(DROP_TABLE_FORMAT, CueLists.TABLE_NAME));

			onCreate(db);
		}

	}

	static DatabaseHelper dbh;
	private static SQLiteDatabase db;

	public abstract static class TableHelper<T> {
		
		public abstract String getTableName();
		
		public String getSelectAllQueryStr() {
			return "SELECT * FROM " + getTableName();
		}

		public Cursor getCursor() {
			Cursor c = readDB().rawQuery(getSelectAllQueryStr(), null);
			return c;
		}

		public abstract T getDataFromCursor(Cursor c);
		
		public boolean clearTable(){
			openDB().delete(getTableName(), null, null);
			Log.d(TAG, "Delete rows in table " + getTableName());
			return true;
		}
	}
	
	public static final class Sequences extends TableHelper<Seq>{
		static final String TABLE_NAME = "SEQUENCES";

		static final String COL_ID = "_id";
		private static final int NUM_COL_ID = 0;

		static final String COL_LABEL = "LABEL";
		private static final int NUM_COL_LABEL = 1;

		static final String CREATE_TABLE = String.format(CREATE_TABLE_FORMAT,
				TABLE_NAME, COL_ID + " INTEGER PRIMARY KEY, " + COL_LABEL
						+ " TEXT NOT NULL");

		public static final String[] COLUMN_NAMES = new String[]{
			COL_ID, COL_LABEL
		};
		
		private static final String SELECT_ALL = "SELECT * FROM " + TABLE_NAME;

		@Override
		public String getTableName() {
			return TABLE_NAME;
		}

		@Override
		public Seq getDataFromCursor(Cursor c) {
			Seq seq = new Seq();
			seq.setId(c.getInt(NUM_COL_ID));
			seq.setLabel(c.getString(NUM_COL_LABEL));
			return seq;
		}

		public boolean insertOrUpdate(int id, String label) {
			Cursor res = openDB().rawQuery(SELECT_ALL + " WHERE " + COL_ID + " = ? ", new String[]{ String.valueOf(id) });
			if ( res != null && res.getCount() > 0 ) {
				return update(id, label);
			} else {
				return insert(id, label);
			}
		}
		
		public static boolean insert(int id, final String name) {
			ContentValues values = new ContentValues();
			values.put(COL_ID, id);
			values.put(COL_LABEL, name);
			long ret = openDB().insert(TABLE_NAME, null, values);
			closeDB();
			Log.d(TAG, "Sequence inserted");
			return ret != -1;
		}
		
		public static boolean update(int id, final String label) {
			ContentValues values = new ContentValues();
			values.put(COL_ID, id);
			values.put(COL_LABEL, label);
			long ret = openDB().update(TABLE_NAME, values,
					COL_ID + " = ?", new String[] { String.valueOf(id) });
			Log.d(TAG, String.format("Sequence %d updated: %s: %s", id,COL_LABEL,label));
			return ret != -1;
		}
	}

	public static final class Cues extends TableHelper<Cue> {
		static final String TABLE_NAME = "CUES";
		
		static final String COL_ID = "_id";
		private static final int NUM_COL_ID = 0;
		
		static final String COL_LABEL = "LABEL";
		private static final int NUM_COL_LABEL = 1;
		
		static final String CREATE_TABLE = String.format(CREATE_TABLE_FORMAT,
				TABLE_NAME, COL_ID + " INTEGER PRIMARY KEY, " + COL_LABEL
				+ " TEXT NOT NULL");
		private static final String SELECT_ALL = "SELECT * FROM " + TABLE_NAME;

		public static final String[] COLUMN_NAMES = new String[]{
			COL_ID,COL_LABEL
		};
		
		public boolean insertOrUpdate (int id, final String label) {
			Cursor res = openDB().rawQuery(SELECT_ALL + " WHERE " + COL_ID + " = ? ", new String[]{ String.valueOf(id) });
			if ( res != null && res.getCount() > 0 ) {
				return update(id, label);
			} else {
				return insert(id, label);
			}
		}
		
		public static boolean insert(int id, final String name) {
			ContentValues values = new ContentValues();
			values.put(COL_ID, id);
			values.put(COL_LABEL, name);
			long ret = openDB().insert(TABLE_NAME, null, values);
			closeDB();
			Log.d(TAG, "Cue inserted");
			return ret != -1;
		}
		
		public static boolean update(int id, final String label) {
			ContentValues values = new ContentValues();
			values.put(COL_ID, id);
			values.put(COL_LABEL, label);
			long ret = openDB().update(TABLE_NAME, values,
					COL_ID + " = ?", new String[] { String.valueOf(id) });
			Log.d(TAG, String.format("Cue %d updated: %s: %s", id,COL_LABEL,label));
			return ret != -1;
		}

		@Override
		public String getTableName() {
			return TABLE_NAME;
		}

		@Override
		public Cue getDataFromCursor(Cursor c) {
			Cue cue = new Cue();
			cue.setId(c.getInt(NUM_COL_ID));
			cue.setLabel(c.getString(NUM_COL_LABEL));
			return cue;
		}
	}

	public static final class Statics extends TableHelper<Static>{
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

		public boolean insertOrUpdate (int id, final String label, int intensity,
				boolean enable) {
			Cursor res = openDB().rawQuery(SELECT_ALL + " WHERE " + COL_ID + " = ? ", new String[]{ String.valueOf(id) });
			if ( res != null && res.getCount() > 0 ) {
				return update(id, label, -1);
			} else {
				return insert(id, label, intensity, enable);
			}
		}
		
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
			return c;
		}
		
		@Override
		public String getTableName() {
			return TABLE_NAME;
		}

		@Override
		public Static getDataFromCursor(Cursor c) {
			Static s = new Static();
			s.setId(c.getInt(NUM_COL_ID));
			s.setLabel(c.getString(NUM_COL_LABEL));
			s.setEnable(c.getInt(NUM_COL_ENABLED) > 0);
			s.setIntensity(c.getInt(NUM_COL_INTENSITY));
			return s;
		}
		
	}
	
	public static final class BGCues extends TableHelper<BGCue>{
		static final String TABLE_NAME = "BGCUES";
		
		static final String COL_ID = "_id";
		private static final int NUM_COL_ID = 0;
		
		static final String COL_LABEL = "LABEL";
		private static final int NUM_COL_LABEL = 1;
		
		static final String CREATE_TABLE = String.format(CREATE_TABLE_FORMAT,
				TABLE_NAME, COL_ID + " INTEGER PRIMARY KEY, " + COL_LABEL
				+ " TEXT NOT NULL");
		private static final String SELECT_ALL = "SELECT * FROM " + TABLE_NAME;

		public static final String[] COLUMN_NAMES = new String[]{
			COL_ID,COL_LABEL
		};
		
		public boolean insertOrUpdate (int id, final String label) {
			Cursor res = openDB().rawQuery(SELECT_ALL + " WHERE " + COL_ID + " = ? ", new String[]{ String.valueOf(id) });
			if ( res != null && res.getCount() > 0 ) {
				return update(id, label);
			} else {
				return insert(id, label);
			}
		}
		
		public static boolean insert(int id, final String name) {
			ContentValues values = new ContentValues();
			values.put(COL_ID, id);
			values.put(COL_LABEL, name);
			long ret = openDB().insert(TABLE_NAME, null, values);
			closeDB();
			Log.d(TAG, "BGCue inserted");
			return ret != -1;
		}
		
		public static boolean update(int id, final String label) {
			ContentValues values = new ContentValues();
			values.put(COL_ID, id);
			values.put(COL_LABEL, label);
			long ret = openDB().update(TABLE_NAME, values,
					COL_ID + " = ?", new String[] { String.valueOf(id) });
			Log.d(TAG, String.format("BGCue %d updated: %s: %s", id,COL_LABEL,label));
			return ret != -1;
		}
		
		@Override
		public String getTableName() {
			return TABLE_NAME;
		}

		@Override
		public BGCue getDataFromCursor(Cursor c) {
			BGCue cue = new BGCue();
			cue.setId(c.getInt(NUM_COL_ID));
			cue.setLabel(c.getString(NUM_COL_LABEL));
			return cue;
		}
		
	}
	
	public static final class CueLists extends TableHelper<CueList>{
		static final String TABLE_NAME = "CUELISTS";
		
		static final String COL_ID = "_id";
		private static final int NUM_COL_ID = 0;
		
		static final String COL_LABEL = "LABEL";
		private static final int NUM_COL_LABEL = 1;
		
		static final String CREATE_TABLE = String.format(CREATE_TABLE_FORMAT,
				TABLE_NAME, COL_ID + " INTEGER PRIMARY KEY, " + COL_LABEL
				+ " TEXT NOT NULL");
		private static final String SELECT_ALL = "SELECT * FROM " + TABLE_NAME;

		public static final String[] COLUMN_NAMES = new String[]{
			COL_ID,COL_LABEL
		};
		
		public boolean insertOrUpdate (int id, final String label) {
			Cursor res = openDB().rawQuery(SELECT_ALL + " WHERE " + COL_ID + " = ? ", new String[]{ String.valueOf(id) });
			if ( res != null && res.getCount() > 0 ) {
				return update(id, label);
			} else {
				return insert(id, label);
			}
		}
		
		public static boolean insert(int id, final String name) {
			ContentValues values = new ContentValues();
			values.put(COL_ID, id);
			values.put(COL_LABEL, name);
			long ret = openDB().insert(TABLE_NAME, null, values);
			closeDB();
			Log.d(TAG, "CueList inserted");
			return ret != -1;
		}
		
		public static boolean update(int id, final String label) {
			ContentValues values = new ContentValues();
			values.put(COL_ID, id);
			values.put(COL_LABEL, label);
			long ret = openDB().update(TABLE_NAME, values,
					COL_ID + " = ?", new String[] { String.valueOf(id) });
			Log.d(TAG, String.format("CueList %d updated: %s: %s", id,COL_LABEL,label));
			return ret != -1;
		}
		@Override
		public String getTableName() {
			return TABLE_NAME;
		}

		@Override
		public CueList getDataFromCursor(Cursor c) {
			CueList cueList = new CueList();
			cueList.setId(c.getInt(NUM_COL_ID));
			cueList.setLabel(c.getString(NUM_COL_LABEL));
			return cueList;
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
	
	public Cues cues() {
		return new Cues();
	}
	
	public BGCues bgCues() {
		return new BGCues();
	}
	
	public CueLists cueLists() {
		return new CueLists();
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
