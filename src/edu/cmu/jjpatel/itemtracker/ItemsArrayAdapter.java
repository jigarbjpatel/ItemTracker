package edu.cmu.jjpatel.itemtracker;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
/**
 * Custom Array Adapter to bind list of Item objects to ListView
 * This is used by Items Master Activity (Item Usage)
 * @author Jigar Patel
 */
public class ItemsArrayAdapter extends ArrayAdapter<Item>{
	private Context context;
	private List<Item> items;
	int resource;
	public ItemsArrayAdapter(Context context,int resource, List<Item> list){
		super(context,R.layout.items_row_layout,list);
		this.items = list;
		this.resource = resource;
		this.context = context;
	}
	@Override
	public View  getView (int position, View convertView, ViewGroup parent) {
	    if (convertView == null) {
	        //This a new view, hence we inflate the new layout
	        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        convertView = inflater.inflate(resource, parent, false);
	    }
		Item i = items.get(position);
		TextView itemName = (TextView) convertView.findViewById(R.id.itemName);
		TextView remindDays = (TextView) convertView.findViewById(R.id.itemRemindDays);
		itemName.setText(i.getName());
		remindDays.setText(String.valueOf(i.getRemindDays()));
		//convertView.setTag(i.getId());
		return convertView;
	}
}
