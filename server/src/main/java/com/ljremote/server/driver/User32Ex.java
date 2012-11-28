package com.ljremote.server.driver;

import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTR;
import com.sun.jna.platform.win32.WinNT.HANDLE;

public interface User32Ex extends com.sun.jna.platform.win32.User32 {
	User32Ex INSTANCE = (User32Ex) Native.loadLibrary("user32", User32Ex.class);

	final int QS_KEY = 0x0001;
	final int QS_MOUSEMOVE = 0x0002;
	final int QS_MOUSEBUTTON = 0x0004;
	final int QS_POSTMESSAGE = 0x0008;
	final int QS_TIMER = 0x0010;
	final int QS_PAINT = 0x0020;
	final int QS_SENDMESSAGE = 0x0040;
	final int QS_HOTKEY = 0x0080;
	final int QS_ALLPOSTMESSAGE = 0x0100;
	final int QS_RAWINPUT = 0x0400;

	final int QS_MOUSE = (QS_MOUSEMOVE | QS_MOUSEBUTTON);

	final int QS_INPUT = (QS_MOUSE | QS_KEY | QS_RAWINPUT);

	final int QS_ALLEVENTS = (QS_INPUT | QS_POSTMESSAGE | QS_TIMER | QS_PAINT | QS_HOTKEY);

	final int QS_ALLINPUT = (QS_INPUT | QS_POSTMESSAGE | QS_TIMER | QS_PAINT
			| QS_HOTKEY | QS_SENDMESSAGE);

	HWND CreateWindowEx(int styleEx, String classname, String windowName,
			int style, int x, int y, int width, int height, HWND hndParent,
			int hndMenu, int hndInst, Object parm);

	int DestroyWindow(HWND hwnd);

	int MsgWaitForMultipleObjects(int nCount, HANDLE[] pHandles,
			boolean bWaitAll, int dwMilliseconds, int dwWakeMask);

	/*
	 * PeekMessage() Options
	 */
	final int PM_NOREMOVE = 0x0000;
	final int PM_REMOVE = 0x0001;
	final int PM_NOYIELD = 0x0002;
	
	int DefWindowProc(HWND hWnd, int msg, WPARAM wParam, LPARAM lParam);

	/*
	 * CopyDat
	 */
	final int WM_COPYDATA = 0x004a;
	class COPYDATASTRUCT extends Structure {
		static class ByReference extends COPYDATASTRUCT implements Structure.ByReference {}
		ULONG_PTR dwData;
		DWORD cbData;
		WString lpData;
		
		@Override
		protected List getFieldOrder() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	
	LRESULT SendMessageA(HWND hwnd,int umsg,int wParam,int lParam);
	void PostMessageA(HWND hwnd,int umsg,int wParam,int lParam);
	HWND FindWindowW(String lcClassname, String lpName);
	HWND FindWindowA(String lcClassname, String lpName);
	
	/* Light Jockey Specifics */
	class LJGenericItem extends Structure{
		static class ByReference extends LJGenericItem implements Structure.ByReference{};
		int number;
		int flags;
		WString name;
		
		@Override
		protected List getFieldOrder() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
