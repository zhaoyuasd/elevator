package com.elevator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.elevator.Building.Floor;

public class Elevator {

	public enum RUNNING{
		UP(1<<2),DOWN(1<<4),STILL(0),OPENDOOR(1<<1),CLOSEDOOR(1<<3);
		public  Integer code;
		RUNNING(Integer code){
			this.code=code;
		}
	}
	
    public RUNNING running;
	/* private BlockingQueue<Floor> toStop= new ArrayBlockingQueue(100); */
	 
	private  final  List<Floor>  toStop=new ArrayList(7);
	
	public  Floor currentFloor;
	
	public final String name;
	
	private final List<Person> persons=new ArrayList(3);
	
	private final Integer Max=3;
	
	private Integer currentSize=0;
	
	private volatile  Floor nextToStop;
	
	public Boolean addInPerson(Person person) {
		if(currentSize<Max) {
			 persons.add(person);
			 currentSize+=1;
		     return true;
		}
		return false;
	}

	public void outPerson(Person person) {
		// TODO Auto-generated method stub
		 persons.remove(person);
		 currentSize-=1;
	}

	public void addStopFloor(Floor targetPosition) {
		toStop.add(targetPosition);
		if((running.code&RUNNING.UP.code)>0) {
			toStop.sort((a,b)->{return a.floorPostion-b.floorPostion;});
		}else if((running.code&RUNNING.DOWN.code)>0) {
			toStop.sort((a,b)->{return b.floorPostion-a.floorPostion;});
		} 
		else {
			toStop.sort((a,b)->{return a.floorPostion-b.floorPostion;});
		}
	}
    
	private Floor getnextStop() {
		return toStop.get(0);
	}
	
	private void done() {
		toStop.remove(0);
	}
	
	private void done(Floor currentFloor) {
		toStop.remove(currentFloor);
	}
	
	public void run() {
		if(this.running==RUNNING.STILL) {
			while(1==1) {
			if(getnextStop()!=null) {
				if(getnextStop().floorPostion>currentFloor.floorPostion) {
					this.running.code=this.running.code|RUNNING.UP.code;
					work();
					continue;
				}
				else if(getnextStop().floorPostion<currentFloor.floorPostion) {
					this.running.code=this.running.code|RUNNING.DOWN.code;
					work();
					continue;
				}
				else {
					 this.running=RUNNING.OPENDOOR;
					 currentFloor.informPerson(this);
					 done(currentFloor);
					 this.running=RUNNING.CLOSEDOOR;
				}
			}
			runCosumTime();
		 }
		}
	}
	
	// 模拟花费的时间
	public void runCosumTime() {
		try {
			Thread.currentThread().sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	// 这里沿着一个方向走 最后停止
	private void work() {
		while(getnextStop()!=null) {
		Integer nextFloor=currentFloor.floorPostion;
		if((this.running.code&RUNNING.UP.code)>0)
			nextFloor=nextFloor+1;
		else if((this.running.code&RUNNING.DOWN.code)>0)
			nextFloor=nextFloor-1;
		runCosumTime();
		currentFloor=Building.getFloorByNum(nextFloor);
		if(getnextStop().floorPostion==currentFloor.floorPostion) {
			this.running.code=this.running.code|RUNNING.OPENDOOR.code;
			informPerson(); //出
			currentFloor.informPerson(this); //进
			done(currentFloor);
			this.running.code=this.running.code|RUNNING.CLOSEDOOR.code;
		  }
		}
		this.running=RUNNING.STILL;
	}

	public void informPerson() {
		 for(int i=0;i<persons.size();i++){
			 if(persons.get(i)!=null)
				 persons.get(i).shouldOutElevator(this);
		 }
		 //persons.removeIf(person->{return person.getState()==STATE.INELEVATOR;});
	}
	
	
	Elevator(Floor currentFloor,String name){
		this.currentFloor=currentFloor;
		this.running=RUNNING.STILL;
		this.name=name;
	}
}
