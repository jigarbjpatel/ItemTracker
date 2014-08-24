package edu.cmu.jjpatel.itemtracker;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
/**
 * Items Due Activity - Displays the items which are due today, this week and later.
 * Also used to get items bought details
 * @author Jigar Patel
 */
public class ReminderActivity extends Activity {
	DatabaseHelper dbHelper;
	List<Item> itemsToday;
	ArrayAdapter<Item> adapterToday;
	List<Item> itemsThisWeek;
	ArrayAdapter<Item> adapterThisWeek;
	List<Item> itemsLater;
	ArrayAdapter<Item> adapterLater;
	Item selectedItem;
	ArrayAdapter<Item> selectedAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reminder);
	}
	@Override
	protected void onResume() {
		super.onResume();
		init();
	};
	/**
	 * Initializes the items list and binds to list view
	 */
	private void init(){
		NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(1);
		//bind to different lists
		int resId = R.layout.reminder_items_row_layout;
		ItemClickListener icl = new ItemClickListener();
		ItemLongClickListener longClickListener = new ItemLongClickListener();
		dbHelper = new DatabaseHelper(this,null);
		
		itemsToday =  dbHelper.getAllItemsByDaysLeft(-1,0);
		//ListView todayList = (ListView) findViewById(R.id.listToday);		
		adapterToday = new ReminderItemsArrayAdapter(this,resId,itemsToday);
		/*todayList.setAdapter(adapterToday);		
		todayList.setOnItemClickListener(icl);
		todayList.setOnItemLongClickListener(longClickListener);
		TextView tvToday = (TextView) findViewById(R.id.txtToday);
		tvToday.setText(R.string.Today);
		tvToday.append(" (" + itemsToday.size() + ")");*/
		bindListView(itemsToday,adapterToday,R.id.listToday,R.id.txtToday,R.string.Today
					, icl,longClickListener);
		
		itemsThisWeek = dbHelper.getAllItemsByDaysLeft(1,6);
		//ListView thisWeekList = (ListView) findViewById(R.id.listThisWeek);		
		adapterThisWeek = new ReminderItemsArrayAdapter(this,resId,itemsThisWeek);
		/*thisWeekList.setAdapter(adapterThisWeek);
		thisWeekList.setOnItemClickListener(icl);
		thisWeekList.setOnItemLongClickListener(longClickListener);
		TextView tvThisWeek = (TextView) findViewById(R.id.txtThisWeek);
		tvThisWeek.setText(R.string.ThisWeek);
		tvThisWeek.append(" (" + itemsThisWeek.size() + ")");*/
		bindListView(itemsThisWeek,adapterThisWeek,R.id.listThisWeek,R.id.txtThisWeek,R.string.ThisWeek
				, icl,longClickListener);
		
		itemsLater = dbHelper.getAllItemsByDaysLeft(7,-1);
		//ListView laterList = (ListView) findViewById(R.id.listLater);		
		adapterLater = new ReminderItemsArrayAdapter(this,resId,itemsLater);
		/*laterList.setAdapter(adapterLater);
		laterList.setOnItemClickListener(icl);
		laterList.setOnItemLongClickListener(longClickListener);
		TextView tvLater = (TextView) findViewById(R.id.txtLater);
		tvLater.setText(R.string.Later);
		tvLater.append(" (" + itemsLater.size() + ")");*/
		bindListView(itemsLater,adapterLater,R.id.listLater,R.id.txtLater,R.string.Later
				, icl,longClickListener);
	}
	/**
	 * Helper method to bind list views
	 * @param items
	 * @param adapter
	 * @param listViewResourceId
	 * @param textViewResourceId
	 * @param textVewStringResourceId
	 * @param clickListener
	 * @param longClickListener
	 */
	private void bindListView(List<Item> items, ListAdapter adapter, int listViewResourceId, int textViewResourceId
			, int textVewStringResourceId,ItemClickListener clickListener, ItemLongClickListener longClickListener){
		ListView lv = (ListView) findViewById(listViewResourceId);		
		lv.setAdapter(adapter);		
		lv.setOnItemClickListener(clickListener);
		lv.setOnItemLongClickListener(longClickListener);
		TextView tv = (TextView) findViewById(textViewResourceId);
		tv.setText(textVewStringResourceId);
		tv.append(" (" + items.size() + ")");
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.reminder, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getTitle().equals("Items")){
			Intent intent = new Intent(ReminderActivity.this, ItemsMaster.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
		}else if(item.getItemId() == R.id.action_settings){
	    	Intent settingsIntent = new Intent(ReminderActivity.this, SettingsActivity.class);
	    	settingsIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    	startActivity(settingsIntent);
	    }
		return true;
	}
	/**
	 * Helper method to update days left of an item in database based on quantity bought and buy date.
	 * Takes care of updating current stock just in case ItemUpdateService has not run as scheduled
	 * @param adapter 
	 * @param selectedItem
	 * @param buyDate
	 * @param quantity
	 */
	private void updateDaysLeft(ArrayAdapter<Item> adapter, Item selectedItem, Date buyDate, float quantityBought) {
		//By default usage duration is for one default quantity. 
		//If user buys more than that then only quantity bought comes in picture.
		int daysLeft = (int) (quantityBought * selectedItem.getRemindDays());
		Date today = new Date();
		//Assume that buy date is always in past
		if(buyDate != null){
			long timeDiff = Math.abs(today.getTime() - buyDate.getTime());
			long days = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
			daysLeft = (int) (daysLeft - days);			
		}
		//Before using current stock ensure that is is updated - 
		// this is to prevent edge case when update service has not run for many days
		Util.updateCurrentStock(getApplicationContext(), selectedItem);
		selectedItem.setDaysLeft(selectedItem.getDaysLeft() + daysLeft);
		selectedItem.setLastUpdatedAt(today);
		dbHelper.updateItem(selectedItem);
		//adapter.notifyDataSetChanged();

	}
	/**
	 * Handles Click Events on the Items Due List
	 * Single click means user has bought 1 quantity today
	 * Refreshes view at the end if updated
	 * @author Jigar Patel
	 */
	class ItemClickListener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> parent, View rowView, int position,long id) {
			@SuppressWarnings("unchecked")
			ArrayAdapter<Item> adapter = (ArrayAdapter<Item>) parent.getAdapter();
			Item selectedItem = adapter.getItem(position);
			//update the daysLeft by 1 day as by default it is assumed that user has bought today and usual quantity.
			updateDaysLeft(adapter,selectedItem,null,1);
			init();
		}	
	}
	/**
	 * Handles Long Click Events on the Items Due List
	 * Shows Edit shopping details form and updates item accordingly
	 * Refreshes view at the end if updated
	 * @author Jigar Patel
	 */
	class ItemLongClickListener implements OnItemLongClickListener{
		@SuppressWarnings("unchecked")
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View rowView, final int position,long id) {
			selectedAdapter = (ArrayAdapter<Item>) parent.getAdapter();
			selectedItem = selectedAdapter.getItem(position);
			//Show the dialog 
			final Dialog d =new Dialog(parent.getContext());
			d.setTitle("Edit Shopping Details");
			d.setContentView(R.layout.edit_shopping_details);
			d.setCancelable(true);
			final EditText txtQuantity = (EditText) d.findViewById(R.id.txtQuantity);
			txtQuantity.setText("1");
			final DatePicker dpBuyDate = (DatePicker) d.findViewById(R.id.dpBuyDate);
			final Calendar cal = Calendar.getInstance();
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			dpBuyDate.init(year, month, day, null);
			//Handle Cancel button click
			Button btnCancel = (Button) d.findViewById(R.id.btnCancel);
			btnCancel.setOnClickListener(new View.OnClickListener(){
				public void onClick(View v){
					d.dismiss();
				}
			});
			//Handle save button click
			Button btnSave = (Button) d.findViewById(R.id.btnSaveItem);		  		
			btnSave.setOnClickListener(new View.OnClickListener() {		  
				public void onClick(View v) 	{	  
					float itemQuantity = Float.parseFloat(txtQuantity.getText().toString());
					int day = dpBuyDate.getDayOfMonth();
					int month = dpBuyDate.getMonth();
					int year = dpBuyDate.getYear();
					cal.set(year, month, day);
					updateDaysLeft(selectedAdapter,selectedItem,cal.getTime(),itemQuantity);					
					d.dismiss();
					init();
				}		 
			});		 
			d.show();
			return true;
		}
	}
}
