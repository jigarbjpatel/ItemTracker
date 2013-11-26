package edu.cmu.jjpatel.itemtracker;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
/**
 * Background service which runs once in a day to update the items table.
 * Reduces days left depending on last updated at date.
 * The service handles odd cases like it runs twice a day or does not run for multiple days
 * @author Jigar Patel
 */
public class ItemUpdateService extends Service {
	//Used to ensure that service completes its run to prevent device from sleeping
	private WakeLock mPartialWakeLock;
	public ItemUpdateService() {
	}

	// This is the old onStart method that will be called on the pre-2.0 platform. 
	// On 2.0 or later we override onStartCommand() so this method will not be called.
	@Override
	public void onStart(Intent intent, int startId) {
		handleCommand(intent);
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/*if((flags & START_FLAG_RETRY) == 0){
			Log.d("START FLAG RETRY",String.valueOf(flags));
		}else{
			Log.d("START FLAG OTHER",String.valueOf(flags));
		}*/
		handleCommand(intent);
		return START_REDELIVER_INTENT;
	};
	@Override
	public void onDestroy() {
		super.onDestroy();
		mPartialWakeLock.release();
	};
	/**
	 * Called when the service is triggered. Spawns a child thread to do updates and releases the main thread.
	 * @param intent Intent passed
	 */
	private void handleCommand(Intent intent) {
		//obtain the wake lock 
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
	/**
	 * Async Task used to do actual update of database in separate thread
	 * @author Jigar Patel
	 *
	 */
	private class UpdateItemTask extends AsyncTask<Void, Void, List<String>>{
		/**
		 * Returns the list of items due (getting over or are already over)
		 */
		@Override
		protected List<String> doInBackground(Void... params) {
			DatabaseHelper dbHelper = null;
			List<String> itemsDue = new ArrayList<String>();
			try{
				//Update the days Left, if required
				dbHelper = new DatabaseHelper(getApplicationContext(),null);
				ArrayList<Item> allItems = (ArrayList<Item>)dbHelper.getAllItemsWithAllFields();
				//Reduce daysLeft by difference of today and lastUpdatedAt
				for(Item i : allItems){
					//Use thread synchronized method
					Util.updateCurrentStock(getApplicationContext(), i);
				}
				
				//Get Items due
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				//Indicates how many days before user should be notified  
				int notifyBefore = Integer.valueOf(prefs.getString("prefNotificationBefore", "0"));
				for(Item i: allItems){
					if(i.getDaysLeft() <= notifyBefore){
						itemsDue.add(i.getName());
					}
				}
			}catch(Exception e){
				Log.e("Update Item Task",e.getMessage());
			}finally{
				if(dbHelper != null)
					dbHelper.close();
			}
			return itemsDue;
		}
		@Override
		protected void onPostExecute(List<String> result) {
			super.onPostExecute(result);

			//Notify only if user wants to be notified
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			boolean notificationEnabled = prefs.getBoolean("prefSendNotification", true); 
			if(notificationEnabled){
				notifyUser(result);
			}
			
			//Stop the service
			stopSelf();
		}
		/**
		 * Notifies the user about items due
		 * @param result List of items due
		 */
		private void notifyUser(List<String> result) {
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
			.setContentTitle(String.valueOf(itemsDue) + " Items Due")
			.setContentText(notificationText.toString())
			.setSmallIcon(R.drawable.ic_basket)
			//.setLargeIcon(R.drawable.ic_basket)
			.setContentIntent(pi)
			.build();	 
			nm.notify(0, n);
		}
	}
}
