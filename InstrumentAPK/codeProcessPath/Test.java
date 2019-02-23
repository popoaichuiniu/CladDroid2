//package com.popoaichuiniu.jacy;
public class Test {
	
	
	private int y=0;
	
	
	public  Test(int y) {
		this.y=y;
		
	}
	
	
   public static void main(String[] args) {
	   
	   Test test=new Test(10);
	   if(test.y>5)
	   {
		   test.y=test.xxx(test.y);
	   }
	   else
	   {
		   test.y=test.zzz(test.y);
	   }
	
	   
	   
}
   
   
   public int   xxx(int x)
   {
	   x=x+5;
	   return x;
   }
   public int   zzz(int x)
   {
	   x=x+5;
	   return x;
   }
}
