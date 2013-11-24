package edu.cmu.jjpatel.itemtracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class ItemUpdateService extends Service {
	private WakeLock mPartialWakeLock;
	public ItemUpdateService() {
		
	}

	// This is the old onStart method that will be called on the pre-2.0
	// platform.  On 2.0 or later we override onStartCommand() so this method will not be called.
	@Override
	public void onStart(Intent intent, int startId) {
	    handleCommand(intent);
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if((flags & START_FLAG_RETRY) == 0){
			Log.d("START FLAG RETRY",String.valueOf(flags));
		}else{
			Log.d("START FLAG OTHER",String.valueOf(flags));
		}
		handleCommand(intent);
		return START_REDELIVER_INTENT;
	};
	@Override
	public void onDestroy() {
		super.onDestroy();
		mPartialWakeLock.release();
	};
	private void handleCommand(Intent intent) {
		// obtain the wake lock 
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE); 
		mPartialWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ItemUpdateService"); 
		mPartialWakeLock.acquire(); 
		// do the actual work, in a separate thread 
		new UpdateItemTask().execute();		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		//throw new UnsupportedOperationException("Not yet implemented");
		return null;
	}
	
	private class UpdateItemTask extends AsyncTask<Void, Void, List<String>>{
		/**
		 * Returns the list of items due today (getting over today or are already over)
		 */
		@Override
		protected List<String> doInBackground(Void... params) {
			//TODO: Update the database based on lastUpdate date - take care of thread synchronization
			DatabaseHelper dbHelper = null;
			List<String> itemsDueToday = new ArrayList<String>();
			try{
				dbHelper = new DatabaseHelper(getApplicationContext(),null);
				ArrayList<Item> allItems = (ArrayList<Item>)dbHelper.getAllItemsWithAllFields();
				Date today = new Date();
				for(Item i : allItems){
					if(i.getDaysLeft() > 0){
						long timeDiff = Math.abs(today.getTime() - i.getLastUpdatedAt().getTime());
						long dayDiff = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
						if(dayDiff > 0){
							int daysLeft = i.getDaysLeft() - (int)dayDiff;  
							i.setLastUpdatedAt(today);
							i.setDaysLeft(daysLeft);
							dbHelper.updateItem(i);
						}
					}else{
						itemsDueToday.add(i.getName());
					}
				}
			}catch(Exception e){
				Log.e("Update Item Task",e.getMessage());
			}finally{
				if(dbHelper != null)
					dbHelper.close();
			}
			return itemsDueToday;
		}
		@Override
		protected void onPostExecute(List<String> result) {
			super.onPostExecute(result);
			//Send notification
			int itemsDue = result.size();
			StringBuffer notificationText = new StringBuffer();
			for(int i=0; i<itemsDue; i++){
				if(i!=0)
					notificationText.append(", ");
				notificationText.append(result.get(i));				
			}
			Context context = getApplicationContext();
			NotificationManager nm = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
			Intent notificationIntent = new Intent(context,ReminderActivity.class);
			PendingIntent pi = PendingIntent.getActivity(context, 0, notificationIntent, 0);
			Notification n = new Notification.Builder(context)
	         .setContentTitle(String.valueOf(itemsDue) + " Items Due Today")
	         .setContentText(notificationText.toString())
	         .setSmallIcon(android.R.drawable.stat_notify_more)
	         //.setLargeIcon(R.drawable.ic_launcher)
	         .setContentIntent(pi)
	         .build();	 
			/*Notification n = new Notification();
			n.icon = android.R.drawable.stat_notify_sync;
			n.tickerText = "Test";
			n.when = System.currentTimeMillis();*/
			nm.notify(0, n);
			//stop the alarm
			stopSelf();
		}
	}
}
