package com.popoaichuiniu.jacy.statistic.TestJava;

public class TestSplit {

    public static void main(String [] args)
    {
        String xx="sss\nvvv";
        String []tt=xx.split("\n");
        for(String str:tt)
        {
            System.out.println(str);
        }

    }
}
