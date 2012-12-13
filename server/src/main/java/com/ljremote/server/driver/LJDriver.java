package com.ljremote.server.driver;

import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ljremote.json.exceptions.LJNotFoundException;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LRESULT;

public class LJDriver {
	
	private final static String LJWindowClassName = "TLJMainForm";
	private static final Log log = LogFactory.getLog(LJDriver.class);
	
	private final static int WM_USER = 0x0400;
	
	final static class TrigSequence{
		final static int UMSG = WM_USER + 125;
		final static int Forward = 47; // lParam = [0..11]
		final static int Reverse = 48; // lParam = [0..11]
		final static int Random = 49; // lParam = [0..11]
		final static int Bounce = 78; // lParam = [0..11]
	}
	
	final static class LJMain{
		final static int UMSG = WM_USER + 1502;
		final static int Ready = 0; // return 1 == ready
		final static int Version = 1; // return = version
	}
	
	final static class Smoke{
		final static int UMSG = WM_USER + 129;
		final static int On = 29; // lParam = 0
		final static int Off = 30; // lParam = 0
		final static int TimerOff = 30; // lParam = 0
	}
	
	final static class Control{
		final static int UMSG = WM_USER + 135;
		final static int SetBlackOut= 6; // lParam = 0
		final static int Restore= 7; // lParam = 0
		final static int SetFadeOut= 8; // lParam = 0
		final static int SetFadeIn= 9; // lParam = 0
		final static int ToggleBlackOutStatus= 50; // lParam = 0
		final static int ToggleFadeOutStatus= 51; // lParam = 0
	}
	
	final static class CueList{
		final static int UMSG = WM_USER + 1001;
		final static int CLEAR = -1;
		/*
		 * To load cuelist Cuelist# :
		 * 		wParam : Cuelist#
		 * 		lParam :
		 * 			0 => N/A 
		 * 			1 => force cuelist reload
		 */
	}
	final static class Cue{
		final static int UMSG = WM_USER + 1002;
		final static int CLEAR = 0;
		/*
		 * To load cue Cue# :
		 * 		wParam : Cue#
		 * 		lParam :
		 * 			-1 => clear 
		 * 			1 => force cuelist reload
		 */
	}
	final static class Seq{
		final static int UMSG = WM_USER + 1004;
		/*
		 * To flash seq Seq# :
		 * 		wParam : Seq#
		 * 		lParam :
		 * 			-1 => clear flash
		 */
	}
	final class BGCue{
		final static int UMSG = WM_USER + 1005;
		final static int CLEAR = 0;
		/*
		 * To load bgcue BGCue# :
		 * 		wParam : BGCue#
		 */
	}
	
	public final class Statics{
		final static int UMSG = WM_USER + 1011;
		final static int DISABLE = 0; //lParam = Static entry# (0-19)
		final static int ENABLE = 1; //lParam = Static entry# (0-19)
		final static int BitMapped = 2;
		/*
		 * bit 0 <=> static entry1
		 * bit 1 <=> static entry2
		 * ...
		 * bit 19 <=> static entry20
		 * 
		 * Bit=0 : static off
		 * Bit=1 : static on
		 */
		final static int BitMappedAllOff= 0;
		final static int BitMappedAllOn= 0xFFFFF000;
		
		public final static int MAX_ID= 19;
		
		public void enableStatic(int id, boolean enable) throws LJNotFoundException{
			checkLJ();
			if(id <0 || id> MAX_ID){
				throw new IllegalArgumentException(String.format("Static id should belong to [0-19], current= %d", id));
			}
			int wParam= enable ? 1 : 0;
			log.trace(User32Ex.INSTANCE.SendMessageA(LJHandle, UMSG, wParam, id));
		}
	}
	
	final static class ExternalConfiguration{
		final static int UMSG = WM_USER + 1600;
		final static int FixtureSelection = 3;
		final static int RequestHelpFileTopic = 128; // lParam = Topic#
		final static int RequestCurrentSeq = 256; // lParam = ; return = Seq#
		final static int RequestCurrentCue = 257; // lParam = ; return = Cue#
		final static int RequestCurrentCueList = 258; // lParam = ; return = CueList#
		final static int RequestUserPath = 260; // lParam = ; return =
		
		final static int RequestFunctionList = 261; // lParam = Handle ; return = ; result through copyData
		final static int RequestCurrentBGCue = 262; // lParam = Handle ; return = ; result through copyData
		final static int RequestSequencesList = 263; // lParam = Handle ; return = ; result through copyData
		final static int RequestCuesList = 264; // lParam = Handle ; return = ; result through copyData
		final static int RequestCueListsList = 265; // lParam = Handle ; return = ; result through copyData
		final static int RequestBGCuesList = 266; // lParam = Handle ; return = ; result through copyData
		final static int RequestStaticsList = 267; // lParam = ; return = ; result through copyData
		
		final static int RequestSeqInfoNotifications = 300; // lParam = Handle; return =
		final static int RequestCueInfoNotifications = 301; // lParam = Handle; return =
		final static int RequestCueListInfoNotifications = 302; // lParam = Handle; return =
		final static int RequestBGCueInfoNotifications = 303; // lParam = Handle; return =
		final static int RequestStaticsInfoNotifications = 304; // lParam = Handle; return =
		
		final static int DisableSeqInfoNotifications = 310; // lParam = Handle; return =
		final static int DisableCueInfoNotifications = 311; // lParam = Handle; return =
		final static int DisableCueListInfoNotifications = 312; // lParam = Handle; return =
		final static int DisableBGCueInfoNotifications = 313; // lParam = Handle; return =
		final static int DisableStaticsInfoNotifications = 314; // lParam = Handle; return =
		
		final static int WM_COPY_SequencesList = 259;
		final static int WM_COPY_CuesList = 260;
		final static int WM_COPY_CueListsList = 261;
		final static int WM_COPY_BGCuesList = 262;
		final static int WM_COPY_StaticsList = 263;
	}
	
	HWND LJHandle = null;
	
	Statics statics= null;
	
	public boolean findLJ(){
		LJHandle = User32Ex.INSTANCE.FindWindowA(LJWindowClassName, null);
		log.debug(String.format("LJHandle : %s", String.valueOf(LJHandle)));
		return LJHandle != null;
	}
	
	public HWND getLJ(){
		return LJHandle;
	}
	
	public void checkLJ() throws LJNotFoundException{
		if(LJHandle == null){
			throw new LJNotFoundException();
		}
	}
	
	public boolean isLJReady() throws LJNotFoundException{
//		User32Ex.INSTANCE.PostMessage(LJHandle, LJMain.UMSG, new WPARAM(LJMain.Ready), new LPARAM(0));
		checkLJ();
		LRESULT ret = User32Ex.INSTANCE.SendMessageA(LJHandle, LJMain.UMSG, LJMain.Ready,0);
		int ok = ret.intValue();
		log.debug(String.format("LJReady : %d", ok));
		return ok == 1;
	}
	
	public String getLJVersion() throws LJNotFoundException{
		checkLJ();
		LRESULT ret = User32Ex.INSTANCE.SendMessageA(LJHandle, LJMain.UMSG, LJMain.Version,0);
		byte[] buf = ByteBuffer.allocate(4).putInt(ret.intValue()).array();
		String ver= String.format("%d.%d.%d", buf[0],buf[1],buf[2]);
		log.info(String.format("LJVersion : %s", ver));
		return ver;
	}
	
	public Statics statics() {
		return statics == null ? statics= new Statics(): statics;
	}
}
