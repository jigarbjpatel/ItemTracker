package edu.cmu.jjpatel.itemtracker;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Collection of helper methods
 * @author Jigar Patel
 */
public class Util{
	static String DEFAULT_NOTIFICATION_TIME = "17:00";
	/**
	 * Creates the notification alarm. This function gets called from multiple places. 
	 * It creates a new alarm only if there is none existing or it is forced to create.
	 * If new alarm is created forcefully then old alarm is cancelled - this scenario is useful 
	 * 	when the user changes notification time
	 * @param context current context object
	 * @param forceRecreate if true new alarm will be created whether or not old alarm is present
	 */
	public static void createAlarm(Context context,boolean forceRecreate){
		//get the notification time as set by user
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String notificationTime = prefs.getString("prefNotificationTime", DEFAULT_NOTIFICATION_TIME);

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
	/**
	 * Updates the current stock of item based on lastUdpatedAt date.
	 * Does not reduce stock amount below zero
	 * Method may be called simultaneously from ItemUpdateService or UI thread hence synchronized 
	 * @param context current context
	 * @param i Item to update
	 */
	public static synchronized void updateCurrentStock(Context context,Item i){
		Date today = new Date();
		DatabaseHelper dbHelper = null;
		try{
			dbHelper = new DatabaseHelper(context,null);
			//Do not update days left if it is alreay 0
			if(i.getDaysLeft() > 0){
				//Following time difference is required because update service may not run on certain day 
				//(for example, phone is switched off for 2 days) 
				long timeDiff = Math.abs(today.getTime() - i.getLastUpdatedAt().getTime());
				long dayDiff = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
				//Do not update if it is already updated today
				if(dayDiff > 0){
					int daysLeft = i.getDaysLeft() - (int)dayDiff;  
					i.setLastUpdatedAt(today);
					i.setDaysLeft(daysLeft);
					dbHelper.updateItem(i);
				}
			}
		}catch(Exception ex){
			String errorMsg = ex.getMessage() != null ? ex.getMessage() : "Null Pointer Exception";
			Log.e("updateCurrentStock",errorMsg);
		}finally{
			if(dbHelper != null)
				dbHelper.close();
		}
	}
}
