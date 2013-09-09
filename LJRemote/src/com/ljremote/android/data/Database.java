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
import com.ljremote.json.model.DMXChannel;
import com.ljremote.json.model.LJFunction;
import com.ljremote.json.model.Seq;
import com.ljremote.json.model.Static;


public class Database {
	private static final String TAG = "Database";
	private static final String DATABASE_NAME = "ljremote.db";
	private static final int DATABASE_VERSION = 4;

	private static final String CREATE_TABLE_FORMAT = "CREATE TABLE %s (%s);";
	private static final String DROP_TABLE_FORMAT = "DROP TABLE IF EXISTS %s";

	final static class DatabaseHelper extends SQLiteOpenHelper {

		private static DatabaseHelper mInstance = null;
		
		public static DatabaseHelper getInstance(Context context) {
			if ( mInstance == null ) {
				mInstance = new DatabaseHelper(context);
			}
			return mInstance;
		}
		
		private DatabaseHelper(Context context) {
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
			createTable(db, LJFunctions.CREATE_TABLE);
			createTable(db, LJFunctions.CREATE_GROUP_TABLE);
			createTable(db, DMXOuts.CREATE_TABLE);
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
			db.execSQL(String.format(DROP_TABLE_FORMAT, LJFunctions.TABLE_NAME));
			db.execSQL(String.format(DROP_TABLE_FORMAT, LJFunctions.GROUP_TABLE_NAME));
			db.execSQL(String.format(DROP_TABLE_FORMAT, DMXOuts.TABLE_NAME));

			onCreate(db);
		}

	}

	static DatabaseHelper dbh;
	private static SQLiteDatabase db;

	public abstract static class TableHelper<T> {
		
		public abstract String getTableName();
		public abstract String getColKey();
		
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
		
		public boolean delete(Object key){
			return openDB().delete(getTableName(), getColKey() + "=" + String.valueOf(key), null) != 0;
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

		@Override
		public String getColKey() {
			return COL_ID;
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
		
		public Cue getCue(int id){
			Cursor res = openDB()
					.query(getTableName(), null, COL_ID + " = ? ",
							new String[]{ String.valueOf(id) }, null, null, null);
//					.rawQuery(SELECT_ALL + " WHERE " + COL_ID + " = ? ", new String[]{ String.valueOf(id) });
			if ( res == null || res.getCount() <= 0) {
				Log.d(TAG, "res null");
				return null;
			}
			res.moveToFirst();
			Cue cue = getDataFromCursor(res);
			res.close();
			return cue; 
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
		@Override
		public String getColKey() {
			return COL_ID;
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
		@Override
		public String getColKey() {
			return COL_ID;
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
		@Override
		public String getColKey() {
			return COL_ID;
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
		@Override
		public String getColKey() {
			return COL_ID;
		}
	}
	
	public static final class LJFunctions extends TableHelper<LJFunction> {
		static final String TABLE_NAME = "LJFUNCTIONS";
		static final String GROUP_TABLE_NAME = "LJFUNCTIONS_GROUPS";

		static final String COL_ID = "_id";
		private static final int NUM_COL_ID = 0;
		
		static final String COL_NAME = "NAME";
		private static final int NUM_COL_NAME = 1;
		
		static final String COL_PARENT_ID = "PARENT_ID";
		private static final int NUM_COL_PARENT_ID = 2;
		
		static final String CREATE_TABLE = String.format(CREATE_TABLE_FORMAT,
				TABLE_NAME, COL_ID + " INTEGER PRIMARY KEY, " + COL_NAME
				+ " TEXT NOT NULL, " + COL_PARENT_ID + " INTEGER");
		
		static final String CREATE_GROUP_TABLE = String.format(CREATE_TABLE_FORMAT,
				GROUP_TABLE_NAME, COL_ID + " INTEGER PRIMARY KEY, " + COL_NAME
				+ " TEXT NOT NULL, " + COL_PARENT_ID + " INTEGER");
		
		private static final String SELECT_ALL = "SELECT * FROM " + TABLE_NAME;
		private static final String SELECT_ALL_GROUP = "SELECT * FROM " + GROUP_TABLE_NAME;
		public static final String[] COLUMN_NAMES = {
			COL_ID,COL_NAME, COL_PARENT_ID
		};
		
		@Override
		public String getTableName() {
			return TABLE_NAME;
		}

		@Override
		public LJFunction getDataFromCursor(Cursor c) {
			LJFunction function = new LJFunction();
			function.setId(c.getInt(NUM_COL_ID));
			function.setName(c.getString(NUM_COL_NAME));
			function.setParent_id(c.getInt(NUM_COL_PARENT_ID));
			return function;
		}
		
		public int insertOrUpdate(int id, final String name, boolean group, int parent_id) {
			Cursor res;
			if ( group ) {
				res = openDB().rawQuery(
						"SELECT * FROM " + GROUP_TABLE_NAME + " WHERE " + COL_NAME + " = ? ",
						new String[]{ name });
			} else {
				res = openDB().rawQuery(SELECT_ALL + " WHERE " + COL_ID + " = ? ", new String[]{ String.valueOf(id) });
			}
			if ( res != null && res.getCount() > 0 ) {
				res.moveToFirst();
				return update(res.getInt(NUM_COL_ID), name, group, parent_id);
			} else {
				return insert(id, name, group, parent_id);
			}
		}
		
		public static int insert(int id, final String name, boolean group, int parent_id) {
			ContentValues values = new ContentValues();
			if (!group) {values.put(COL_ID, id);};
			values.put(COL_NAME, name);
			values.put(COL_PARENT_ID, parent_id);
			String tableName = group ? GROUP_TABLE_NAME : TABLE_NAME;
			openDB().insert(tableName, null, values);
			closeDB();
			Log.d(TAG, "LJFunction inserted");
			return id;
		}
		
		public static int update(int id, final String name, boolean group, int parent_id) {
			ContentValues values = new ContentValues();
			values.put(COL_ID, id);
			values.put(COL_NAME, name);
			values.put(COL_PARENT_ID, parent_id);
			String tableName = group ? GROUP_TABLE_NAME : TABLE_NAME;
			openDB().update(tableName, values,
					COL_ID + " = ?", new String[] { String.valueOf(id) });
			Log.d(TAG, String.format("LJFunction %d updated: %s: %s, %s: %d", id,COL_NAME,name, COL_PARENT_ID, parent_id));
			return id;
		}
		
		public Cursor getFunctionCursor(int parent_id){
			Cursor c = readDB().rawQuery(SELECT_ALL + " WHERE " + COL_PARENT_ID + " = "  + parent_id, null);
			return c;
		}
		
		public Cursor getGroupFunctionCursor(){
			Cursor c = readDB().rawQuery(SELECT_ALL_GROUP, null);
			return c;
		}
		
		public Cursor getGroupFunctionCursor(int parent_id){
			Cursor c = readDB().rawQuery(getSelectAllQueryOnGroup(parent_id), null);
			return c;
		}
		
		public String getSelectAllQueryOnGroup(){
			return SELECT_ALL_GROUP;
		}
		
		public String getSelectAllQueryOnGroup(int parent_id){
			return SELECT_ALL_GROUP + " WHERE " + COL_PARENT_ID + " = "  + parent_id;
		}
		
		public boolean isGroup(int func_id) {
			Cursor res = openDB().rawQuery(SELECT_ALL + " WHERE " + COL_PARENT_ID + " = ? LIMIT 1",
					new String[]{ String.valueOf(func_id) });
			return res != null && res.getCount() > 0;
		}
		
		public boolean isGroupOfGroup(int func_id) {
			Cursor res = openDB().rawQuery(SELECT_ALL_GROUP + " WHERE " + COL_PARENT_ID + " = ? LIMIT 1",
					new String[]{ String.valueOf(func_id) });
			return res != null && res.getCount() > 0;
		}
		@Override
		public String getColKey() {
			return COL_ID;
		}
	}
	
	public static final class DMXOuts extends TableHelper<DMXChannel>{
		static final String TABLE_NAME = "DMXOUTS";

		public static final String COL_ID = "_id";
		public static final int NUM_COL_ID = 0;

		public static final String COL_VALUE = "VALUE";
		public static final int NUM_COL_VALUE = 1;

		public static final String COL_FORCE= "FORCE";
		public static final int NUM_COL_FORCE = 2;

		public static final String[] COLUMN_NAMES = {
			COL_ID,COL_VALUE,COL_FORCE
		};
		static final String CREATE_TABLE = String.format(CREATE_TABLE_FORMAT,
				TABLE_NAME, COL_ID + " INTEGER PRIMARY KEY, "
						+ COL_VALUE + " INTEGER, "
						+ COL_FORCE + " INTEGER");

		private static final String SELECT_ALL = "SELECT * FROM " + TABLE_NAME;

		public boolean insertOrUpdate (int id, int value, boolean force) {
			Cursor res = openDB().rawQuery(SELECT_ALL + " WHERE " + COL_ID + " = ? ", new String[]{ String.valueOf(id) });
			if ( res != null && res.getCount() > 0 ) {
				res.close();
				return update(id, value, force);
			} else {
				return insert(id, value, force);
			}
		}
		
		public boolean insert(int id, int value, boolean force) {
			ContentValues contentValues = new ContentValues();
			contentValues.put(COL_ID, id);
			contentValues.put(COL_VALUE, value);
			contentValues.put(COL_FORCE, force);
			long ret = openDB().insert(TABLE_NAME, null, contentValues);
			closeDB();
			Log.d(TAG, String.format("DMXOut %d insered", id));
			return ret != -1;
		}

		public boolean update(int id, int value, boolean force) {
			ContentValues contentValues = new ContentValues();
			if (value >= 0)
				contentValues.put(COL_VALUE, value);
			contentValues.put(COL_FORCE, force);
			int ret = openDB().update(TABLE_NAME, contentValues,
					COL_ID + " = ?", new String[] { String.valueOf(id) });
			closeDB();
			Log.d(TAG, String.format("DMXOut %d updated: %s: %s, %s: %b", id,COL_VALUE,value,COL_FORCE,force));
			return ret > 0;
		}
		

		public boolean update(int id, int value) {
			ContentValues contentValues = new ContentValues();
			if (value >= 0)
				contentValues.put(COL_VALUE, value);
			int ret = openDB().update(TABLE_NAME, contentValues,
					COL_ID + " = ?", new String[] { String.valueOf(id) });
			closeDB();
			Log.d(TAG, String.format("DMXOut %d updated: %s: %s, %s: %d, %s: %s", id,COL_VALUE,value));
			return ret > 0;
		}

		public boolean force(int id, boolean force) {
			return update(id, -1, force);
		}

		public boolean setValue(int id, int value) {
			return update(id, value);
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
		public DMXChannel getDataFromCursor(Cursor c) {
			DMXChannel channel = new DMXChannel();
			channel.setChannel(c.getInt(NUM_COL_ID));
			channel.setForce(c.getInt(NUM_COL_FORCE) > 0);
			channel.setValue(c.getInt(NUM_COL_VALUE));
			return channel;
		}
		@Override
		public String getColKey() {
			return COL_ID;
		}
	}
	
	public Database(Context context) {
		dbh = DatabaseHelper.getInstance(context);
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

	public LJFunctions lJFunctions() {
		return new LJFunctions();
	}
	
	public DMXOuts dmxOuts() {
		return new DMXOuts();
	}
	
	public static SQLiteDatabase openDB() {
		if (db != null) {
			if (db.inTransaction() || db.isOpen()) {
				return db;
			}
//			db.close();
		}
		return db = dbh.getWritableDatabase();
	}

	public static SQLiteDatabase readDB() {
		if (db != null) {
			if (db.inTransaction() || db.isReadOnly()) {
				return db;
			}
//			db.close();
		}
		return db = dbh.getWritableDatabase();
	}

	public static void closeDB() {
		if (db != null && !db.inTransaction()) {
//			db.close();
		}
	}
	
}
