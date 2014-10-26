package com.example.autodisconnect;

import java.util.Calendar;

import android.text.Editable;
import android.text.TextWatcher;

public class DaysHoursMinsWatcher implements TextWatcher {

	public DaysHoursMinsWatcher(MainActivity act) {
		this.act = act;
	}

	@Override
	public void afterTextChanged(Editable edit) {
		int days;
		int hours;
		int mins;
		try {
			days = Integer.parseInt(act.days_edit.getText().toString());
		} catch(Exception e) {
			days = 0;
		}
		try {
			hours = Integer.parseInt(act.hours_edit.getText().toString());
		} catch(Exception e) {
			hours = 0;
		}
		try {
			mins = Integer.parseInt(act.minutes_edit.getText().toString());
		} catch(Exception e) {
			mins = 0;
		}
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, days);
		c.add(Calendar.HOUR_OF_DAY, hours);
		c.add(Calendar.MINUTE, mins);
		act.edit_time.setTimeInMillis(c.getTimeInMillis());
		act.updateDateAndTimeView();
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {

	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

	}
	
	private MainActivity act;
}
