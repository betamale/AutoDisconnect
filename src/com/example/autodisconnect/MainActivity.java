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
	protected Calendar edit_time = Calendar.getInstance();
	protected Calendar sched_time = Calendar.getInstance();
	protected boolean scheduled = false;
	protected TextView date_view;
	protected TextView time_view;
	protected EditText days_edit;
	protected EditText hours_edit;
	protected EditText minutes_edit;
	protected DaysHoursMinsWatcher watcher;
	
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
		sched_time.setTimeInMillis(sharedPref.getLong(getString(R.string.saved_schedule_time), 0));
		updateStatusView();
		
		edit_time.setTimeInMillis(sharedPref.getLong(getString(R.string.saved_edit_time), 0));
		preventThePast();
		updateDateAndTimeView();
		
		days_edit.setText(sharedPref.getString(getString(R.string.saved_days), "1"));
		hours_edit.setText(sharedPref.getString(getString(R.string.saved_hours), "0"));
		minutes_edit.setText(sharedPref.getString(getString(R.string.saved_minutes), "0"));
	}

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences sharedPref = getPreferences(
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		
		editor.putBoolean(getString(R.string.saved_schedule_state), scheduled);
		editor.putLong(getString(R.string.saved_schedule_time), sched_time.getTimeInMillis());
		
		editor.putLong(getString(R.string.saved_edit_time), edit_time.getTimeInMillis());
		
		editor.putString(getString(R.string.saved_days), days_edit.getText().toString());
		editor.putString(getString(R.string.saved_hours), hours_edit.getText().toString());
		editor.putString(getString(R.string.saved_minutes), minutes_edit.getText().toString());
		editor.commit();
	}

	public void onTrafficSwitchClick(View v) {
		Switch s = (Switch)v;
		MobileDataFunctions.setMobileDataEnabled(getApplicationContext(), s.isChecked());
	}
	
	public void onScheduleAtButtonClick(View v) {        
		scheduleDisconnectAt(edit_time);
	}
	
	public void onScheduleInButtonClick(View v) {
		int days;
		int hours;
		int mins;
		try {
			days = Integer.parseInt(days_edit.getText().toString());
		} catch(Exception e) {
			days = 0;
		}
		try {
			hours = Integer.parseInt(hours_edit.getText().toString());
		} catch(Exception e) {
			hours = 0;
		}
		try {
			mins = Integer.parseInt(minutes_edit.getText().toString());
		} catch(Exception e) {
			mins = 0;
		}
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, days);
		c.add(Calendar.HOUR_OF_DAY, hours);
		c.add(Calendar.MINUTE, mins);
		scheduleDisconnectAt(c);
	}
	
	public void onUnscheduleButtonClick(View v) {        
		Log.d("MainActivity.onScheduleButtonClick", "Unscheduling planned disconnect.");
		if(scheduled) {
			Log.d("MainActivity.onScheduleButtonClick", "Disconnect was scheduled at " + sched_time + ".");
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
	
	public void onTimeSet(TimePicker view, int hour, int minute) {
		Log.d("MainActivity.onTimeSet()", hour + " " + minute);
		edit_time.set(Calendar.HOUR_OF_DAY, hour);
        edit_time.set(Calendar.MINUTE, minute);
        preventThePast();
        updateDateAndTimeView();
    }
	
	public void onDateSet(DatePicker view, int year, int month, int day) {
		Log.d("MainActivity.onDateSet()", year + " " + month + " " + day);
        edit_time.set(Calendar.YEAR, year);
        edit_time.set(Calendar.MONTH, month);
        edit_time.set(Calendar.DAY_OF_MONTH, day);
        preventThePast();
        updateDateAndTimeView();
    }
	
	public void showTimePickerDialog(View v) {
	    DialogFragment newFragment = new TimePickerFragment();
	    newFragment.show(getFragmentManager(), "timePicker");
	}
	
	public void showDatePickerDialog(View v) {
	    DialogFragment newFragment = new DatePickerFragment();
	    newFragment.show(getFragmentManager(), "datePicker");
	}
		
	public void preventThePast() {
		Calendar now = Calendar.getInstance();
        if(edit_time.before(now)) {
        	edit_time = now;
        }
    }
	
	protected void updateDateAndTimeView() {
		date_view.setText(DateFormat.getDateFormat(this).format(edit_time.getTime()));
		time_view.setText(DateFormat.getTimeFormat(this).format(edit_time.getTime()));
	}
	
	protected void updateStatusView() {
		String state = getString(R.string.status_nothing_scheduled);
		if(scheduled){
			state = getString(R.string.status_scheduled_at) 
					+ " " + DateFormat.getDateFormat(this).format(sched_time.getTime())
					+ " " + DateFormat.getTimeFormat(this).format(sched_time.getTime());
		}
		TextView tv = (TextView)findViewById(R.id.status_view);
		tv.setText(state);
	}
	
	public void scheduleDisconnectAt(Calendar time) {        
		Log.d("MainActivity.scheduleDisconnectAt(Calendar)", "Scheduling disconnect at " + time + ".");
						
		DisconnectIntent intent = new DisconnectIntent(getApplicationContext());
		PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
		
		AlarmManager am =  (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP,  time.getTimeInMillis(), pi);
		sched_time = time;
		scheduled = true;
		updateStatusView();
	}

}
