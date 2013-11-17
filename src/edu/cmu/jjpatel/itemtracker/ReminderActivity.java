package edu.cmu.jjpatel.itemtracker;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ReminderActivity extends Activity {
	DatabaseHelper dbHelper;
	List<Item> items;
	ArrayAdapter<Item> adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reminder);
		
		ListView todayList = (ListView) findViewById(R.id.listToday);
		dbHelper = new DatabaseHelper(this,null);
		items = new ArrayList<Item>();
		for(Item i : dbHelper.getAllItemsByDaysLeft()){
			items.add(i);
		}
		int resId = R.layout.items_row_layout;
		adapter = new ItemsArrayAdapter(this,resId,items);
		todayList.setAdapter(adapter);
		
		//registerForContextMenu(todayList);
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
}
