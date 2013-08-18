package com.ljremote.server.driver;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ljremote.json.exceptions.LJNotFoundException;
import com.ljremote.json.model.BGCue;
import com.ljremote.json.model.Cue;
import com.ljremote.json.model.CueList;
import com.ljremote.json.model.Seq;
import com.ljremote.json.model.Static;
import com.ljremote.server.driver.User32Ex.COPYDATASTRUCT;
import com.ljremote.server.driver.Win32CopyDataMonitor.OnDataReceiver;
import com.sun.jna.Pointer;
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

	final static class Control {
		final static int UMSG = WM_USER + 135;
		final static int SetBlackOut = 6; // lParam = 0
		final static int Restore = 7; // lParam = 0
		final static int SetFadeOut = 8; // lParam = 0
		final static int SetFadeIn = 9; // lParam = 0
		final static int ToggleBlackOutStatus = 50; // lParam = 0
		final static int ToggleFadeOutStatus = 51; // lParam = 0
	}

	public final class CueLists extends CopyDataProcessor<CueList>{
		final static int UMSG = WM_USER + 1001;
		final static int CLEAR = -1;
		final static int CUE_LIST_ITEM_SIZE = 39;
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
			return new CueList(no,name);
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
							log.error("Timeout déclenché");
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

		final static int WM_COPY_SequencesList = 259;
		final static int WM_COPY_CuesList = 260;
		final static int WM_COPY_CueListsList = 261;
		final static int WM_COPY_BGCuesList = 262;
		final static int WM_COPY_StaticsList = 263;
	}

	HWND LJHandle = null;

	private Win32CopyDataMonitor datamonitor = null;
	private Statics statics = null;
	private Sequences seq;
	private Cues cues;
	private BGCues bgCues;
	private CueLists cueLists;

	public LJDriver() {
		datamonitor = new Win32CopyDataMonitor();
		datamonitor.registerOnDataReceiver(this);
		datamonitor.start();
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
	
	public BGCues bgCues(){
		return bgCues == null ? bgCues = new BGCues() : bgCues;
	}
	
	public CueLists cueLists(){
		return cueLists == null ? cueLists = new CueLists() : cueLists;
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
			bgCues.onDataReceived(copy_data);
			break;
		case ExternalConfiguration.WM_COPY_CueListsList:
			cueLists.onDataReceived(copy_data);
		default:
			break;
		}
		copy_data.clear();
	}
}
