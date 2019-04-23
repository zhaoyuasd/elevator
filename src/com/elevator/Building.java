package com.elevator;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.elevator.Elevator.RUNNING;

public class Building {
	
 public static  BlockingQueue<Floor>  queue= new ArrayBlockingQueue(100);
	
  public static class Floor{
	  Floor(Integer floorPostion){
		  this.floorPostion=floorPostion;
	  }
	 public final Integer floorPostion;
	 public List<Person> persons=new LinkedList(); 
	 public static final Integer TOP=6;
	 public static final Integer BOTTOM=-1;
	 public Boolean upRequest=false;
	 public Boolean downRequest=false;
	 public void informPerson() {
	    
	 }
	 
	 public void addToquen(Person person) {
		 persons.add(person);
		 sendRequest(person);
	 }
	 
	 private void sendRequest(Person person) {
		 if(downRequest&&upRequest)
			 return ;
		 if(person.getRunState()!=null) {
			 if(person.getRunState()==RUNNING.UP&&!upRequest) 
				 upRequest=true;
			 else
				 downRequest=true;
			 queue.add(this) ;
			 
		 }
	 }
  }
  
  public static Floor BottomFloor=new Floor(-1);
  public static Floor ZEROFloor=new Floor(0);
  public static Floor ONEFloor=new Floor(1);
  public static Floor TWOFloor=new Floor(2);
  public static Floor THREEFloor=new Floor(3);
  public static Floor FOURFloor=new Floor(4);
  public static Floor FIVEFloor=new Floor(5);
 
  
  
}
