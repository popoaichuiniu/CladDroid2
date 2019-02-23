package com.popoaichuiniu.jacy.statistic;

public class B extends A{
	
	
	public static void main(String[] args) {
		A t =new B();
		
		System.out.println(t.getClass().getSimpleName());
	}

	int a()
	{
		return 1;
	}
	int  a(int a)
	{
		return 'c';
	}

}
