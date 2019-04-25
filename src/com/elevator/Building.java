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
	private static void initQueue(){
		for(int i=0;i<7;i++)
			queue.add(null);
	}

	public static volatile Boolean isWork=true;

	public static class Floor{

		Floor(Integer floorPostion){
			this.floorPostion=floorPostion;
		}

		// 重写equal
		private String uuid=UUID.randomUUID().toString();
		// 楼层数字
		public final Integer floorPostion;
		// 楼层等电梯的人
		public final List<Person> persons=new LinkedList();
		public static final Integer TOP=6;
		public static final Integer BOTTOM=-1;
		// 上楼请求
		public Boolean upRequest=false;

		// 下楼请求
		public Boolean downRequest=false;

		// 通知这个楼层的人电梯到了 可以进了
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

		// 坐电梯请求
		public void sendRequest(Person person) {
			System.out.println("开始处理请求");
			if(downRequest&&upRequest)
				return ;
			if(person.getRunState()!=null) {
				if(person.getRunState()==RUNNING.UP&&!upRequest)
					upRequest=true;
				else
					downRequest=true;
				System.out.println("加入请求队列");
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
			System.out.println("-----------开始equals 方法");
			if(person instanceof Floor) {
				System.out.println("-----------开始equals 方法   "+this.uuid.equals(((Floor)person).getUuid()));
				return this.uuid.equals(((Floor)person).getUuid());
			}
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
	private static  void initElevators(){
		elevators.add(left);
		// elevators.add(middle);
		//elevators.add(right);
	}


	private static List<Floor>  floors=new ArrayList();
	private static void initfloors(){
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

	public static void initElevatorsStart() {
		for(Elevator itn:elevators)
			itn.start();
		System.out.println("");
	}

	public static void schdule() {
		System.out.println("开始 运行");
		initQueue();
		initfloors();
		initElevators();
		initElevatorsStart();
		new Thread(()->{
			for(int i=0;isWork;i=(i+1)%7) {
				Floor todeal=queue.get(i);
				if(todeal==null)
					continue;
				System.out.println(String.format("收到去往 %d 楼的请求  index为：%d",todeal.floorPostion,i));
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
			System.out.println("暂时没有可以处理该请求的电梯");
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
		if((target.running.code&RUNNING.UP.code)>0) {
			todeal.upRequest=false;
			if(!todeal.downRequest)
				queue.set(todeal.floorPostion+1,null);
		}
		if((target.running.code&RUNNING.DOWN.code)>0) {
			todeal.downRequest=false;
			if(!todeal.upRequest)
				queue.set(todeal.floorPostion+1,null); }
		if(target.running.code==RUNNING.STILL.code) {
			if(!todeal.downRequest||!todeal.upRequest) {
				todeal.downRequest=false;
				todeal.upRequest=false;
				queue.set(todeal.floorPostion, null) ; }
			target.addStopFloor(todeal); }

		target.addStopFloor(todeal);
		System.out.println(String.format("电梯：%s 已经受理该请求", target.name));
	}



	private static List<Elevator> findTheCandidate(Floor todeal) {
		List<Elevator> candidate =new LinkedList();
		// 上楼请求
		if(todeal.upRequest) {
			for(Elevator ele:elevators) {
				if((ele.running.code&RUNNING.UP.code)>0) {
					System.out.println("电梯目前为 上楼状态 可以处理上楼请求");
					if(ele.currentFloor.floorPostion>todeal.floorPostion)
						continue;
					else if(ele.currentFloor.floorPostion<todeal.floorPostion)
						candidate.add(ele);
					else if((ele.running.code&RUNNING.OPENDOOR.code)>0) {
						if(!todeal.downRequest) {
							queue.set(todeal.floorPostion+1,null);
						}
						todeal.upRequest=false;
						todeal.informPerson(ele); //这里可能会发送额外的请求
						candidate.clear();
						return candidate;
					}
				}
				else if(ele.running.code==RUNNING.STILL.code) {
					System.out.println("电梯目前为 静止状态 可以处理上楼请求");
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
				if((ele.running.code&RUNNING.DOWN.code)>0) {
					System.out.println("电梯目前为 下楼状态 可以处理下楼请求");
					if(ele.currentFloor.floorPostion<todeal.floorPostion)
						continue;
					else if(ele.currentFloor.floorPostion>todeal.floorPostion)
						candidate.add(ele);
					else if((ele.running.code&RUNNING.OPENDOOR.code)>0) {
						if(!todeal.upRequest) {
							queue.set(todeal.floorPostion+1,null);
						}
						todeal.downRequest=false;
						todeal.informPerson(ele); //这里可能会发送额外的请求
						candidate.clear();
						return candidate;
					}
				}
				else if(ele.running.code==RUNNING.STILL.code) {
					System.out.println("电梯目前为 静止状态 可以处理下楼请求");
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
}
