package com.popoaichuiniu.jacy.statistic.TestJava;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class Data
{
   public int x=6;

    public int y=7;

    public Data(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return x == data.x &&
                y == data.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
public class TestSet {

    public static void main(String[] args) {
        Set<Data> set=new HashSet<>();

        Data data1=new Data(3,4);

        Data data2=new Data(3,5);

        set.add(data1);
        set.add(data2);

        System.out.println(set.size());

        data2.y=4;

        System.out.println(set.size());

    }
}
