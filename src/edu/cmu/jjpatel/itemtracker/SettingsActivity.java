package edu.cmu.jjpatel.itemtracker;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
/**
 * User Preferences Screen called when Settings button is clicked
 * @author Jigar Patel
 */
public class SettingsActivity extends PreferenceActivity 
	implements OnSharedPreferenceChangeListener{

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
	@Override
	protected void onResume() {
	    super.onResume();
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		prefs.registerOnSharedPreferenceChangeListener(this);
	}
	@Override
	protected void onPause() {
	    super.onPause();
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if(key.equals("prefNotificationTime")){
		//Notification Time has changed => Cancel current alarm and create new
			Util.createAlarm(getApplicationContext(), true);
		}
	}

}
