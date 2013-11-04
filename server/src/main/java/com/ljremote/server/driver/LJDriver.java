package com.ljremote.server.driver;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.model.BGCue;
import com.ljremote.json.model.Cue;
import com.ljremote.json.model.CueList;
import com.ljremote.json.model.LJFunction;
import com.ljremote.json.model.Seq;
import com.ljremote.json.model.Static;
import com.ljremote.server.driver.LJDriver.MyUserLib.ExternalDMXOverride;
import com.ljremote.server.driver.LJDriver.MyUserLib.MasterIntensitySettings;
import com.ljremote.server.driver.LJDriver.MyUserLib.StaticItem;
import com.ljremote.server.driver.User32Ex.COPYDATASTRUCT;
import com.ljremote.server.driver.Win32CopyDataMonitor.OnDataReceiver;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTR;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LRESULT;

public class LJDriver implements OnDataReceiver {

	private static final Log log = LogFactory.getLog(LJDriver.class);
	private final static String LJWindowClassName = "TLJMainForm";

	private final static int WM_USER = 0x0400;
	public static final long MONITOR_TIMEOUT = 4000;

	final static class TrigSequence {
		final static int UMSG = WM_USER + 125;
		final static int Forward = 47; // lParam = [0..11]
		final static int Reverse = 48; // lParam = [0..11]
		final static int Random = 49; // lParam = [0..11]
		final static int Bounce = 78; // lParam = [0..11]

	}

	final static class LJMain {
		final static int UMSG = WM_USER + 1502;
		final static int Ready = 0; // return 1 == ready
		final static int Version = 1; // return = version
	}

	final static class Smoke {
		final static int UMSG = WM_USER + 129;
		final static int On = 29; // lParam = 0
		final static int Off = 30; // lParam = 0
		final static int TimerOff = 30; // lParam = 0
	}

	public final class Control {
		final static int UMSG = WM_USER + 135;
		final static int SetBlackOut = 6; // lParam = 0
		final static int Restore = 7; // lParam = 0
		final static int SetFadeOut = 8; // lParam = 0
		final static int SetFadeIn = 9; // lParam = 0
		final static int ToggleBlackOutStatus = 50; // lParam = 0
		final static int ToggleFadeOutStatus = 51; // lParam = 0

		public boolean setBlackOut() {
			checkLJ();
			log.trace("setBlackOut "
					+ User32Ex.INSTANCE.SendMessageA(LJHandle, UMSG,
							SetBlackOut, 0).intValue());
			return true;
		}

		public boolean restore() {
			checkLJ();
			log.trace("restore "
					+ User32Ex.INSTANCE
							.SendMessageA(LJHandle, UMSG, Restore, 0)
							.intValue());
			return true;
		}
	}

	public final class CueLists extends CopyDataProcessor<CueList> {
		final static int UMSG = WM_USER + 1001;
		final static int CLEAR = -1;
		final static int CUE_LIST_ITEM_SIZE = 39;

		final static int CUELIST_CONTROL_UMSG = WM_USER + 137;
		final static int CUELIST_CONTROL_GO = 32;
		final static int CUELIST_CONTROL_BACK = 33;

		/*
		 * To load cuelist Cuelist# : wParam : Cuelist# lParam : 0 => N/A 1 =>
		 * force cuelist reload
		 */

		public List<CueList> getCueListsList() {
			log.debug("Driver:getCuesList");
			return getDataList(ExternalConfiguration.RequestCueListsList);
		}

		@Override
		public int getDataSize() {
			return CUE_LIST_ITEM_SIZE;
		}

		@Override
		public CueList readData(Pointer p, int offset) {
			int no = p.getInt(offset);
			int flags = p.getInt(offset + 4);
			String name = p.getString(offset + 9);
			log.trace("CueList -> No : " + no + ", Flags : "
					+ Integer.toHexString(flags) + ", Name : " + name);
			return new CueList(no, name);
		}

		public int getCurrentCueList() {
			checkLJ();
			return User32Ex.INSTANCE.SendMessageA(LJHandle,
					ExternalConfiguration.UMSG,
					ExternalConfiguration.RequestCurrentCueList, 1).intValue();
		}

		public boolean loadCueList(int id) {
			checkLJ();
			log.trace(User32Ex.INSTANCE.SendMessageA(LJHandle, UMSG, id, 1));
			return true;
		}

		public boolean cueListGo() {
			checkLJ();
			log.trace(User32Ex.INSTANCE.SendMessageA(LJHandle,
					CUELIST_CONTROL_UMSG, CUELIST_CONTROL_GO, 1));
			return true;
		}

		public boolean cueListBack() {
			checkLJ();
			log.trace(User32Ex.INSTANCE.SendMessageA(LJHandle,
					CUELIST_CONTROL_UMSG, CUELIST_CONTROL_BACK, 1));
			return true;
		}
	}

	public final class Cues extends CopyDataProcessor<Cue> {
		final static int UMSG = WM_USER + 1002;
		final static int CLEAR = 0;
		final static int CUE_ITEM_SIZE = 39;

		/*
		 * To load cue Cue# : wParam : Cue# lParam : -1 => clear 1 => force
		 * cuelist reload
		 */
		public List<Cue> getCuesList() {
			log.debug("Driver:getCuesList");
			return getDataList(ExternalConfiguration.RequestCuesList);
		}

		@Override
		public int getDataSize() {
			return CUE_ITEM_SIZE;
		}

		@Override
		public Cue readData(Pointer p, int offset) {
			int no = p.getInt(offset);
			int flags = p.getInt(offset + 4);
			String name = p.getString(offset + 9);
			log.trace("Cue -> No : " + no + ", Flags : "
					+ Integer.toHexString(flags) + ", Name : " + name);
			return new Cue(no, name);
		}

		public boolean loadCue(int id) {
			checkLJ();
			log.trace(User32Ex.INSTANCE.SendMessageA(LJHandle, UMSG, id, 1));
			return true;
		}

		public int getCurrentCue() {
			checkLJ();
			return User32Ex.INSTANCE.SendMessageA(LJHandle,
					ExternalConfiguration.UMSG,
					ExternalConfiguration.RequestCurrentCue, 1).intValue();
		}
	}

	public final class Sequences extends CopyDataProcessor<Seq> {
		final static int UMSG = WM_USER + 1004;
		final static int SEQ_ITEM_SIZE = 39;

		/*
		 * To flash seq Seq# : wParam : Seq# lParam : -1 => clear flash
		 */

		public List<Seq> getSequencesList() {
			return getDataList(ExternalConfiguration.RequestSequencesList);
		}

		@Override
		public int getDataSize() {
			return SEQ_ITEM_SIZE;
		}

		@Override
		public Seq readData(Pointer p, int offset) {
			int no = p.getInt(offset);
			int flags = p.getInt(offset + 4);
			String name = p.getString(offset + 9);
			log.trace("Seq -> No : " + no + ", Flags : "
					+ Integer.toHexString(flags) + ", Name : " + name);
			return new Seq(no, name);
		}

		public int getCurrentSeq() {
			checkLJ();
			return User32Ex.INSTANCE.SendMessageA(LJHandle,
					ExternalConfiguration.UMSG,
					ExternalConfiguration.RequestCurrentSeq, 1).intValue();
		}
	}

	public final class BGCues extends CopyDataProcessor<BGCue> {
		final static int UMSG = WM_USER + 1005;
		final static int CLEAR = 0;
		final static int BGCUE_ITEM_SIZE = 39;

		/*
		 * To load bgcue BGCue# : wParam : BGCue#
		 */

		public List<BGCue> getBGCueList() {
			return getDataList(ExternalConfiguration.RequestBGCuesList);
		}

		@Override
		public int getDataSize() {
			return BGCUE_ITEM_SIZE;
		}

		@Override
		public BGCue readData(Pointer p, int offset) {
			int no = p.getInt(offset);
			int flags = p.getInt(offset + 4);
			String name = p.getString(offset + 9);
			log.trace("BGCue -> No : " + no + ", Flags : "
					+ Integer.toHexString(flags) + ", Name : " + name);
			return new BGCue(no, name);
		}
	}

	public final class Statics extends CopyDataProcessor<Static> {
		final static int UMSG = WM_USER + 1011;
		final static int DISABLE = 0; // lParam = Static entry# (0-19)
		final static int ENABLE = 1; // lParam = Static entry# (0-19)
		final static int BitMapped = 2;
		/*
		 * bit 0 <=> static entry1 bit 1 <=> static entry2 ... bit 19 <=> static
		 * entry20
		 * 
		 * Bit=0 : static off Bit=1 : static on
		 */
		final static int BitMappedAllOff = 0;
		final static int BitMappedAllOn = 0xFFFFF000;

		public final static int MAX_ID = 19;
		final static int STATIC_ITEM_SIZE = 39;

		public void enableStatic(int id, boolean enable)
				throws LJNotFoundException {
			checkLJ();
			if (id < 0 || id > MAX_ID) {
				throw new IllegalArgumentException(String.format(
						"Static id should belong to [0-19], current= %d", id));
			}
			int wParam = enable ? 1 : 0;
			log.trace(User32Ex.INSTANCE
					.SendMessageA(LJHandle, UMSG, wParam, id));
		}

		public List<Static> getStaticsList() {
			checkLJ();
			log.trace("getStaticsList");
			return getDataList(ExternalConfiguration.RequestStaticsList);
		}

		public Static readData(Pointer p, int offset) {
			int no = p.getInt(offset);
			int flags = p.getInt(offset + 4);
			String name = "";
			if (flags > 0) {
				name = p.getString(offset + 9);
			}
			log.trace("Static -> No : " + no + ", Flags : "
					+ Integer.toHexString(flags) + ", Name : " + name);
			return new Static(no, name);
		}

		@Override
		public int getDataSize() {
			return STATIC_ITEM_SIZE;
		}
	}

	public final class LJFunctions extends CopyDataProcessor<LJFunction> {
		final static int UMSG = WM_USER + 1006;

		public boolean executeFunction(int id) {
			checkLJ();
			log.trace("Executing LJ function " + id);
			return User32Ex.INSTANCE.SendMessageA(LJHandle, UMSG, id, 0)
					.intValue() == 0;
		}

		@Override
		public int getDataSize() {
			return 40;
		}

		public List<LJFunction> getLJFunctionsList() {
			checkLJ();
			return getDataList(ExternalConfiguration.RequestFunctionList);
		}

		@Override
		public LJFunction readData(Pointer p, int offset) {
			int no = p.getInt(offset);
			String name = p.getString(offset + 5);
			LJFunction func = new LJFunction(no, name);
			log.trace(func);
			return func;
		}

	}

	private abstract class CopyDataProcessor<T> {
		protected final Object monitor = new Object();
		protected List<T> retreiveList;

		public synchronized List<T> getDataList(int requestId) {
			log.debug("getDataList : " + this.getClass().getName());
			checkLJ();
			if (datamonitor.isStarted()) {
				synchronized (monitor) {
					retreiveList = new ArrayList<T>();
					LRESULT ret = User32Ex.INSTANCE.SendMessageA(LJHandle,
							ExternalConfiguration.UMSG, requestId, datamonitor
									.getViewer().getPointer());
					if (ret.intValue() == 0) {
						log.debug("Request sent: id=" + requestId);
						try {
							monitor.wait(MONITOR_TIMEOUT);
						} catch (InterruptedException e) {
							log.error("Timeout d�clench�");
							e.printStackTrace();
						}
					}
				}
			}
			return retreiveList;
		}

		public void onDataReceived(COPYDATASTRUCT copy_data) {
			int data_size = copy_data.cbData.intValue();
			log.debug("CopyDataProcessor.onDataReceived, size=" + data_size
					+ "B");
			List<T> list = new ArrayList<T>();
			if (data_size > 0) {
				int tab_size = data_size / getDataSize();
				System.out.println(tab_size);
				Pointer p = copy_data.lpData;
				for (int pos = 0; pos < tab_size; pos++) {
					list.add(readData(p, getDataSize() * pos));
				}
			}
			fecthData(list);
		}

		public abstract int getDataSize();

		public void fecthData(final List<T> newData) {
			new Thread(new Runnable() {

				public void run() {
					synchronized (monitor) {
						setDataList(newData);
						monitor.notify();
					}
				}
			}).start();
		}

		public abstract T readData(Pointer p, int offset);

		public void setDataList(List<T> newData) {
			retreiveList = newData;
		}
	}

	final static class ExternalConfiguration {
		final static int UMSG = WM_USER + 1600;
		final static int FixtureSelection = 3;
		final static int RequestHelpFileTopic = 128; // lParam = Topic#
		final static int RequestCurrentSeq = 256; // lParam = ; return = Seq#
		final static int RequestCurrentCue = 257; // lParam = ; return = Cue#
		final static int RequestCurrentCueList = 258; // lParam = ; return =
														// CueList#
		final static int RequestUserPath = 260; // lParam = ; return =

		final static int RequestFunctionList = 261; // lParam = Handle ; return
													// = ; result through
													// copyData
		final static int RequestCurrentBGCue = 262; // lParam = Handle ; return
													// = ; result through
													// copyData
		final static int RequestSequencesList = 263; // lParam = Handle ; return
														// = ; result through
														// copyData
		final static int RequestCuesList = 264; // lParam = Handle ; return = ;
												// result through copyData
		final static int RequestCueListsList = 265; // lParam = Handle ; return
													// = ; result through
													// copyData
		final static int RequestBGCuesList = 266; // lParam = Handle ; return =
													// ; result through copyData
		final static int RequestStaticsList = 267; // lParam = ; return = ;
													// result through copyData

		final static int RequestSeqInfoNotifications = 300; // lParam = Handle;
															// return =
		final static int RequestCueInfoNotifications = 301; // lParam = Handle;
															// return =
		final static int RequestCueListInfoNotifications = 302; // lParam =
																// Handle;
																// return =
		final static int RequestBGCueInfoNotifications = 303; // lParam =
																// Handle;
																// return =
		final static int RequestStaticsInfoNotifications = 304; // lParam =
																// Handle;
																// return =

		final static int DisableSeqInfoNotifications = 310; // lParam = Handle;
															// return =
		final static int DisableCueInfoNotifications = 311; // lParam = Handle;
															// return =
		final static int DisableCueListInfoNotifications = 312; // lParam =
																// Handle;
																// return =
		final static int DisableBGCueInfoNotifications = 313; // lParam =
																// Handle;
																// return =
		final static int DisableStaticsInfoNotifications = 314; // lParam =
																// Handle;
																// return =

		final static int WM_COPY_FunctionsList = 257;
		final static int WM_COPY_SequencesList = 259;
		final static int WM_COPY_CuesList = 260;
		final static int WM_COPY_CueListsList = 261;
		final static int WM_COPY_BGCuesList = 262;
		final static int WM_COPY_StaticsList = 263;
	}

	public interface MyUserLib extends User32Ex {

		class ExternalDMXOverride extends Structure {
			static class ByReference extends ExternalDMXOverride implements
					Structure.ByReference {

				public ByReference() {
					super();
				}
			}

			public int[] Reserved = new int[16];
			public byte[] ChFlags = new byte[DMXOutOveride.NB_CHANEL];
			public byte[] Values = new byte[DMXOutOveride.NB_CHANEL];

			public ExternalDMXOverride() {
				super(ALIGN_NONE);
			}

			@Override
			protected List<String> getFieldOrder() {
				return Arrays.asList(new String[] { "Reserved", "ChFlags",
						"Values" });
			}

		}

		class MasterIntensitySettings extends Structure {
			static class ByReference extends MasterIntensitySettings implements
					Structure.ByReference {

				public ByReference() {
					super();
				}
			}

			public MasterIntensitySettings() {
				super();
				this.setAlignType(ALIGN_NONE);
			}

			public byte[] Flags = new byte[MasterIntensity.COUNT];
			public byte[] Values = new byte[MasterIntensity.COUNT];

			@Override
			protected List<String> getFieldOrder() {
				return Arrays.asList(new String[] { "Flags", "Values" });
			}
			
			@Override
			public String toString() {
				return String.format("MasterIntensity:\n\tFlags: %s\n\tValues: %s",
						Arrays.toString(Flags),
						Arrays.toString(Values)
						);
			}
		}

		class StaticItem extends Structure {
			static class ByReference extends MasterIntensitySettings implements
					Structure.ByReference {

				public ByReference() {
					super();
				}
			}

			public int No;
			public int Flags;
			public PascalString.ByValue Name = new PascalString.ByValue(30);

			public StaticItem() {
				super();
				this.setAlignType(ALIGN_NONE);
			}
			
			@Override
			protected List<String> getFieldOrder() {
				return Arrays.asList(new String[] { "No", "Flags", "Name" });
			}
			
			public void setName(String name){
				this.Name.setString(name);
			}
		}
		
		public class PascalString extends Structure {
			
			static final int DEFAULT_SIZE = 32;
			
			public static class ByReference extends PascalString implements
				Structure.ByReference {

				public ByReference() {
					super();
				}
				
				public ByReference(int size) {
					super(size);
				}
			}
			public static class ByValue extends PascalString implements Structure.ByValue {
				
				public ByValue() {
					super();
				}
				
				public ByValue(int size) {
					super(size);
				}
			}
			
			public byte length;
			public char[] str = new char[DEFAULT_SIZE];
			
			public PascalString() {
				super(ALIGN_NONE);
			}
			
			public PascalString(int size) {
				this();
				str = new char[size];
			}
			
			public void setString(String str) {
				this.length = (byte) str.length();
				this.str    = str.toCharArray();
			}

			@Override
			public String toString() {
				return new String(str);
			}
			
			@Override
			protected List<String> getFieldOrder() {
				return Arrays.asList(new String[] { "length", "str" });
			}
			
		}
		
	}

	public final class DMXOutOveride implements Runnable {
		final static int NB_CHANEL = 2048;
		final static byte OVERRIDE = 1;
		int _WMCOPY_DMXOverride = 267;
		COPYDATASTRUCT copy_data = null;
		ExternalDMXOverride data = null;
		ScheduledExecutorService sender;

		public int override(Map<Integer, Integer> channels) {
			log.debug("Overriding Datas : " + channels);
			if (channels.size() == 0) {
				copy_data = null;
			} else {
				data = new ExternalDMXOverride();
				for( int i =0; i < NB_CHANEL ; i++ ) {
					data.ChFlags[i] = 0;
					data.Values[i] = 0;
				}
				for (int can : channels.keySet()) {
					data.ChFlags[can] = OVERRIDE;
					data.Values[can] = channels.get(can).byteValue();
				}
				data.write();
				copy_data = new COPYDATASTRUCT();
				copy_data.dwData = new ULONG_PTR(_WMCOPY_DMXOverride);
				copy_data.cbData = new DWORD(data.size());
				copy_data.lpData = data.getPointer();
				copy_data.write();
			}

			// return 0;
			return startStopSendingData();
		}

		private int startStopSendingData() {
			if (copy_data == null) {
				stopSendingData();
			} else {
				startSendingData();
			}
			return 0;
		}

		public void startSendingData() {
			if (sender != null && !sender.isShutdown()) {
				return;
			}
			sender = Executors.newSingleThreadScheduledExecutor();
			sender.scheduleAtFixedRate(this, 50, 50, TimeUnit.MILLISECONDS);
			log.info("Start sending DMXOUT data");
		}

		public void stopSendingData() {
			if (sender == null || sender.isShutdown()) {
				return;
			}
			sender.shutdownNow();
			try {
				sender.awaitTermination(150, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				log.error("Error while stopping DMXOUT data sender", e);
			}
			log.info("Sending DMXOUT data stopped");
		}

		public void run() {
			if (copy_data == null) {
				return;
			}
			boolean ret = MyUserLib.INSTANCE.SendMessageA(LJHandle,
					MyUserLib.WM_COPYDATA, datamonitor.getViewer(),
					copy_data.getPointer());
			System.out.println(ret);
		}
	}

	public final class MasterIntensity {
		public static final int COUNT = 9;
		final static byte OVERRIDE = 1;
		int _WMCOPY_SetIntensityMasters = 266;
		COPYDATASTRUCT copy_data = null;
		MasterIntensitySettings settings = null;

		public boolean setIntensity(int pos, int intensity) {
			if (settings == null) {
				settings = new MasterIntensitySettings();
			}
			;
			if (copy_data == null) {
				copy_data = new COPYDATASTRUCT();
				copy_data.dwData = new ULONG_PTR(_WMCOPY_SetIntensityMasters);
				copy_data.cbData = new DWORD(settings.size());
				copy_data.lpData = settings.getPointer();
			}

			settings.Flags[pos] = OVERRIDE;
			settings.Values[pos] = (byte) intensity;
			
			log.debug("Settings Master Intensity : " + settings);

			settings.write();
			copy_data.write();

			boolean ret = MyUserLib.INSTANCE.SendMessageA(LJHandle,
					MyUserLib.WM_COPYDATA, datamonitor.getViewer(),
					copy_data.getPointer());
			System.out.println(ret);
			return ret;
		}
	}

	HWND LJHandle = null;

	private Win32CopyDataMonitor datamonitor = null;
	private Statics statics = null;
	private Sequences seq;
	private Cues cues;
	private BGCues bgCues;
	private CueLists cueLists;
	private Control control;
	private LJFunctions ljFunctions;
	private DMXOutOveride dmxOutOverride;
	private MasterIntensity masterInt;

	public LJDriver() {
		datamonitor = new Win32CopyDataMonitor();
		datamonitor.registerOnDataReceiver(this);
		datamonitor.start();

		if (Platform.is64Bit()) {
			log.info("Plateform is 64bit");
		} else {
			log.info("Plateform is 32bit");
		}
		
//		StaticItem item = new StaticItem();
//		StaticItem[] items = (StaticItem[]) item.toArray(4);
//		for(int i=0;i<4;i++){
//			items[i].No = i;
//			items[i].Flags = 10 + i;
//			items[i].setName("Name_" + i);
//			items[i].write();
//		}
//		System.out.println(items);
	}

	public boolean findLJ() {
		LJHandle = User32Ex.INSTANCE.FindWindowA(LJWindowClassName, null);
		log.debug(String.format("LJHandle : %s",
				String.valueOf(LJHandle == null ? null : LJHandle.getPointer())));
		return LJHandle != null;
	}

	public HWND getLJ() {
		return LJHandle;
	}

	public void checkLJ() throws LJNotFoundException {
		if (LJHandle == null) {
			throw new LJNotFoundException();
		}
	}

	public boolean isLJReady() throws LJNotFoundException {
		checkLJ();
		LRESULT ret = User32Ex.INSTANCE.SendMessageA(LJHandle, LJMain.UMSG,
				LJMain.Ready, 0);
		int ok = ret.intValue();
		log.debug(String.format("LJReady : %d", ok));
		return ok == 1;
	}

	public String getLJVersion() throws LJNotFoundException {
		checkLJ();
		LRESULT ret = User32Ex.INSTANCE.SendMessageA(LJHandle, LJMain.UMSG,
				LJMain.Version, 0);
		byte[] buf = ByteBuffer.allocate(4).putInt(ret.intValue()).array();
		String ver = String.format("%d.%d.%d", buf[0], buf[1], buf[2]);
		log.info(String.format("LJVersion : %s", ver));
		return ver;
	}

	public Statics statics() {
		return statics == null ? statics = new Statics() : statics;
	}

	public Sequences sequences() {
		return seq == null ? seq = new Sequences() : seq;
	}

	public Cues cues() {
		return cues == null ? cues = new Cues() : cues;
	}

	public BGCues bgCues() {
		return bgCues == null ? bgCues = new BGCues() : bgCues;
	}

	public CueLists cueLists() {
		return cueLists == null ? cueLists = new CueLists() : cueLists;
	}

	public Control control() {
		return control == null ? control = new Control() : control;
	}

	public LJFunctions ljFunctions() {
		return ljFunctions == null ? ljFunctions = new LJFunctions()
				: ljFunctions;
	}

	public DMXOutOveride dmxOutOverride() {
		return dmxOutOverride == null ? dmxOutOverride = new DMXOutOveride()
				: dmxOutOverride;
	}
	
	public MasterIntensity masterInt() {
		return masterInt == null ? masterInt = new MasterIntensity()
		: masterInt;
	}

	public void onDataReceived(COPYDATASTRUCT copy_data) {
		log.debug("Dest : " + copy_data.dwData.intValue());
		switch (copy_data.dwData.intValue()) {
		case ExternalConfiguration.WM_COPY_SequencesList:
			sequences().onDataReceived(copy_data);
			break;
		case ExternalConfiguration.WM_COPY_StaticsList:
			statics().onDataReceived(copy_data);
			break;
		case ExternalConfiguration.WM_COPY_CuesList:
			cues().onDataReceived(copy_data);
			break;
		case ExternalConfiguration.WM_COPY_BGCuesList:
			bgCues().onDataReceived(copy_data);
			break;
		case ExternalConfiguration.WM_COPY_CueListsList:
			cueLists().onDataReceived(copy_data);
		case ExternalConfiguration.WM_COPY_FunctionsList:
			ljFunctions().onDataReceived(copy_data);
			break;
		default:
			break;
		}
		copy_data.clear();
	}

	public int sendMessageToLj(int uMsg, int wParam, int lParam) {
		checkLJ();
		return User32Ex.INSTANCE.SendMessageA(LJHandle, uMsg, wParam, lParam)
				.intValue();
	}

	public void stopActions() {
		dmxOutOverride().stopSendingData();
	}
}
