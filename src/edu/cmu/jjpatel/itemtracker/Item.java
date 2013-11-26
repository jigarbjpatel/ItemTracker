package edu.cmu.jjpatel.itemtracker;

import java.util.Date;
/**
 * Main Model for the application.
 * Models Item and related information
 * @author Jigar Patel
 */
public class Item {
	private String name;
	private int remindDays;
	private int id;
	private int daysLeft;
	private Date lastUpdatedAt;
	public Item(){
	}
	public Item(int id, String name){
		this.id= id;
		this.name = name;
	}
	public Item(int id, String name, int remindDays){
		this.id = id;
		this.name = name;
		this.remindDays = remindDays;
	}
	public Item(int id, String name, int remindDays, int daysLeft){
		this.id = id;
		this.name = name;
		this.remindDays = remindDays;
		this.daysLeft = daysLeft;
	}
	/**
	 * Item name
	 * @return name of item
	 */
	public String getName(){
		return name;
	}
	/**
	 * Usage Duration in days - days in which an item with default quantity gets over.
	 * @return usage duration or remind days 
	 */
	public int getRemindDays(){
		return remindDays;
	} 
	/**
	 * Number of days left before the item gets over
	 * It is adjusted based on quantity bought
	 * @return number of days left
	 */
	public int getDaysLeft(){
		return this.daysLeft;
	}
	/**
	 * Sets the name of the item
	 * @param name item name
	 */
	public void setName(String name){
		this.name = name;
	}
	/**
	 * Sets the usage duration also called remind days
	 * @param remindDays Number of days an item with default quantity will last 
	 */
	public void setRemindDays(int remindDays){
		this.remindDays	= remindDays;
	}
	/**
	 * Sets number of days left for before which the item goes out of stock
	 * @param daysLeft Number of days left
	 */
	public void setDaysLeft(int daysLeft){
		this.daysLeft = daysLeft;
	}
	/**
	 * Returns the unique identifier of the item
	 * @return Item Id
	 */
	public int getId() {
		return id;
	}
	/**
	 * Returns the date when the item was last updated by 
	 * ItemUpdateService or when user buys item or updates item usage. 
	 * @return Last Updated date - it is not record last update date
	 */
	public Date getLastUpdatedAt() {
		return this.lastUpdatedAt;
	}
	/**
	 * It indicates the date when the item's daysLeft attribute was last updated.
	 * Used to correctly reflect current stock value even when Update service does not run as expected.
	 * @param value
	 */
	public void setLastUpdatedAt(Date value) {
		this.lastUpdatedAt = value;
	}

}
