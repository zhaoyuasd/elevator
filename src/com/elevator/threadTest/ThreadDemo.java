package com.elevator.threadTest;

public class ThreadDemo extends Thread{
   public volatile Integer state=0;
   public static class Status{
	   public Integer status=0;
  }
   public volatile  Status status=new Status();
   
   public void run() {
	   while(true) {
		   state=state+1;
		   status.status=state;
		   System.out.println();
	   }
   }
}
