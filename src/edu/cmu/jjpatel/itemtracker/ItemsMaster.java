package edu.cmu.jjpatel.itemtracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
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
		registerForContextMenu(itemsListView);
	}
	public void init(){
		ListView itemsListView = (ListView) findViewById(R.id.itemsListview);
		dbHelper = new DatabaseHelper(this,null);
		items = dbHelper.getAllItemsWithAllFields();
		int resId = R.layout.items_row_layout;
		adapter = new ItemsArrayAdapter(this,resId,items);
		itemsListView.setAdapter(adapter);
		
		/*AnimationSet set = new AnimationSet(true);
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
        itemsListView.setLayoutAnimation(controller);*/
	}
	public void addItem(View v){
		final Dialog d = new Dialog(this);		  
		d.setContentView(R.layout.add_new_item);		  
		d.setTitle("Add Item");		  
		d.setCancelable(true);		  
		final EditText txtName = (EditText) d.findViewById(R.id.txtName);
		final EditText txtRemindInDays = (EditText) d.findViewById(R.id.txtRemindInDays);
		Button btnCancel = (Button) d.findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				d.dismiss();
			}
		});
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
			
		Button btnCancel = (Button) d.findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				d.dismiss();
			}
		});
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
						int currentStock = i.getDaysLeft();
						int unitsOfItemAvailable = (int)Math.round((float)currentStock * oldPerDayConsumption); //Ignore truncation
						float newPerDayConsumption = (float)1/newUsageDuration;
						if(newPerDayConsumption != 0){
							int daysLeft = (int)Math.round((float)unitsOfItemAvailable/newPerDayConsumption) ;
							i.setDaysLeft(daysLeft);
						}
						i.setRemindDays(newUsageDuration);
					}
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
		}else if(item.getItemId() == R.id.action_addnew){
			addItem(this.findViewById(R.id.action_addnew));
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
		init();
		Util.createAlarm(this, false);
	}

}
