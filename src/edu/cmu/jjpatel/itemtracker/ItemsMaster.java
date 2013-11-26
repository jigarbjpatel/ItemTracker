package edu.cmu.jjpatel.itemtracker;

import java.util.Date;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Items Usage Activity - interface for adding new items and their usage duration. 
 * Allows Edit and Delete of items as well.
 * @author Jigar Patel
 */
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
		registerForContextMenu(itemsListView);
	}
	@Override
	public void onResume(){
		super.onResume();
		init();
		Util.createAlarm(this, false);
	}
	/**
	 * Initializes the items list and binds to list view
	 */
	public void init(){
		ListView itemsListView = (ListView) findViewById(R.id.itemsListview);
		dbHelper = new DatabaseHelper(this,null);
		items = dbHelper.getAllItemsWithAllFields();
		int resId = R.layout.items_row_layout;
		adapter = new ItemsArrayAdapter(this,resId,items);
		itemsListView.setAdapter(adapter);

		//ListView Animation (inspired from API demos) 
		AnimationSet set = new AnimationSet(true);
		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(50);
		set.addAnimation(animation);
		animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f
				);
		animation.setDuration(100);
		set.addAnimation(animation);
		LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
		itemsListView.setLayoutAnimation(controller);
	}
	/**
	 * Called when user clicks on Add New Item button or + icon in action bar
	 * Presents the Add New Item form to user and handles the button click events
	 * @param v the view which called this method
	 */
	public void addItem(View v){
		final Dialog d = new Dialog(this);		  
		d.setContentView(R.layout.add_new_item);		  
		d.setTitle("Add Item");		  
		d.setCancelable(true);		  
		final EditText txtName = (EditText) d.findViewById(R.id.txtName);
		final EditText txtRemindInDays = (EditText) d.findViewById(R.id.txtRemindInDays);
		//Handle Cancel button click event
		Button btnCancel = (Button) d.findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				d.dismiss();
			}
		});
		//Handle save button click event
		Button btnSave = (Button) d.findViewById(R.id.btnSaveItem);		  
		btnSave.setOnClickListener(new View.OnClickListener() {		  
			public void onClick(View v) 	{	  
				String itemName = txtName.getText().toString();
				if(!itemName.isEmpty()){
					Item i=new Item();
					i.setName(itemName);
					i.setRemindDays(Integer.parseInt(txtRemindInDays.getText().toString()));
					i.setDaysLeft(0);//set days left as 0 by default
					i.setLastUpdatedAt(new Date()); //set last updted to todays date
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
	//Called when the context menu is about to be shown - show Edit and Delete options
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
		return true;
	}
	/**
	 * Called when user clicks on Edit option in context menu.
	 * User sees an Edit form.
	 * Handles both save and cancel events.
	 */
	public void editItem(){
		//Set up the dialog
		final Dialog d = new Dialog(this);		  
		d.setContentView(R.layout.add_new_item);		  
		d.setTitle("Edit Item");		  
		d.setCancelable(true);		  
		final EditText txtName = (EditText) d.findViewById(R.id.txtName);
		final EditText txtRemindInDays = (EditText) d.findViewById(R.id.txtRemindInDays);
		txtName.setText(selectedItem.getName());
		txtRemindInDays.setText(String.valueOf(selectedItem.getRemindDays()));
		//Handle Cancel event
		Button btnCancel = (Button) d.findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				d.dismiss();
			}
		});
		//Handle Save event
		Button btnSave = (Button) d.findViewById(R.id.btnSaveItem);		  		
		btnSave.setOnClickListener(new View.OnClickListener() {		  
			public void onClick(View v) 	{	  
				String itemName = txtName.getText().toString();
				if(!itemName.isEmpty()){
					Item i = items.get(selectedItemPosition);
					i.setName(itemName);
					//Update DaysLeft based on current stock and new usage duration
					int newUsageDuration = Integer.parseInt(txtRemindInDays.getText().toString());
					int oldUsageDuration = i.getRemindDays();
					if(oldUsageDuration != newUsageDuration){
						float oldPerDayConsumption = (float)1/oldUsageDuration;
						//Before using current stock ensure that is updated - 
						// this is to prevent edge case when update service has not run for many days
						Util.updateCurrentStock(getApplicationContext(), i);
						int currentStock = i.getDaysLeft();
						int unitsOfItemAvailable = (int)Math.round((float)currentStock * oldPerDayConsumption); 
						float newPerDayConsumption = (float)1/newUsageDuration;
						if(newPerDayConsumption != 0){
							int daysLeft = (int)Math.round((float)unitsOfItemAvailable/newPerDayConsumption) ;
							i.setDaysLeft(daysLeft);
						}
						//Update the Usage Duration if changed
						i.setRemindDays(newUsageDuration);
					}
					items.set(selectedItemPosition, i);
					dbHelper.updateItem(i);
					adapter.notifyDataSetChanged();
				}else{
					//TODO: Notify the user to enter some name
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
		}else if(item.getItemId() == R.id.action_settings){
			Intent settingsIntent = new Intent(ItemsMaster.this, SettingsActivity.class);
			settingsIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(settingsIntent); 
		}else if(item.getItemId() == R.id.action_addnew){
			addItem(this.findViewById(R.id.action_addnew));
		}
		return true;
	}	
}
