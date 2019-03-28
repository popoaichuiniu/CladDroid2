package com.popoaichuiniu.jacy.statistic;

public class A {
    public static void main(String[] args) {

        System.out.println(strContainsTwoPoint(".java.,,."));

    }

    private static boolean strContainsTwoPoint(String str) {
        int count = 0;
        int index = -1;
        while (true) {


            index = str.indexOf(".", index + 1);


            if (index != -1) {
                count++;
            }
            else
            {
                break;
            }
        }

        if (count >= 2) {
            return true;
        } else {
            return false;
        }


    }

}
