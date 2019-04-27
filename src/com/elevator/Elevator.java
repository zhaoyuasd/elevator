package com.elevator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class Elevator extends Thread{

	public enum RUNNING{
		//4       16         0         2               8
		UP, DOWN, STILL;/* ,OPENDOOR(1<<1),CLOSEDOOR(1<<3); */
	}
	
	
	public Object lockDirection=new Object();
	
	// 当前的运行状态
    public  RUNNING currentRunningDirection=RUNNING.STILL;
    
    // 当前接收指令后的运行方向 
    // 下一个要处理的请求是上楼还是下楼  目前可能是静止
    private RUNNING currentOrderDirection=RUNNING.STILL;
    
    public void setCurrentOrderDirection(RUNNING currentOrderDirection) {
    	synchronized (lockDirection) {
			this.currentOrderDirection=currentOrderDirection;
		}
    }
    
    public RUNNING getCurrentOrderDirection() {
    	synchronized (lockDirection) {
			return this.currentOrderDirection;
		}
    }
    



	// 实际的运行方向 只包方向
    private Integer  runDrirection=0;
	/* private BlockingQueue<Floor> toStop= new ArrayBlockingQueue(100); */
	 
	public Integer getRunDrirection() {
		return runDrirection;
	}


	private  volatile List<Floor>  toStop=new ArrayList(7);
	/*
	 * { for(int i=0;i<7;i++) toStop.add(null); }
	 */
	
	public  Floor currentFloor;
	
	public final String name;
	
	private final List<Person> persons=new ArrayList(3);
	
	private final Integer Max=3;
	
	private Integer currentSize=0;
	
	private String getWork="%s ：我在%d楼  接到了 去%d楼的请求";
	private String outPerson="%s ：在%d楼下楼去了";
	private String inPerson="%s ： 在%d楼进电梯了";
	private String netxstop="%s :下一个要停的楼层是 %d楼层   目前处于 %d楼";
	
	private volatile  Floor nextToStop;
	
	public Boolean addInPerson(Person person) {
		if(currentSize<Max) {
			 persons.add(person);
			 currentSize+=1;
		System.out.println(String.format(inPerson,name,currentFloor.floorPostion));		 
		return true;
		}
		return false;
	}

	public Boolean couldGetIn() {
		return currentSize<persons.size();
	}
	
	public void outPerson(Person person) {
		// TODO Auto-generated method stub
		 persons.remove(person);
		 currentSize-=1;
	}

	// 这个方法会产生 ConcurrentModificationException 异常
	// 产生原因是 人写入自己的要去的楼层 后进行排序
	public  void addStopFloor(Floor targetPosition) {
		synchronized (toStop) {
		//if(!toStop.contains(targetPosition))
		// 在电梯为still的时候临时指定运行的方向
		// 这里指定一下方向 不然所有请求 看到电梯的状态都是静止的
		toStop.add(targetPosition);
		System.out.println(String.format(name+" :以受理 去往%d楼层的请求  目前状态 ：%d", targetPosition.floorPostion,this.running.code) );
		if(getRunning()==0) {
			//这俩个条件 并不能解决本层楼向上的请求
			if(targetPosition.floorPostion>currentFloor.floorPostion) {
				runDrirection=RUNNING.UP.code;
				setRunning(runDrirection);
				//this.running.code=runDrirection;
				System.out.println(" 从静止改为上楼状态");
			}
			else if(targetPosition.floorPostion<currentFloor.floorPostion) {
				runDrirection=RUNNING.DOWN.code;
				setRunning(runDrirection);
				//this.running.code=runDrirection;
				System.out.println(" 从静止改为下楼状态");
			}
		}
		//if(toStop.size()>=2) {
			if((running.code&RUNNING.UP.code)>0) {
				toStop.sort((a,b)->{return a.floorPostion-b.floorPostion;});
			}else if((running.code&RUNNING.DOWN.code)>0) {
				toStop.sort((a,b)->{return b.floorPostion-a.floorPostion;});
			} 
			else {
				toStop.sort((a,b)->{return a.floorPostion-b.floorPostion;});
			}
		//}
		  System.out.println(String.format(netxstop,name,toStop.get(0).floorPostion,currentFloor.floorPostion));
		}
	}
    
	
	private Floor getnextStop() {
		//synchronized(toStop)  {
		if(toStop.size()==0) return null;
		return toStop.get(0);
		//}
	}
	
	private void done() {
		toStop.remove(0);
	}
	
	private void done(Floor currentFloor) {
		synchronized(toStop)  {
			toStop.remove(currentFloor);
		}
	}
	
	public void run() {
		if(this.running.code==RUNNING.STILL.code) {
			while(1==1) {
			if(getnextStop()!=null) {
				if(getnextStop().floorPostion>currentFloor.floorPostion) {
					runDrirection=RUNNING.UP.code;
					setRunning(runDrirection);
					//this.running.code=runDrirection;
					System.out.println(String.format(getWork,name,currentFloor.floorPostion,getnextStop().floorPostion)+" "+this.running.code);
					System.out.println("准备上楼 "+this.running.code);
					work();
					continue;
				}
				else if(getnextStop().floorPostion<currentFloor.floorPostion) {
					runDrirection=RUNNING.DOWN.code;
					setRunning(runDrirection);
					//this.running.code=runDrirection;
					System.out.println(String.format(getWork,name,currentFloor.floorPostion,getnextStop().floorPostion)+" "+this.running.code);
					System.out.println("准备下楼 "+this.running.code);
					work();
					continue;
				}
				else {
					System.out.println(name+":在本层 有请求 直接开门处理");
					setRunning(RUNNING.OPENDOOR.code);
					 //this.running.code=RUNNING.OPENDOOR.code;
					 currentFloor.informPerson(this);
					 done(currentFloor);
					 setRunning(RUNNING.CLOSEDOOR.code);
					 //this.running.code=RUNNING.CLOSEDOOR.code;
				}
			}
			 //System.out.println("没有活干 原地待命");
			//runCosumTime();
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
		if((getRunning()&RUNNING.UP.code)>0)
			nextFloor=nextFloor+1;
		else if((getRunning()&RUNNING.DOWN.code)>0)
			nextFloor=nextFloor-1;
		runCosumTime();
		currentFloor=Building.getFloorByNum(nextFloor);
		System.out.println(String.format(netxstop,name,getnextStop().floorPostion,currentFloor.floorPostion));
		
		if(getnextStop().floorPostion==currentFloor.floorPostion) {
			done(currentFloor); 
			setRunning(runDrirection|RUNNING.OPENDOOR.code);
			//this.running.code=runDrirection|RUNNING.OPENDOOR.code;
			informPerson(); //出
			currentFloor.informPerson(this); //进
			// 到打一个楼层后 要重新检查 运行状态 是否可以和原来的保持一致
			// 人进入电梯后 获取下一个要去的楼层  如果与原来的运行状态一致 那么继续 如果不一致 那么直接跳出
			//this.running.code=runDrirection|RUNNING.CLOSEDOOR.code;
			setRunning(runDrirection|RUNNING.CLOSEDOOR.code);
			if(shouldChangeDirection())
				break;
		  }
		
		if(shouldChangeDirection())
			break;
		}
		
		//this.running=RUNNING.STILL; 这么写不起作用  
		
		this.running.code=0;
		System.out.println(name+" "+this.running.code);
	}

	// 到打一层后 检查要去的下一层 是否 符合当前的运行方向
	private boolean shouldChangeDirection() {
	  if(getnextStop()!=null) {	
			if(currentFloor.floorPostion<getnextStop().floorPostion && (this.running.code&RUNNING.UP.code)>0 )
			  return false;
			if(currentFloor.floorPostion>getnextStop().floorPostion && (this.running.code&RUNNING.DOWN.code)>0 )
			 return false;
		  }
		System.out.println(name+": 即将改变运行方向");
		return true;
	}

	public void informPerson() {
		 for(int i=0;i<persons.size();i++){
			 if(persons.get(i)!=null)
				if(persons.get(i).shouldOutElevator(this))
					System.out.println(String.format(outPerson,name,currentFloor.floorPostion));
		 }
		 //persons.removeIf(person->{return person.getState()==STATE.INELEVATOR;});
	}
	
	
	Elevator(Floor currentFloor,String name){
		this.currentFloor=currentFloor;
		this.running=RUNNING.STILL;
		this.name=name;
	}
	
	
}
