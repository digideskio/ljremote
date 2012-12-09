package com.ljremote.android.json;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class KeepAliveAlarm extends BroadcastReceiver {
	
	public KeepAliveAlarm() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (context instanceof LJClientService) {
			LJClientService service = (LJClientService) context;
			service.doKeepAlive();
		}
	}
}