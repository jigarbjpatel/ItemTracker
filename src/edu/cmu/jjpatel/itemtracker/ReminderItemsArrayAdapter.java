package edu.cmu.jjpatel.itemtracker;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ReminderItemsArrayAdapter extends ArrayAdapter<Item>{
	private Context context;
	private List<Item> items;
	int resource;
	public ReminderItemsArrayAdapter(Context context,int resource, List<Item> list){
		super(context,R.layout.reminder_items_row_layout,list);
		this.items = list;
		this.resource = resource;
		this.context = context;
	}
	@Override
	public View  getView (int position, View convertView, ViewGroup parent) {
	    //First time the convertView will be null afterwards system uses cache
		if (convertView == null) {
	        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        convertView = inflater.inflate(resource, parent, false);
	    }
		Item i = items.get(position);
		TextView itemName = (TextView) convertView.findViewById(R.id.itemName);
		TextView daysLeft = (TextView) convertView.findViewById(R.id.itemDaysLeft);
		//CheckBox chkItem = (CheckBox) convertView.findViewById(R.id.chkItem);
		itemName.setText(i.getName());
		daysLeft.setText(String.valueOf(i.getDaysLeft()));
		//chkItem.setOnCheckedChangeListener((ReminderActivity)context);
		//convertView.setTag(i.getId());
		return convertView;
	}
}
