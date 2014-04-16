package com.example.autodisconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DisconnectAlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		MobileDataFunctions.setMobileDataEnabled(context, false);
		Log.d("DisconnectAlarmReceiver.onReceive", "Mobile data traffic disabled.");
	}
}
