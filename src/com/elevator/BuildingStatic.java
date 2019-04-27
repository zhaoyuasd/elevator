package com.elevator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.elevator.Elevator.RUNNING;
import com.elevator.Person.STATE;

public class BuildingStatic implements Building{
	
	/*
	 * public static final BlockingQueue<Floor> queue= new ArrayBlockingQueue(100);
	 */
	
//	public static final  List<Floor>  upQueue=new ArrayList(7);
//	public static final  List<Floor>  downQueue=new ArrayList(7);
	public static final  List<Floor>  queue=new ArrayList(7);
	private static void initQueue(){
		for(int i=0;i<7;i++)
			queue.add(null);
	}
	
	public static volatile Boolean isWork=true;
	
 
  
 
  public static Elevator left=new Elevator(BottomFloor,"left");
  public static Elevator middle=new Elevator(TWOFloor,"middle");
  public static Elevator right=new Elevator(FIVEFloor,"right");
  
  private static List<Elevator> elevators =new ArrayList();
  private static  void initElevators(){
	  elevators.add(left);
	 // elevators.add(middle);
	  //elevators.add(right);
  }
     
  
 
 
  
 
  
  public static void initElevatorsStart() {
	  for(Elevator itn:elevators) {
		  itn.start();
	      System.out.println(itn.name+"  電梯已運行");
	  }
  }
  
   public static void schdule() {
	   System.out.println("开始 运行");
	   initQueue();
	   initElevators();
	   initElevatorsStart();
//	   new Thread(()->{
//			while(true) {
//			    if(upQueue.size()>0)
//			    for(Floor todeal:upQueue )
//			         dispatch(todeal);
//			}
//	   }).start();
//	   
//	   new Thread(()->{
//				while(true) {
//				    if(upQueue.size()>0)
//				    for(Floor todeal:downQueue )
//				         dispatch(todeal);
//				}
//		   }).start();
//	   
	   new Thread(()->{
			while(true) {
			    if(queue.size()>0)
			    for(Floor todeal:queue )
			         dispatch(todeal);
			}
	   }).start();
	   
   }
    //分派
	private static void dispatch(Floor todeal) {
		List<Elevator> list= findTheCandidate(todeal);
		handleRequest(list,todeal);
	}
	private static void handleRequest(List<Elevator> list, Floor todeal) {
		 if(list==null||list.size()==0) {
			// System.out.println("暂时没有可以处理该请求的电梯");
			 return ;
			 }
		 Elevator target=null;
		 Integer distance=10;
		 for(Elevator itm:list) {
			 int dis=Math.abs(itm.currentFloor.floorPostion-todeal.floorPostion);
			 if(dis<distance) {
				 distance=dis;
				 target=itm;
			 }
		 }
		
		 // 這裡處理完后 要把請求從列表裡面清楚 避免重複發送
		  if((target.getRunning()&RUNNING.UP.code)>0) {
			  todeal.upRequest=false;
		     if(!todeal.downRequest) 
		    	 queue.set(todeal.floorPostion+1,null); 
		     }
		  if((target.getRunning()&RUNNING.DOWN.code)>0) {
			  todeal.downRequest=false;
		     if(!todeal.upRequest) 
		    	 queue.set(todeal.floorPostion+1,null); }
		  if(target.getRunning()==RUNNING.STILL.code) {
		          if(!todeal.downRequest||!todeal.upRequest) {
		        	  todeal.downRequest=false;
					  todeal.upRequest=false; 
					  queue.set(todeal.floorPostion, null) ; 
					  }
			}
		  target.addStopFloor(todeal);
		  System.out.println(String.format("电梯：%s 已经受理该请求", target.name));
	}
	
	
	
	private static List<Elevator> findTheCandidate(Floor todeal) {
		List<Elevator> candidate =new LinkedList();
		// 上楼请求
		if(todeal.upRequest) {
			for(Elevator ele:elevators) {
				if((ele.getRunning()&RUNNING.UP.code)>0) {
					System.out.println(ele.name+": 电梯目前为 上楼状态 可以处理上楼请求");
					if(ele.currentFloor.floorPostion>todeal.floorPostion)
						continue;
					else if(ele.currentFloor.floorPostion<todeal.floorPostion)
						candidate.add(ele);
					else if((ele.getRunning()&RUNNING.OPENDOOR.code)>0) {
						if(!todeal.downRequest) {
							queue.set(todeal.floorPostion+1,null);
						}
						todeal.upRequest=false;
						todeal.informPerson(ele); //这里可能会发送额外的请求
						candidate.clear();
						return candidate;
					}
				}
				else if(ele.getRunning()==RUNNING.STILL.code) {
					System.out.println(ele.name+":电梯目前为 asd静止状态 可以处理上楼请求");
					if(!todeal.downRequest) {
						queue.set(todeal.floorPostion+1,null);
					}
					todeal.upRequest=false;
					candidate.add(ele);
				}
			}
		}
		else  if(todeal.downRequest) {
			for(Elevator ele:elevators) {
				System.out.println("处理下楼请求");
				if((ele.getRunning()&RUNNING.DOWN.code)>0) {
					System.out.println(ele.name+": 电梯目前为 下楼状态 可以处理下楼请求");
					if(ele.currentFloor.floorPostion<todeal.floorPostion)
						continue;
					else if(ele.currentFloor.floorPostion>todeal.floorPostion)
						candidate.add(ele);
					else if((ele.getRunning()&RUNNING.OPENDOOR.code)>0) {
						if(!todeal.upRequest) {
							queue.set(todeal.floorPostion+1,null);
						}
						todeal.downRequest=false;
						todeal.informPerson(ele); //这里可能会发送额外的请求
						candidate.clear();
						return candidate;
					}
				}
				else if(ele.getRunning()==RUNNING.STILL.code) {
					System.out.println(ele.name+": 电梯目前为 静止状态 可以处理下楼请求");
					if(!todeal.upRequest) {
						queue.set(todeal.floorPostion+1,null);
					}
					todeal.downRequest=false;
					candidate.add(ele);
				}
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

	@Override
	public void sendRequest(Floor request) {
		if(!queue.contains(request))
			queue.add(request);
	}
}
