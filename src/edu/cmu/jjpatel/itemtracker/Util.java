package edu.cmu.jjpatel.itemtracker;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Util{
	public static void createAlarm(Context context,boolean forceRecreate){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String notificationTime = prefs.getString("prefNotificationTime", "17:00");
		
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent itemUpdateIntent = new Intent(context,ItemUpdateService.class);
		PendingIntent pi = PendingIntent.getService(context, 0, itemUpdateIntent,PendingIntent.FLAG_NO_CREATE);
		//stop any current alarm when ever preferences are changed
		if(forceRecreate && pi!=null)
			am.cancel(pi);
		//Create alarm only if not present or if settings are changed		
		if(pi==null || forceRecreate){			
			pi = PendingIntent.getService(context, 0, itemUpdateIntent,PendingIntent.FLAG_UPDATE_CURRENT);
			//Schedule the update service as per the notification time
			int hour = Integer.valueOf(notificationTime.split(":")[0]);
			int minute = Integer.valueOf(notificationTime.split(":")[1]);
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY,hour);
			cal.set(Calendar.MINUTE,minute);
			cal.set(Calendar.SECOND,00);
			//Repeat the alarm every day
			am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 24*60*60*1000, pi);
		}
	}
}
