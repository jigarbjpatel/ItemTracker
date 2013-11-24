package edu.cmu.jjpatel.itemtracker;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ItemsMaster extends Activity {
	List<Item> items;	
	ArrayAdapter<Item> adapter;
	Item selectedItem ;
	int selectedItemPosition;
	DatabaseHelper dbHelper;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_items_master);
		
		ListView itemsListView = (ListView) findViewById(R.id.itemsListview);
		dbHelper = new DatabaseHelper(this,null);
		items = new ArrayList<Item>();
		for(Item i : dbHelper.getAllItems()){
			items.add(i);
		}
		int resId = R.layout.items_row_layout;
		adapter = new ItemsArrayAdapter(this,resId,items);
		itemsListView.setAdapter(adapter);
		
		registerForContextMenu(itemsListView);
	}
	public void addItem(View v){
		final Dialog d = new Dialog(this);		  
		d.setContentView(R.layout.add_new_item);		  
		d.setTitle("Add Item");		  
		d.setCancelable(true);		  
		final EditText txtName = (EditText) d.findViewById(R.id.txtName);
		final EditText txtRemindInDays = (EditText) d.findViewById(R.id.txtRemindInDays);
		Button b = (Button) d.findViewById(R.id.btnSaveItem);		  
		b.setOnClickListener(new View.OnClickListener() {		  
			public void onClick(View v) 	{	  
				String itemName = txtName.getText().toString();
				if(!itemName.isEmpty()){
					Item i=new Item();
					i.setName(itemName);
					i.setRemindDays(Integer.parseInt(txtRemindInDays.getText().toString()));
					i.setDaysLeft(0);//set days left as 0 by default
					items.add(i);
					dbHelper.addItem(i);
					adapter.notifyDataSetChanged();
				}else{
					//TODO: Notify the user to enter something
				}
				d.dismiss();		  
			}		 
		});		 
		d.show();
	}
	//Called when the context menu is about to be shown
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
	      super.onCreateContextMenu(menu, v, menuInfo);
	      AdapterContextMenuInfo aInfo = (AdapterContextMenuInfo) menuInfo;
	      selectedItem  =  (Item) adapter.getItem(aInfo.position);
	      selectedItemPosition = aInfo.position;
	      menu.setHeaderTitle("Options for " + selectedItem.getName());
	      menu.add(1, 1, 1, "Edit");
	      menu.add(1, 2, 2, "Delete");
	  }
	// This method is called when user selects an Item in the Context menu
	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {
	    int menuItemId = menuItem.getItemId();	    
	    if(menuItemId == 2){
	    	//Delete item
	    	items.remove(selectedItemPosition);
	    	dbHelper.deleteItem(selectedItem.getId());
	    	adapter.notifyDataSetChanged();
	    }else if(menuItemId ==1){
	    	//show edit form
	    	editItem();
	    }
	    //Toast.makeText(this, "Item id ["+itemId+"]", Toast.LENGTH_SHORT).show();
	    return true;
	}
	public void editItem(){
		final Dialog d = new Dialog(this);		  
		d.setContentView(R.layout.add_new_item);		  
		d.setTitle("Edit Item");		  
		d.setCancelable(true);		  
		final EditText txtName = (EditText) d.findViewById(R.id.txtName);
		final EditText txtRemindInDays = (EditText) d.findViewById(R.id.txtRemindInDays);
		txtName.setText(selectedItem.getName());
		txtRemindInDays.setText(String.valueOf(selectedItem.getRemindDays()));
		Button b = (Button) d.findViewById(R.id.btnSaveItem);		  		
		b.setOnClickListener(new View.OnClickListener() {		  
			public void onClick(View v) 	{	  
				String itemName = txtName.getText().toString();
				if(!itemName.isEmpty()){
					Item i = items.get(selectedItemPosition);
					i.setName(itemName);
					i.setRemindDays(Integer.parseInt(txtRemindInDays.getText().toString()));
					items.set(selectedItemPosition, i);
					dbHelper.updateItem(i);
					adapter.notifyDataSetChanged();
				}else{
					//TODO: Notify the user to enter something
				}
				d.dismiss();		  
			}		 
		});		 
		d.show();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.items_master, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    if(item.getItemId() == R.id.action_reminder){
	    	Intent intent = new Intent(ItemsMaster.this, ReminderActivity.class);
	    	intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    	startActivity(intent);
	    	//return true;
	    }else if(item.getItemId() == R.id.action_settings){
	    	Intent settingsIntent = new Intent(ItemsMaster.this, SettingsActivity.class);
	    	startActivity(settingsIntent); 
	    }
	    //return super.onOptionsItemSelected(item);
	    return true;
	}	
	/*@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
			case 1:
			//recreate the alarm if values changed
			break;
		}
	};*/
	@Override
	public void onResume(){
		super.onResume();
		
		Util.createAlarm(this, false);
	}
	/**
	 * Creates new repeating alarm to trigger ItemUpdateService
	 */
	/*private void createAlarm() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String notificationTime = prefs.getString("prefNotificationTime", "170000");
		
		Intent itemUpdateIntent = new Intent(this,ItemUpdateService.class);
		//Create alarm only if not present
		PendingIntent pi = PendingIntent.getService(this, 0, itemUpdateIntent,PendingIntent.FLAG_NO_CREATE);
		//am.cancel(pi);
		if(pi==null){
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			pi = PendingIntent.getService(this, 0, itemUpdateIntent,PendingIntent.FLAG_UPDATE_CURRENT);
			//stop any current alarm
			//am.cancel(pi);
			//start new alarm
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY,Integer.valueOf(notificationTime.substring(0, 1)));
			cal.set(Calendar.MINUTE,Integer.valueOf(notificationTime.substring(2, 3)));
			cal.set(Calendar.SECOND,Integer.valueOf(notificationTime.substring(4, 5)));
			//Repeat the alarm every day
			am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 24*60*60*1000, pi);
		}
		
		//am.setInexactRepeating(AlarmManager.INTERVAL_DAY, triggerAtMillis, intervalMillis, operation)
	}*/
}
