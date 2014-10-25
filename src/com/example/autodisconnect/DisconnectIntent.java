package com.example.autodisconnect;

import android.content.Context;
import android.content.Intent;

public class DisconnectIntent extends Intent {

	public DisconnectIntent(Context cont) {
		super(cont, DisconnectAlarmReceiver.class);
	}

}
