package com.elevator;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.elevator.Building.Floor;

public class Elevator {

	public enum RUNNING{
		UP,DOWN,STILL;
	}
	
	 public RUNNING running;
	 private BlockingQueue<Floor>  toStop= new ArrayBlockingQueue(100);
	
	
	public Floor currentFloor;
	
	public List<Person> persons=new LinkedList();
	
	public Integer Max=3;
	
	public void addInPerson(Person person) {
		if(persons.size()<Max)
			persons.add(person);
		 Max=persons.size();
	}

	public void outPerson(Person person) {
		// TODO Auto-generated method stub
		 persons.remove(person);
		 Max=persons.size();
	}

	public void addStopFloor(Floor targetPosition) {
		toStop.add(targetPosition);
	}

	
	
	public void dealRequest(Floor request) {
		toStop.add(request);
	}
	
	Elevator(Floor currentFloor){
		this.currentFloor=currentFloor;
		this.running=RUNNING.STILL;
	}
}
