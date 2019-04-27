package com.elevator;

public interface Building {
	/*
	  *  抽取共有属性 方法
	 */
 public void sendRequest(Floor request);
 public final static Floor BottomFloor=new Floor(-1);
 public final static Floor ZEROFloor=new Floor(0);
 public final static Floor ONEFloor=new Floor(1);
 public final static Floor TWOFloor=new Floor(2);
 public final static Floor THREEFloor=new Floor(3);
 public final static Floor FOURFloor=new Floor(4);
 public final static Floor FIVEFloor=new Floor(5);
 
 public static Floor getFloorByNum(Integer num){
	switch(num) {
	case -1 : return BottomFloor;
	case 0 : return ZEROFloor;
	case 1 : return ONEFloor;
	case 2 : return TWOFloor;
	case 3 : return THREEFloor;
	case 4 : return FOURFloor;
	case 5 : return FIVEFloor;
	default:
		return ZEROFloor; 
	}
  }
}
