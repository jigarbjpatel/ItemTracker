package edu.cmu.jjpatel.itemtracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.Dialog;
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
import android.widget.ListView;

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
	protected void onStart() {
		super.onStart();
		init();
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reminder);
		init();
		//registerForContextMenu(todayList);
	}
	private void init(){
		//bind to different lists
		int resId = R.layout.reminder_items_row_layout;
		ItemClickListener icl = new ItemClickListener();
		ItemLongClickListener longClickListener = new ItemLongClickListener();
		dbHelper = new DatabaseHelper(this,null);
		itemsToday = new ArrayList<Item>();
		for(Item i : dbHelper.getAllItemsByDaysLeft(0,0))
			itemsToday.add(i);

		ListView todayList = (ListView) findViewById(R.id.listToday);		
		adapterToday = new ReminderItemsArrayAdapter(this,resId,itemsToday);
		todayList.setAdapter(adapterToday);		
		todayList.setOnItemClickListener(icl);
		todayList.setOnItemLongClickListener(longClickListener);

		itemsThisWeek = new ArrayList<Item>();
		for(Item i : dbHelper.getAllItemsByDaysLeft(1,6))
			itemsThisWeek.add(i);
		ListView thisWeekList = (ListView) findViewById(R.id.listThisWeek);		
		adapterThisWeek = new ReminderItemsArrayAdapter(this,resId,itemsThisWeek);
		thisWeekList.setAdapter(adapterThisWeek);
		thisWeekList.setOnItemClickListener(icl);
		thisWeekList.setOnItemLongClickListener(longClickListener);
		
		itemsLater = new ArrayList<Item>();
		for(Item i : dbHelper.getAllItemsByDaysLeft(7,-1))
			itemsLater.add(i);
		ListView laterList = (ListView) findViewById(R.id.listLater);		
		adapterLater = new ReminderItemsArrayAdapter(this,resId,itemsLater);
		laterList.setAdapter(adapterLater);
		laterList.setOnItemClickListener(icl);
		laterList.setOnItemLongClickListener(longClickListener);
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
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	private void updateDaysLeft(ArrayAdapter<Item> adapter, Item selectedItem, Date buyDate, int quantity) {
		int daysLeft = quantity * selectedItem.getRemindDays();
		Date today = new Date();
		//Assume that buy date is always in past
		if(buyDate != null){
			long timeDiff = Math.abs(today.getTime() - buyDate.getTime());
			long days = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
			if(days!=0){
				daysLeft = (int) (daysLeft - days);
			}
		}
		selectedItem.setDaysLeft(selectedItem.getDaysLeft() + daysLeft);
		selectedItem.setLastUpdatedAt(today);
		dbHelper.updateItem(selectedItem);
		//adapter.notifyDataSetChanged();

	}
	class ItemClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View rowView, int position,long id) {
			ArrayAdapter<Item> adapter = (ArrayAdapter<Item>) parent.getAdapter();
			Item selectedItem = adapter.getItem(position);
			//update the daysLeft
			updateDaysLeft(adapter,selectedItem,null,1);
			init();
		}	
	}
	class ItemLongClickListener implements OnItemLongClickListener{

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View rowView, final int position,long id) {
			selectedAdapter = (ArrayAdapter<Item>) parent.getAdapter();
			selectedItem = adapterToday.getItem(position);
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
			Button b = (Button) d.findViewById(R.id.btnSaveItem);		  		
			b.setOnClickListener(new View.OnClickListener() {		  
				public void onClick(View v) 	{	  
					int itemQuantity = Integer.parseInt(txtQuantity.getText().toString());
					int day = dpBuyDate.getDayOfMonth();
					int month = dpBuyDate.getMonth();
					int year = dpBuyDate.getYear();
					cal.set(year, month, day);
					updateDaysLeft(selectedAdapter,selectedItem,cal.getTime(),itemQuantity);
					/*int daysLeft = 0;
					selectedItem.setDaysLeft(daysLeft);
					itemsToday.set(position, selectedItem);
					dbHelper.updateItem(selectedItem);
					selectedAdapter.notifyDataSetChanged();*/
					d.dismiss();
					init();
				}		 
			});		 
			d.show();
			return true;
		}

	}
	/*@Override
	public void onCheckedChanged(CompoundButton chkItem, boolean isChecked) {
		//TODO: 
	}	*/
}
