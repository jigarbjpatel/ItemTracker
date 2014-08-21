package edu.cmu.jjpatel.itemtracker;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
/**
 * Helper class to access SQLite DB
 * @author Jigar Patel
 */
public class DatabaseHelper extends SQLiteOpenHelper{
	private static final String DATABASE_NAME = "items.db";
	private static final int DATABASE_VERSION = 2;
	private static final String ITEM_TABLE = "itemsTable";

	public DatabaseHelper(Context context, CursorFactory factory) {
		super(context, DATABASE_NAME, factory, DATABASE_VERSION);
	}

	/**
	 * SQL statement to create a new database and table
	 */
	private static final String DATABASE_CREATE = "create table " + ITEM_TABLE + 
			" (id integer primary key autoincrement, name text not null, remindDays integer, " +
			"	daysLeft integer, lastUpdatedAt date);";

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	// Called when there is a database version mismatch, meaning that the version
	// of the database on disk needs to be upgraded to the current version.
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + ITEM_TABLE);
		onCreate(db);
	}
	/**
	 * Adds new Item to database
	 * @param i Item to add
	 */
	public void addItem(Item i){
		Date today = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);    	
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("name", i.getName());
		values.put("remindDays", i.getRemindDays());
		values.put("daysLeft",i.getDaysLeft());
		values.put("lastUpdatedAt",sdf.format(today));
		db.insert(ITEM_TABLE, null, values);
		db.close();
	}
	/**
	 * Gets item by id
	 * @param id item id
	 * @return Item if exists else null
	 */
	Item getItem(int id){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(ITEM_TABLE, 
				new String[] { "id","name", "remindDays","daysLeft","lastUpdatedAt"}, "id=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		Item i = null;
		if (cursor != null){
			cursor.moveToFirst();
			i = new Item(Integer.parseInt(cursor.getString(0)),
					cursor.getString(1), 
					Integer.parseInt(cursor.getString(2)),
					Integer.parseInt(cursor.getString(3)));
			try{
				i.setLastUpdatedAt(new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH).parse(cursor.getString(4)));
			}catch(ParseException ex){
				String errorMsg = ex.getMessage() != null ? ex.getMessage() : "Null Pointer Exception";
				Log.e("getItem",errorMsg);
			}
			cursor.close();
		}
		db.close();
		return i;
	}
	/**
	 * Gets all items with all the fields from database
	 * @return List of Item objects
	 */
	public List<Item> getAllItemsWithAllFields() {
		List<Item> itemList = new ArrayList<Item>();
		String selectQuery = "SELECT  id,name,remindDays,daysLeft,lastUpdatedAt FROM " + ITEM_TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				Item i = new Item(Integer.parseInt(cursor.getString(0)),
						cursor.getString(1), 
						Integer.parseInt(cursor.getString(2)),
						Integer.parseInt(cursor.getString(3)));
				try{
					i.setLastUpdatedAt(
							new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH).parse(cursor.getString(4)));
				}catch(ParseException ex){
					String errorMsg = ex.getMessage() != null ? ex.getMessage() : "Null Pointer Exception";
					Log.e("getAllItemsWithAllFields",errorMsg);
				}
				itemList.add(i);
			} while (cursor.moveToNext());
			cursor.close();
		}
		db.close();
		return itemList;
	}
	/**
	 * Updates Item object with new values
	 * @param i New Item object
	 * @return Number of rows affected
	 */
	public int updateItem(Item i) {
		SQLiteDatabase db = this.getWritableDatabase(); 
		ContentValues values = new ContentValues();
		values.put("name", i.getName());
		values.put("remindDays", i.getRemindDays());
		values.put("daysLeft", i.getDaysLeft());
		values.put("lastUpdatedAt", 
				new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH).format(i.getLastUpdatedAt()));
		int numberOfRowsAffected = db.update(ITEM_TABLE, values, "id = ?",
				new String[] { String.valueOf(i.getId()) });
		db.close();
		return numberOfRowsAffected;
	}
	/**
	 * Delets the Item based on id
	 * @param itemId id of Item to delete
	 */
	public void deleteItem(int itemId) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(ITEM_TABLE, "id = ?",
				new String[] { String.valueOf(itemId) });
		db.close();
	}
	/**
	 * Gets all the items whose daysLeft is between given from and to days
	 * @param daysLeftFrom Starting value of daysLeft range
	 * @param daysLeftTo Ending value of daysLeft range
	 * @return List of Item objects
	 */
	public List<Item> getAllItemsByDaysLeft(int daysLeftFrom,int daysLeftTo) {
		List<Item> itemList = new ArrayList<Item>();
		int remindDays = 0;
		int daysLeft = 0;

		String selectQuery = "SELECT  id,name,remindDays,daysLeft,lastUpdatedAt FROM " + ITEM_TABLE;
		if(daysLeftFrom == -1 && daysLeftTo == 0)
			selectQuery += " WHERE daysLeft <= 0";          		
		else if(daysLeftFrom != -1 && daysLeftTo == -1)
			selectQuery += " WHERE daysLeft >= "+daysLeftFrom;  
		else if(daysLeftFrom != -1 && daysLeftTo != -1)
			selectQuery += " WHERE daysLeft >= "+daysLeftFrom+ " and daysLeft <= " + daysLeftTo;
		selectQuery += " ORDER BY daysLeft";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {            	
				if(cursor.getString(2) != null)
					remindDays = Integer.parseInt(cursor.getString(2));
				if(cursor.getString(3) != null)
					daysLeft = Integer.parseInt(cursor.getString(3));
				Item i = new Item(Integer.parseInt(cursor.getString(0)),
						cursor.getString(1), remindDays, daysLeft);
				try{
					i.setLastUpdatedAt(new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH).parse(cursor.getString(4)));
				}catch(ParseException ex){
					String errorMsg = ex.getMessage() != null ? ex.getMessage() : "Null Pointer Exception";
					Log.e("getAllItemsWithAllFields",errorMsg);
				}
				itemList.add(i);
			} while (cursor.moveToNext());
			cursor.close();
		}
		db.close();
		return itemList;
	}

	/* public int getItemsCount() {
        String countQuery = "SELECT  * FROM " + ITEM_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        return cursor.getCount();
    }*/
}
