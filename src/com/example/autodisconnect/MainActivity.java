package com.example.autodisconnect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
			ts.setChecked(getMobileDataEnabled());
		} catch (Throwable t) {
			Log.e("MainActivity.onResume", "caught: " + t);
		}
	}
	
	protected boolean getMobileDataEnabled() {
		try {
			final ConnectivityManager conman = (ConnectivityManager) getApplicationContext()
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			final Class<?> conmanClass = Class.forName(conman.getClass()
					.getName());
			final Field iConnectivityManagerField = conmanClass
					.getDeclaredField("mService");
			iConnectivityManagerField.setAccessible(true);
			final Object iConnectivityManager = iConnectivityManagerField
					.get(conman);
			final Class<?> iConnectivityManagerClass = Class
					.forName(iConnectivityManager.getClass().getName());
			final Method getMobileDataEnabledMethod = iConnectivityManagerClass
					.getDeclaredMethod("getMobileDataEnabled");
			getMobileDataEnabledMethod.setAccessible(true);

			return (Boolean)getMobileDataEnabledMethod.invoke(
					iConnectivityManager, (Object[]) null);
		} catch (Throwable t) {
			Log.e("MainActivity.getMobileDataEnabled", "caught: " + t);
			return false;
		}
	}
	
	protected void setMobileDataEnabled(boolean value) {
		try {
			final ConnectivityManager conman = (ConnectivityManager) getApplicationContext()
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			final Class<?> conmanClass = Class.forName(conman.getClass()
					.getName());
			final Field iConnectivityManagerField = conmanClass
					.getDeclaredField("mService");
			iConnectivityManagerField.setAccessible(true);
			final Object iConnectivityManager = iConnectivityManagerField
					.get(conman);
			final Class<?> iConnectivityManagerClass = Class
					.forName(iConnectivityManager.getClass().getName());
			final Method setMobileDataEnabledMethod = iConnectivityManagerClass
					.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
			setMobileDataEnabledMethod.setAccessible(true);

			setMobileDataEnabledMethod.invoke(iConnectivityManager, value);
		} catch (Throwable t) {
			Log.e("MainActivity.getMobileDataEnabled", "caught: " + t);
		}
	}
	
	public void onTrafficSwitchClick(View v) {
		Switch s = (Switch)v;
		setMobileDataEnabled(s.isChecked());
	}

}
