package com.ljremote.android.json;

public enum ServerStatus {

	UNBOUND (0),
	BOUND   (1),
	DRIVE   (2);
	
	ServerStatus(int i) {
		nativeInt = i;
	}
	
	final int nativeInt;
}
