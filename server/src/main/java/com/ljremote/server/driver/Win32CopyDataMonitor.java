package com.ljremote.server.driver;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;

public class Win32CopyDataMonitor implements StdCallCallback, Runnable {

	private HWND viewer = null;
	private final HANDLE event = Kernel32.INSTANCE.CreateEvent(null, false,
			false, null);

	public void run() {
		viewer = User32Ex.INSTANCE.CreateWindowEx(0, "STATIC", "", 0, 0, 0, 0,
				0, null, 0, 0, null);

		MSG msg = new MSG();

		final HANDLE handles[] = { event };
		while (true) {
			int result = User32Ex.INSTANCE.MsgWaitForMultipleObjects(
					handles.length, handles, false, Kernel32.INFINITE,
					User32Ex.QS_ALLINPUT);
			
			if(result == Kernel32.WAIT_OBJECT_0){
				User32Ex.INSTANCE.DestroyWindow(viewer);
//				viewer= null;
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

	public WinDef.HWND getViewer() {
		return viewer;
	}
	
	public LRESULT callback(HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam){
		if(uMsg == User32Ex.WM_COPYDATA){
			System.out.println(lParam);
		}
		return User32Ex.INSTANCE.DefWindowProc(hWnd, uMsg, wParam, lParam);
	}

}
