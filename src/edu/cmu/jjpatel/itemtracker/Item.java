package edu.cmu.jjpatel.itemtracker;

import java.util.Date;

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
	public String getName(){
		return name;
	}
	public int getRemindDays(){
		return remindDays;
	} 
	public int getDaysLeft(){
		return this.daysLeft;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setRemindDays(int remindDays){
		this.remindDays	= remindDays;
	}
	public void setDaysLeft(int daysLeft){
		this.daysLeft = daysLeft;
	}
	public int getId() {
		return id;
	}
	public Date getLastUpdatedAt() {
		return this.lastUpdatedAt;
	}
	public void setLastUpdatedAt(Date value) {
		this.lastUpdatedAt = value;
	}
	
}
