package com.ljremote.server.driver;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ljremote.server.driver.User32Ex.COPYDATASTRUCT;
import com.sun.jna.Callback;
import com.sun.jna.LastErrorException;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;

public class Win32CopyDataMonitor {
	
	private static final Log log = LogFactory.getLog(Win32CopyDataMonitor.class);

	public static final int RETRY = 3;
	public static final String WINDOW_NAME = "CDM receiver";
	private HWND viewer = null;
	private ExecutorService executor;
	private List<OnDataReceiver> listeners;
	private ReceverWindow window;
	
	public interface OnDataReceiver {
		public void onDataReceived(COPYDATASTRUCT copy_data);
	}
	
	public Win32CopyDataMonitor() {
		executor = Executors.newSingleThreadExecutor();
		listeners = new LinkedList<Win32CopyDataMonitor.OnDataReceiver>();
	}
	
	public void start(){
		if ( !isStarted() && executor != null ) {
			window = new ReceverWindow();
			executor.submit(window);
			log.info("Start monitoring window");
		}
	}
	
	public void stop(){
		if ( !isStarted() ) {
			return;
		}
		executor.shutdownNow();
		viewer = null;
	}
	
	public void registerOnDataReceiver(OnDataReceiver listener){
		listeners.add(listener);
	}
	
	public void fireOnDatareceived(COPYDATASTRUCT copy_data){
		log.debug("works");
		System.err.println("works");
		for(OnDataReceiver listener : listeners) {
			listener.onDataReceived(copy_data);
		}
	}
	
	private class ReceverWindow implements Callback, StdCallCallback, Runnable {

		private final HANDLE event = Kernel32.INSTANCE.CreateEvent(null, false,
				false, null);
		private JFrame wcmFrame;

		public void run() {
			log.info("Run ReceverWindow");
			createWindow();
			for ( int nb_try = 0; viewer == null && nb_try < RETRY; nb_try++) {
				viewer = User32Ex.INSTANCE.FindWindowA("SunAwtFrame", WINDOW_NAME);
			}
			wcmFrame.setVisible(false);
			log.debug("View : " + String.valueOf(viewer));
			NativeLong ret = null;
			try {
				ret = User32Ex.INSTANCE.SetWindowLongA(viewer, new NativeLong(User32Ex.GWLP_WNDPROC), this);
			} catch (LastErrorException e) {
				log.error("Windows error code : ",e);
			} catch (Exception e) {
				log.error("???????? error code : ",e);
			}
			
			log.debug(ret);
			log.debug(User32Ex.INSTANCE.GetWindowLongA(viewer, User32Ex.GWLP_WNDPROC));
			log.debug(this);
			
			MSG msg = new MSG();
			final HANDLE handles[] = { event };
			while (true) {
				int result = User32Ex.INSTANCE.MsgWaitForMultipleObjects(
						handles.length, handles, false, Kernel32.INFINITE,
						User32Ex.QS_ALLINPUT);
				log.error("res : " + result);
				if(result == Kernel32.WAIT_OBJECT_0){
					return;
				}
				
				if(result != Kernel32.WAIT_OBJECT_0 + handles.length){
					// Serious problem!
					break;
				}
				
				while (User32Ex.INSTANCE.PeekMessage(msg, null, 0, 0, User32Ex.PM_REMOVE)) {
					User32Ex.INSTANCE.TranslateMessage(msg);
					User32Ex.INSTANCE.DispatchMessage(msg);
				}
			}
		}
	
		private void createWindow(){
			wcmFrame = new JFrame(WINDOW_NAME);
			wcmFrame.setVisible(true);
		}
		
		@SuppressWarnings("unused")
		public LRESULT callback(final HWND hWnd, final NativeLong uMsg, final NativeLong wParam, final Pointer lParam){
			if(uMsg.intValue() == User32Ex.WM_COPYDATA){
				log.debug("Message from " + String.valueOf(hWnd) + " : " + uMsg + ", wnd : " + Integer.toHexString(wParam.intValue()) + ", lParam : " + lParam);
				COPYDATASTRUCT copy_data = new COPYDATASTRUCT(lParam);
				copy_data.read();
				System.out.println(copy_data);
				fireOnDatareceived(copy_data);
			}
			return User32Ex.INSTANCE.DefWindowProcA(hWnd, uMsg, wParam, lParam);
		}
		
	}

	public WinDef.HWND getViewer() {
		return viewer;
	}
	
	public boolean isStarted(){
		return getViewer() != null;
	}
}
