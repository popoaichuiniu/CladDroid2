package com.popoaichuiniu.jacy.statistic.TestJava;


class Student{
    int age;

}

public class Test2 {

    public static void main(String []args)
    {
        Student s=new Student();
        s.age=5;
        xxx(s);
        System.out.println(s.age);
    }

    private static void xxx(Student s) {

        s=new Student();
        s.age=7;
    }
}
