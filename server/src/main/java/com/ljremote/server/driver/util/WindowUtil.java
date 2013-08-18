package com.ljremote.server.driver.util;

import com.ljremote.server.driver.util.WindowUtil.User32.WNDEUMPROC;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

public class WindowUtil {

	public interface User32 extends StdCallLibrary {
		User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);
		
		interface WNDEUMPROC extends StdCallCallback {
			boolean callback(Pointer hWnd, Pointer arg);
		}
		
		boolean EnumWindows(WNDEUMPROC lpEnumFunc, Pointer arg);
		
		int GetWindowTextA(Pointer hWnd, byte[] lpString, int nMaxCount);
		int GetClassNameA(Pointer hWnd, byte[] lpClassName, int nMaxCount);
	}
	
	public static void enumAllWindows() {
		User32.INSTANCE.EnumWindows(new WNDEUMPROC() {
			
			int count;
			
			public boolean callback(Pointer hWnd, Pointer arg) {
				byte[] windowText = new byte[512];
				User32.INSTANCE.GetWindowTextA(hWnd, windowText, 512);
				String wText = Native.toString(windowText);
				byte[] windowClass = new byte[512];
				User32.INSTANCE.GetClassNameA(hWnd, windowClass, 512);
				String wClass = Native.toString(windowClass);
				
				wText = (wText.isEmpty()) ? "" : "; text: " + wText;
                System.out.println(
                		String.format("Found window %s num %d, [%s] : %s",
                		String.valueOf(hWnd),++count,wClass,wText
                		));
				return true;
			}
		}, null);
	}
}
