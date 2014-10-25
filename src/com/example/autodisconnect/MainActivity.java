package com.example.autodisconnect;

import java.util.Calendar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.DatePicker;

public class MainActivity extends Activity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
	protected Calendar editTime = Calendar.getInstance();
	private Calendar schedTime = Calendar.getInstance();
	private boolean scheduled = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		date_view = (TextView)findViewById(R.id.date_view);
		time_view = (TextView)findViewById(R.id.time_view);

		watcher = new DaysHoursMinsWatcher(this);
		days_edit =  (EditText) findViewById(R.id.days_edit);
		days_edit.addTextChangedListener(new DaysHoursMinsWatcher(this));
		hours_edit =  (EditText) findViewById(R.id.hours_edit);
		hours_edit.addTextChangedListener(watcher);
		minutes_edit =  (EditText) findViewById(R.id.minutes_edit);
		minutes_edit.addTextChangedListener(watcher);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart() {
		super.onStart();
		updateDateAndTimeView();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		try {
			Switch ts = (Switch)findViewById(R.id.traffic_switch);
			ts.setChecked(MobileDataFunctions.getMobileDataEnabled(getApplicationContext()));
		} catch (Throwable t) {
			Log.e("MainActivity.onResume", "caught: " + t);
		}
		
		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		scheduled = sharedPref.getBoolean(getString(R.string.saved_schedule_state), false);
		schedTime.setTimeInMillis(sharedPref.getLong(getString(R.string.saved_schedule_time), 0));
		updateStatusView();
	}

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences sharedPref = getPreferences(
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(getString(R.string.saved_schedule_state), scheduled);
		editor.putLong(getString(R.string.saved_schedule_time), schedTime.getTimeInMillis());
		editor.commit();
	}

	public void onTrafficSwitchClick(View v) {
		Switch s = (Switch)v;
		MobileDataFunctions.setMobileDataEnabled(getApplicationContext(), s.isChecked());
	}
	
	public void onScheduleAtButtonClick(View v) {        
		Log.d("MainActivity.onScheduleButtonClick", "Scheduling disconnect at " + editTime + ".");
						
		DisconnectIntent intent = new DisconnectIntent(getApplicationContext());
		PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
		
		AlarmManager am =  (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP,  editTime.getTimeInMillis(), pi);
		schedTime = editTime;
		scheduled = true;
		updateStatusView();
	}
	
	public void onUnscheduleButtonClick(View v) {        
		Log.d("MainActivity.onScheduleButtonClick", "Unscheduling planned disconnect.");
		if(scheduled) {
			Log.d("MainActivity.onScheduleButtonClick", "Disconnect was scheduled at " + schedTime + ".");
		} else {
			Log.d("MainActivity.onScheduleButtonClick", "No disconnect was scheduled. "
					+ "Trying to remove all disconnect intents anyway.");
		}

		DisconnectIntent intent = new DisconnectIntent(getApplicationContext());
		PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
		
		AlarmManager am =  (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		am.cancel(pi);
		scheduled = false;
		updateStatusView();
	}
	
	public void showTimePickerDialog(View v) {
	    DialogFragment newFragment = new TimePickerFragment();
	    newFragment.show(getFragmentManager(), "timePicker");
	}
	
	public void onTimeSet(TimePicker view, int hour, int minute) {
		editTime.set(Calendar.HOUR_OF_DAY, hour);
        editTime.set(Calendar.MINUTE, minute);
        updateDateAndTimeView();
        updateDaysHoursMinsView();
    }
	
	public void setTime(int hour, int minute) {
        
        long now = Calendar.getInstance().getTimeInMillis();
        if(editTime.getTimeInMillis() < now) {
        	editTime.setTimeInMillis(now);
        }
	}
	
	public void showDatePickerDialog(View v) {
	    DialogFragment newFragment = new DatePickerFragment();
	    newFragment.show(getFragmentManager(), "datePicker");
	}
	
	public void onDateSet(DatePicker view, int year, int month, int day) {
        editTime.set(Calendar.YEAR, year);
        editTime.set(Calendar.MONTH, month);
        editTime.set(Calendar.DAY_OF_MONTH, day);
        preventThePast();
        updateDateAndTimeView();
        updateDaysHoursMinsView();
	}
	
	protected void updateDaysHoursMinsView() {
        long nowmins = Calendar.getInstance().getTimeInMillis() / (1000 * 60);
        long diffmins = (editTime.getTimeInMillis()) / (1000 * 60)  - nowmins;
        long days = diffmins / (60 * 24);
        long hours = (diffmins / 60) % 24;
        long mins = diffmins % 60;
        Log.d("MainActivity.onDateSet()", " " + days + " " + hours + " " + mins);
        days_edit.setText(Long.toString(days));
        hours_edit.setText(Long.toString(hours));
        minutes_edit.setText(Long.toString(mins));
    }
	
	public void preventThePast() {
        long now = Calendar.getInstance().getTimeInMillis();
        if(editTime.getTimeInMillis() < now) {
        	editTime.setTimeInMillis(now);
        }
    }
	
	protected void updateDateAndTimeView() {
		date_view.setText(DateFormat.getDateFormat(this).format(editTime.getTime()));
		time_view.setText(DateFormat.getTimeFormat(this).format(editTime.getTime()));
	}
	
	protected void updateStatusView() {
		String state = getString(R.string.status_nothing_scheduled);
		if(scheduled){
			state = getString(R.string.status_scheduled_at) 
					+ " " + DateFormat.getDateFormat(this).format(schedTime.getTime())
					+ " " + DateFormat.getTimeFormat(this).format(schedTime.getTime());
		}
		TextView tv = (TextView)findViewById(R.id.status_view);
		tv.setText(state);
	}
	
	protected TextView date_view;
	protected TextView time_view;
	protected EditText days_edit;
	protected EditText hours_edit;
	protected EditText minutes_edit;
	protected DaysHoursMinsWatcher watcher;
}
