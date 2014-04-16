package com.example.autodisconnect;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;

public class MainActivity extends Activity {

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
	protected void onResume() {
		super.onResume();
		try {
			Switch ts = (Switch)findViewById(R.id.traffic_switch);
			ts.setChecked(MobileDataFunctions.getMobileDataEnabled(getApplicationContext()));
		} catch (Throwable t) {
			Log.e("MainActivity.onResume", "caught: " + t);
		}
	}
	
	public void onTrafficSwitchClick(View v) {
		Switch s = (Switch)v;
		MobileDataFunctions.setMobileDataEnabled(getApplicationContext(), s.isChecked());
	}
	
	public void onScheduleButtonClick(View v) {
		
        
		final EditText et = (EditText)findViewById(R.id.edit_timespan);
		final String text = et.getText().toString().trim();
		final String[] split = text.split(":");
		
		long millis = 0;
		if(split.length >= 4){
			millis += Long.parseLong(split[split.length - 4]) * 24 * 60 * 60 * 1000;
		}
		if(split.length >= 3){
			millis += Long.parseLong(split[split.length - 3]) * 60 * 60 * 1000;
		}
		if(split.length >= 2){
			millis += Long.parseLong(split[split.length - 2]) * 60 * 1000;
		}
		if(split.length >= 1){
			millis += Long.parseLong(split[split.length - 1]) * 1000;
		}
		Log.d("MainActivity.onScheduleButtonClick", "scheduling disconnect in " + millis + "ms");
		millis += SystemClock.elapsedRealtime();
				
		Intent intent = new Intent(getApplicationContext(), DisconnectAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
		
		AlarmManager am =  (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,  millis, pi);
	}
}
