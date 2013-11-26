package edu.cmu.jjpatel.itemtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/**
 * Handles boot completed event and recreates the notification alarm 
 * @author Jigar Patel
 */
public class BootReceiver extends BroadcastReceiver {
	public BootReceiver() {
	}
	/**
	 * Whenever the systems boots, repeating alarms are lost.
	 * This method will start the alarm again on boot receive event.
	 */
	@Override
	public void onReceive(Context context, Intent receivedIntent) {
		if(receivedIntent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
			Util.createAlarm(context,false);
		}
	}
}
