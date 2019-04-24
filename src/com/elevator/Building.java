package com.elevator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.elevator.Elevator.RUNNING;
import com.elevator.Person.STATE;

public class Building {
	
	/*
	 * public static final BlockingQueue<Floor> queue= new ArrayBlockingQueue(100);
	 */
	
	public static final  List<Floor>  queue=new ArrayList(7);
	
	public static volatile Boolean isWork=true;
	
  public static class Floor{
	  Floor(Integer floorPostion){
		  this.floorPostion=floorPostion;
	  }
	 
	 private String uuid=UUID.randomUUID().toString();
	  
	 public final Integer floorPostion;
	 public final List<Person> persons=new LinkedList(); 
	 public static final Integer TOP=6;
	 public static final Integer BOTTOM=-1;
	 public Boolean upRequest=false;
	 public Boolean downRequest=false;
	 public void informPerson(Elevator ele) {
		 List<Person>  enter=new LinkedList(); 
		 for(Person itm:persons) {
			 if(itm.shouldEnterElevator(ele))
				 enter.add(itm);
		 }
		 //persons.removeIf(person->{return person.getState()==STATE.INELEVATOR;});
		 persons.removeAll(enter);
	 }
	 
	 public void addWaitPerson(Person person) {
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
			 
		  if(queue.get(this.floorPostion+1)==null)	 
			    queue.set(this.floorPostion+1, this);  //按照序号存放
			
			 // queue.sort((f1,f2)->{return f1.floorPostion-f2.floorPostion;});  //
		 }
	 }
	 
		public String getUuid() {
			return uuid;
		}
		
	    @Override
		public boolean equals(Object person) {
		    if(person instanceof Floor)	
			   return this.uuid.equals(((Floor)person).getUuid());
		    
		    return false;
		}
  }
  
  public static Floor BottomFloor=new Floor(-1);
  public static Floor ZEROFloor=new Floor(0);
  public static Floor ONEFloor=new Floor(1);
  public static Floor TWOFloor=new Floor(2);
  public static Floor THREEFloor=new Floor(3);
  public static Floor FOURFloor=new Floor(4);
  public static Floor FIVEFloor=new Floor(5);
 
  public static Elevator left=new Elevator(BottomFloor,"left");
  public static Elevator middle=new Elevator(TWOFloor,"middle");
  public static Elevator right=new Elevator(FIVEFloor,"right");
  
  private static List<Elevator> elevators =new ArrayList();
  {
	  elevators.add(left);
	  elevators.add(middle);
	  elevators.add(right);
  }
     
  
  private static List<Floor>  floors=new ArrayList();
  {
	  floors.add(BottomFloor);
	  floors.add(ZEROFloor);
	  floors.add(ONEFloor);
	  floors.add(TWOFloor);
	  floors.add(THREEFloor);
	  floors.add(FOURFloor);
	  floors.add(FIVEFloor);
	  
  }
  
  public static Floor getFloorByNum(Integer num){
	if(num<Floor.BOTTOM)  num=Floor.BOTTOM;
	if(num>Floor.TOP)num=Floor.TOP;
	return floors.get(num+1);
  }
  
  
   public static void schdule() {
		for(int i=0;isWork;i=(i+1)%7) {
			Floor todeal=queue.get(i);
			if(todeal==null)	
				continue;
			dispatch(todeal);
			
		}
   }
    //分派
	private static void dispatch(Floor todeal) {
		List<Elevator> list= findTheCandidate(todeal);
		handleRequest(list,todeal);
	}
	private static void handleRequest(List<Elevator> list, Floor todeal) {
		 if(list==null||list.size()==0)
			 return ;
		 Elevator target=null;
		 Integer distance=10;
		 for(Elevator itm:list) {
			 int dis=Math.abs(itm.currentFloor.floorPostion-todeal.floorPostion);
			 if(dis<distance) {
				 distance=dis;
				 target=itm;
			 }
		 }
		
		 if((target.running.code&RUNNING.UP.code)>0) {
			 todeal.upRequest=false;
			 if(!todeal.downRequest)
				 queue.remove(todeal.floorPostion+1);
		 }
		 if((target.running.code&RUNNING.DOWN.code)>0) {
			 todeal.downRequest=false;
			 if(!todeal.upRequest)
				 queue.remove(todeal.floorPostion+1);
		 } 
		    target.addStopFloor(todeal);
	}
	
	
	
	private static List<Elevator> findTheCandidate(Floor todeal) {
		List<Elevator> candidate =new LinkedList();
		// 上楼请求
		if(todeal.upRequest) {
			for(Elevator ele:elevators) {
				if((ele.running.code&RUNNING.UP.code)>0) {
					if(ele.currentFloor.floorPostion>todeal.floorPostion)
						continue;
					else if(ele.currentFloor.floorPostion<todeal.floorPostion)
						candidate.add(ele);
					else if((ele.running.code&RUNNING.OPENDOOR.code)>0) {
						todeal.informPerson(ele);
						todeal.upRequest=false;
						if(!todeal.downRequest) {
							queue.remove(todeal.floorPostion+1);
						}
						candidate.clear();
						return candidate;
					}
				}
				else if(ele.running==RUNNING.STILL)
					candidate.add(ele);
			}
		}
		else  if(todeal.downRequest) {
			for(Elevator ele:elevators) {
				if((ele.running.code&RUNNING.DOWN.code)>0) {
					if(ele.currentFloor.floorPostion<todeal.floorPostion)
						continue;
					else if(ele.currentFloor.floorPostion>todeal.floorPostion)
						candidate.add(ele);
					else if((ele.running.code&RUNNING.OPENDOOR.code)>0) {
						todeal.informPerson(ele);
						todeal.downRequest=false;
						if(!todeal.upRequest) {
							queue.remove(todeal.floorPostion+1);
						}
						candidate.clear();
						return candidate;
					}
				}
				else if(ele.running==RUNNING.STILL)
					candidate.add(ele);
			}
		
		}
	    return candidate;
	}
	
	public static void main(String[] args) {
		List<Integer> list=new LinkedList();
		list.add(6);
		list.add(7);
		list.add(3);
		List<Integer> list2=new LinkedList();
		list2.add(7);
		//list.removeIf(i->{return i<4;});
		list.removeAll(list2);
		System.out.println(list);
	}
}
