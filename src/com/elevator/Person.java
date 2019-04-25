package com.elevator;

import java.util.UUID;

import com.elevator.Building.Floor;
import com.elevator.Elevator.RUNNING;

public class Person {
	public enum STATE{
		INELEVATOR,
		OUTELEVATOR;
	}	
	// 所在楼层
	private Floor  currentPosition;
	
	// 目标楼层
	private Floor  targetPosition;
	
	// 状态
	private STATE  state;
	
	// 运行状态
	private RUNNING runState;
	
	// 比较一致
	private String uuid=UUID.randomUUID().toString();
	
	// 进入电梯
	private Boolean getInElevator(Elevator  vator) {
		if(vator.addInPerson(this)) {
			state=STATE.INELEVATOR;
			return true;
		}
		return false;
	}
	
	// 出电梯
	private void outElevator(Elevator  vator) {
		this.state=STATE.OUTELEVATOR;
		vator.outPerson(this);
	}
	
	private void setTargetFloor(Elevator  vator) {
		vator.addStopFloor(this.targetPosition);
	}
	
	
	// 尝试进入电梯
	public Boolean shouldEnterElevator(Elevator  vator) {
		if((vator.getRunning()&runState.code)>0||vator.getRunning()==RUNNING.STILL.code) {
		
			//满员未上 在按一次电梯
			if(!getInElevator(vator))
			{
				currentPosition.sendRequest(this);
			}
			setTargetFloor(vator);
			return true;
		}
		return false;
	}
	
	
	// 尝试走出电梯
	public Boolean shouldOutElevator(Elevator  vator) {
		if(vator.currentFloor.floorPostion==this.targetPosition.floorPostion)
		{
			outElevator(vator);
			return true;
		}
		return false;
	}
	
	
	// 初始化
	public Person(int  currentPosition,int  targetPosition) {
		this.currentPosition=Building.getFloorByNum(currentPosition);
		this.targetPosition=Building.getFloorByNum(targetPosition);
		if(currentPosition<targetPosition)
			runState=RUNNING.UP;
		else if(currentPosition>targetPosition)
			runState=RUNNING.DOWN;
		else {
			System.out.println("同一层楼不做处理");
			return;
		}
		this.currentPosition.addWaitPerson(this);
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
