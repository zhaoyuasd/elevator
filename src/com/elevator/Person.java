package com.elevator;

import java.util.UUID;

import com.elevator.Building.Floor;
import com.elevator.Elevator.RUNNING;

public class Person {
	public enum STATE{
		INELEVATOR,
		OUTELEVATOR;
	}		
	private Floor  currentPosition;
	
	private Floor  targetPosition;
	
	private STATE  state;
	
	private RUNNING runState;
	
	private String uuid=UUID.randomUUID().toString();
	
	private void getInElevator(Elevator  vator) {
		state=STATE.INELEVATOR;
		vator.addInPerson(this);
	}
	
	private void outElevator(Elevator  vator) {
		this.state=STATE.OUTELEVATOR;
		vator.outPerson(this);
	}
	
	private void setTargetFloor(Elevator  vator) {
		vator.addStopFloor(this.targetPosition);
	}
	
	public Boolean shouldEnterElevator(Elevator  vator) {
		if((vator.running.code&runState.code)>0||vator.running==RUNNING.STILL) {
			getInElevator(vator);
			setTargetFloor(vator);
			return true;
		}
		return false;
	}
	
	public Boolean shouldOutElevator(Elevator  vator) {
		if(vator.currentFloor.floorPostion==this.targetPosition.floorPostion)
		{
			outElevator(vator);
			return true;
		}
		return false;
	}
	
	
	public Person(Floor  currentPosition,Floor  targetPosition) {
		this.currentPosition=currentPosition;
		this.targetPosition=targetPosition;
		if(currentPosition.floorPostion>targetPosition.floorPostion)
			runState=RUNNING.UP;
		else if(currentPosition.floorPostion<targetPosition.floorPostion)
			runState=RUNNING.DOWN;
		else {
			System.out.println("同一层楼不做处理");
			return;
		}
		currentPosition.addWaitPerson(this);
	}
	
	@Override
	public boolean equals(Object person) {
	    if(person instanceof Person)	
		   return this.uuid.equals(((Person)person).getUuid());
	    
	    return false;
	}
	
	public RUNNING getRunState() {
		return this.runState;
	}

	public String getUuid() {
		return uuid;
	}

	public STATE getState() {
		return state;
	}
	
	
}
