package com.popoaichuiniu.jacy.statistic.TestJava;

import java.io.IOException;

public class TestException {


    public static void main(String [] args)
    {

        try
        {
            int x=7/0;
        }
       catch (Exception io)
       {
           System.out.println(io);
       }

       System.out.println("yyy");


    }
}
