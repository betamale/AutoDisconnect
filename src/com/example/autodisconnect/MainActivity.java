package com.example.autodisconnect;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.DatePicker;

public class MainActivity extends Activity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
	private Calendar editTime = Calendar.getInstance();
	private Calendar schedTime = Calendar.getInstance();
	private boolean scheduled = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
	
	
	@Override
	protected void onStart() {
		super.onStart();
		updateTimeView();
		updateDateView();
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
	
	public void onScheduleButtonClick(View v) {        
		Log.d("MainActivity.onScheduleButtonClick", "Scheduling disconnect at " + editTime);
						
		Intent intent = new Intent(getApplicationContext(), DisconnectAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
		
		AlarmManager am =  (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP,  editTime.getTimeInMillis(), pi);
		schedTime = editTime;
		scheduled = true;
		updateStatusView();
	}
	
	public void showTimePickerDialog(View v) {
	    DialogFragment newFragment = new TimePickerFragment();
	    newFragment.show(getFragmentManager(), "timePicker");
	}
	
	public void onTimeSet(TimePicker view, int hour, int minute) {
        editTime.set(Calendar.HOUR_OF_DAY, hour);
        editTime.set(Calendar.MINUTE, minute);
        updateTimeView();
    }
	
	public void showDatePickerDialog(View v) {
	    DialogFragment newFragment = new DatePickerFragment();
	    newFragment.show(getFragmentManager(), "datePicker");
	}
	
	public void onDateSet(DatePicker view, int year, int month, int day) {        
        editTime.set(Calendar.YEAR, year);
        editTime.set(Calendar.MONTH, month);
        editTime.set(Calendar.DAY_OF_MONTH, day);
        updateDateView();
    }
	
	protected void updateDateView() {
		TextView tv = (TextView)findViewById(R.id.date_view);
		tv.setText(DateFormat.getDateFormat(this).format(editTime.getTime()));
	}
	
	protected void updateTimeView() {
		TextView tv = (TextView)findViewById(R.id.time_view);
		tv.setText(DateFormat.getTimeFormat(this).format(editTime.getTime()));
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
}
