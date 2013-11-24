package edu.cmu.jjpatel.itemtracker;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	public BootReceiver() {
	}
	/**
	 * Whenever the systems boots alarms are lost so we need to start the alarm again on boot receive event
	 */
	@Override
	public void onReceive(Context context, Intent receivedIntent) {
		if(receivedIntent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
			/*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context); 
			int minutes = prefs.getInt("interval"); 
			*/
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent itemUpdateIntent = new Intent(context,ItemUpdateService.class);
			PendingIntent pi = PendingIntent.getService(context, 0, itemUpdateIntent, 0);
			//stop any current alarm
			am.cancel(pi);
			//start new alarm
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY,20);
			cal.set(Calendar.MINUTE,00);
			cal.set(Calendar.SECOND,00);
			//Repeat the alarm every day
			am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 24*60*60*1000, pi);
		}
	}
}
