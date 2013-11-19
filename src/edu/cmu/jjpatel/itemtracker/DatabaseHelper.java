package edu.cmu.jjpatel.itemtracker;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
public class DatabaseHelper extends SQLiteOpenHelper{
	private static final String DATABASE_NAME = "items.db";
    private static final int DATABASE_VERSION = 1;
    private static final String ITEM_TABLE = "itemsTable";
    
    public DatabaseHelper(Context context, CursorFactory factory) {
      super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    // SQL statement to create a new database.
    private static final String DATABASE_CREATE = "create table " + ITEM_TABLE + 
      " (id integer primary key autoincrement, name text not null, remindDays integer, daysLeft integer);";

    // Called when no database exists in disk and the helper class needs
    // to create a new one.
    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(DATABASE_CREATE);
    }

    // Called when there is a database version mismatch, meaning that the version
    // of the database on disk needs to be upgraded to the current version.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      // Log the version upgrade.
      Log.w("TaskDBAdapter", "Upgrading from version " +
                             oldVersion + " to " +
                             newVersion + ", which will destroy all old data");

      // Upgrade the existing database to conform to the new version. Multiple
      // previous versions can be handled by comparing oldVersion and newVersion
      // values.

      // The simplest case is to drop the old table and create a new one.
      db.execSQL("DROP TABLE IF IT EXISTS " + ITEM_TABLE);
      // Create a new one.
      onCreate(db);
    }
    
    void addItem(Item i){
    	SQLiteDatabase db = this.getWritableDatabase();
    	ContentValues values = new ContentValues();
    	values.put("name", i.getName());
    	values.put("remindDays", i.getRemindDays());
    	values.put("daysLeft",i.getDaysLeft());
    	db.insert(ITEM_TABLE, null, values);
    	db.close();
    }
    
    Item getItem(int id){
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor cursor = db.query(ITEM_TABLE, new String[] { "id","name", "remindDays"}, "id=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
 
        Item i = new Item(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), Integer.parseInt(cursor.getString(2)));
        return i;
    }
    public List<Item> getAllItems() {
        List<Item> itemList = new ArrayList<Item>();
        String selectQuery = "SELECT  id,name,remindDays FROM " + ITEM_TABLE;
 
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        if (cursor.moveToFirst()) {
            do {
            	Item i = new Item(Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1), Integer.parseInt(cursor.getString(2)));
            	itemList.add(i);
            } while (cursor.moveToNext());
        }
        return itemList;
    }
    
    public int updateItem(Item i) {
        SQLiteDatabase db = this.getWritableDatabase(); 
        ContentValues values = new ContentValues();
        values.put("name", i.getName());
        values.put("remindDays", i.getRemindDays());
        values.put("daysLeft", i.getDaysLeft());
        return db.update(ITEM_TABLE, values, "id = ?",
                new String[] { String.valueOf(i.getId()) });
    }
 
    public void deleteItem(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ITEM_TABLE, "id = ?",
                new String[] { String.valueOf(itemId) });
        db.close();
    }

	public List<Item> getAllItemsByDaysLeft(int daysLeftFrom,int daysLeftTo) {
		List<Item> itemList = new ArrayList<Item>();
		int remindDays = 0;
        int daysLeft = 0;
        
		String selectQuery = "SELECT  id,name,remindDays,daysLeft FROM " + ITEM_TABLE;
        if(daysLeftTo != -1)
        		selectQuery += " WHERE daysLeft >= "+daysLeftFrom+ " and daysLeft <= " + daysLeftTo;        		
       	else
        	selectQuery += " WHERE daysLeft >= "+daysLeftFrom;        
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
            	itemList.add(i);
            } while (cursor.moveToNext());
        }
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
