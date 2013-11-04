package com.ljremote.android.data;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.ljremote.android.json.LJClientService;
import com.ljremote.android.json.LJClientService.MODE;


public class DataManager {

	private final static String TAG = "DataManager";
	private Database db;
	private LJClientService ljService;
	private Context context;
	private List<OnDatabaseUpdateListener> db_listeners;
	private StaticManager staticManager ;
	private CueManager cueManager;
	private BGCueManager bgCueManager;
	private CueListManager cueListManager;
	private SequenceManager seqManager;
	private LJFunctionManager lJFunctionManager;
	private DMXOutManager dmxOutManager;
	private MasterIntManager masterIntManager;
	
	public interface OnDatabaseUpdateListener {
		public void onTableUpdateListener(Context context, TABLES table);
	}
	
	public void registerDatabaseUpdateListener(OnDatabaseUpdateListener listener) {
		db_listeners.add(listener);
	}
	
	public void fireDatabaseUpdate(TABLES table) {
		for(OnDatabaseUpdateListener listener : db_listeners){
			listener.onTableUpdateListener(this.context,table);
		}
	}
	
	public enum TABLES{
		STATICS,SEQUENCES,CUES, CUELISTS, BGCUES, LJFUNCTIONS, DMXOUT, MASTER_INT
	}
	
	public DataManager(Context context) {
		db= new Database(context);
		this.context = context;
		ljService= null;
		db_listeners = new LinkedList<DataManager.OnDatabaseUpdateListener>();
	}
	
	public void bindService ( LJClientService service ) {
		ljService = service;
		Log.i(TAG, "Service bound for data");
	}

	public boolean checkService() {
		return ljService != null && ljService.getCurrentMode() == MODE.DRIVE;
	}
	
	public StaticManager getStaticManager() {
		return staticManager = staticManager == null ? staticManager = new StaticManager(this) : staticManager;
	}

	public SequenceManager getSequenceManager() {
		return seqManager = seqManager == null ? seqManager = new SequenceManager(this) : seqManager;
	}
	
	public CueManager getCueManager() {
		return cueManager = cueManager == null ? cueManager = new CueManager(this) : cueManager;
	}

	public BGCueManager getBGCueManager() {
		return bgCueManager = bgCueManager == null ? bgCueManager = new BGCueManager(this) : bgCueManager;
	}
	
	public CueListManager getCueListManager() {
		return cueListManager = cueListManager == null ? cueListManager = new CueListManager(this) : cueListManager;
	}

	public LJFunctionManager getLJFunctionManager() {
		return lJFunctionManager = lJFunctionManager == null ? lJFunctionManager = new LJFunctionManager(this) : lJFunctionManager;
	}

	public DMXOutManager getDMXOutManager() {
		return dmxOutManager = dmxOutManager == null ? dmxOutManager = new DMXOutManager(this) : dmxOutManager;
	}

	public MasterIntManager getMasterIntManager() {
		return masterIntManager = masterIntManager == null ? masterIntManager = new MasterIntManager(this) : masterIntManager;
	}
	
	public LJClientService getService() {
		return ljService;
	}
	
	public Database getDB(){
		return db;
	}

	public Context getContext() {
		return context;
	}
	
}
