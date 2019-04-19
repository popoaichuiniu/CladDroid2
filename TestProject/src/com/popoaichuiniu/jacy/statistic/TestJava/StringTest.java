package com.popoaichuiniu.jacy.statistic.TestJava;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StringTest {
    public static void main(String[] args) {
        String ss="\"";
        Map<String,String> map=new HashMap<>();
        String t=map.get(ss);
        try {
            BufferedReader bufferedReader=new BufferedReader(new FileReader("/media/mobile/myExperiment/idea_ApkIntentAnalysis/TestProject/src/com/popoaichuiniu/jacy/statistic/TestJava/file"));
            String content=bufferedReader.readLine();
            System.out.println(content);

            t=map.get(content);
            System.out.println("success");
        }
        catch (IOException e)
        {

        }


    }
}
