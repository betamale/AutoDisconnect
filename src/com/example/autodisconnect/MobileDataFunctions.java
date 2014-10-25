package com.example.autodisconnect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

public class MobileDataFunctions {

	public static void setMobileDataEnabled(Context context, boolean value) {
		try {
			final ConnectivityManager conman = (ConnectivityManager) context
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
			Log.e("MobileDataFunctions.setMobileDataEnabled", "caught: " + t);
		}
	}

	public static boolean getMobileDataEnabled(Context context) {
		try {
			final ConnectivityManager conman = (ConnectivityManager) context
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
			Log.e("MobileDataFunctions.getMobileDataEnabled", "caught: " + t);
			return false;
		}
	}

}
