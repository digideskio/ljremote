package com.ljremote.server.driver;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Callback;
import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTR;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.ptr.NativeLongByReference;

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

	final NativeLong ZERO = new NativeLong(0);
	
	HWND CreateWindowEx(int styleEx, String classname, String windowName,
			int style, int x, int y, int width, int height, HWND hndParent,
			int hndMenu, int hndInst, Object parm);

	boolean DestroyWindow(HWND hwnd);

//	NativeLong MsgWaitForMultipleObjects(NativeLong nCount, HANDLE[] pHandles,
//			boolean bWaitAll, NativeLong dwMilliseconds, NativeLong dwWakeMask);
	int MsgWaitForMultipleObjects(int nCount, HANDLE[] pHandles,
			boolean bWaitAll, int dwMilliseconds, int dwWakeMask);

	/*
	 * PeekMessage() Options
	 */
	final int PM_NOREMOVE = 0x0000;
	final int PM_REMOVE = 0x0001;
	final int PM_NOYIELD = 0x0002;

	LRESULT DefWindowProc(HWND hWnd, int msg, WPARAM wParam, LPARAM lParam);

	/*
	 * CopyData
	 */
	final int WM_COPYDATA = 0x004a;

	class COPYDATASTRUCT extends Structure {
		static class ByReference extends COPYDATASTRUCT implements
				Structure.ByReference {

			public ByReference(Pointer lParam) {
				super(lParam);
			}
		}

		public ULONG_PTR dwData;
		public DWORD cbData;
		public Pointer lpData;

		public COPYDATASTRUCT(Pointer lParam) {
			super(lParam);
		}

		public COPYDATASTRUCT() {
			super();
		}

		@Override
		protected List<?> getFieldOrder() {
			return Arrays.asList(new String[] { "dwData", "cbData", "lpData" });
		}
	}

	interface WindowProc extends StdCallCallback {
		public LRESULT callback (HWND hWnd, int uMsg, int wParam, Pointer lParam );
	}
	
	/**
     * Sets a new address for the window procedure (value to be set).
     */
    public static final int GWLP_WNDPROC = -4;
	
	/**
     * Changes an attribute of the specified window
     * @param   hWnd        A handle to the window
     * @param   nIndex      The zero-based offset to the value to be set.
     * @param   callback    The callback function for the value to be set.
     */
    public NativeLong SetWindowLongA(HWND hWnd, NativeLong nIndex, Callback callback) throws LastErrorException;
    public NativeLongByReference SetWindowLongPtr(HWND hWnd, NativeLong nIndex, Callback callback) throws LastErrorException;
    public Callback GetWindowLongA(HWND hWnd, int nIndex);
//    public NativeLong GetWindowLongA(HWND hWnd, NativeLong nIndex);
	
	LRESULT SendMessageA(HWND hwnd, int umsg, int wParam, int lParam);
	LRESULT SendMessageA(HWND lJHandle, int umsg, int wParam,
			Pointer pointer);
//	LRESULT SendMessageA(HWND lJHandle, int wmCopydata, HWND viewer, Pointer pointer);
	boolean SendMessageA(HWND lJHandle, int wmCopydata, HWND viewer, Pointer pointer);

	void PostMessageA(HWND hwnd, int umsg, int wParam, int lParam);

	HWND FindWindowW(String lcClassname, String lpName);

	HWND FindWindowA(String lcClassname, String lpName);

	/* Light Jockey Specifics */
	class LJGenericItem extends Structure {
		static class ByReference extends LJGenericItem implements
				Structure.ByReference {
		};

		int number;
		int flags;
		WString name;

		@Override
		protected List<?> getFieldOrder() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	LRESULT DefWindowProcA(HWND hWnd, NativeLong uMsg, NativeLong wParam, Pointer lParam);

	LRESULT DefWindowProcA(HWND hWnd, int uMsg, WPARAM wParam, LPARAM lParam);

	int GetLastError();

	int GetModuleHandleA(Object object);

	NativeLong SendMessageA(HWND lJHandle, NativeLong wmCopydata, HWND viewer,
			Pointer pointer);

	boolean PeekMessageA(MSG msg, HWND hWnd, NativeLong i, NativeLong j, NativeLong pmRemove);

	boolean TranslateMessageA(MSG msg);

	LRESULT DispatchMessageA(MSG msg);


}
