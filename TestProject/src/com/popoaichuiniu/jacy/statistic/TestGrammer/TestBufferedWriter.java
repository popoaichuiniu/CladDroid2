package com.popoaichuiniu.jacy.statistic.TestGrammer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class TestBufferedWriter {

    public static void main(String[] args) {
        try {
            BufferedWriter bufferedWriter =new BufferedWriter(new FileWriter("/home/lab418/bufferedwriter/tt.txt",false));
            bufferedWriter.write("xxx");
            bufferedWriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


    }
}
