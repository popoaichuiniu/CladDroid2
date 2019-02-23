package com.popoaichuiniu.jacy.statistic.TestJava;

public class TestInstance {

    public static void main(String[] args) {

        Object a=null;

        String b=new String("c");

        if(a instanceof String)

        {
            System.out.println("zzz");
        }

        System.out.println(b.equals(a));
    }
}
