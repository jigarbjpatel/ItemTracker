package edu.cmu.jjpatel.itemtracker;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
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
	
	
}
