package com.elevator;

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
	
	public void getInElevator(Elevator  vator) {
		state=STATE.INELEVATOR;
		vator.addInPerson(this);
	}
	
	public void outElevator(Elevator  vator) {
		this.state=STATE.OUTELEVATOR;
		vator.outPerson(this);
	}
	
	public void setTargetFloor(Elevator  vator) {
		vator.addStopFloor(this.targetPosition);
	}
	
	public Person(Floor  currentPosition,Floor  targetPosition) {
		this.currentPosition=currentPosition;
		this.targetPosition=targetPosition;
		if(currentPosition.floorPostion>targetPosition.floorPostion)
			runState=RUNNING.UP;
		else if(currentPosition.floorPostion<targetPosition.floorPostion)
			runState=RUNNING.DOWN;
		else 
			System.out.println("同一层楼不做处理");
		currentPosition.addToquen(this);
	}
	
	public RUNNING getRunState() {
		return this.runState;
	}
}
