package com.popoaichuiniu.jacy.statistic.statistic;

import java.io.*;

public class findAPPHasUnitNeedAnalysis {

    public static void main(String[] args) {

        File dir=new File("/media/lab418/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/down_fdroid_app_from_androzoo/f-droid-app");

        BufferedWriter bufferedWriter=null;
        try {

            bufferedWriter = new BufferedWriter(new FileWriter("F-droid_aPPHasUnitToAnalysisList.txt"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        BufferedWriter bufferedWriter_Unit=null;
        try {

            bufferedWriter_Unit = new BufferedWriter(new FileWriter("F-droid_UnitsToAnalysisList.txt"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        int count =0;//有Vun的app数量
        int count2 =0;//vun数量
        System.out.println("APP数量:"+dir.listFiles().length);

        for(File file: dir.listFiles())
        {
            if(file.getName().contains("UnitsNeedAnalysis"))
            {
                try{
                    BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
                    String content=null;

                    boolean flag=true;
                    while(((content=bufferedReader.readLine())!=null))
                    {
                        String []str=content.split("#");
                        bufferedWriter_Unit.write(str[2]+"\n");


                        if(flag) {
                            bufferedWriter.write(file.getName() + "\n");
                            count = count + 1;
                            flag=false;
                        }
                        count2=count2+1;
                    }
                    bufferedReader.close();
                }
                catch (IOException io)
                {

                }
            }


        }
        try {
            bufferedWriter.close();
            bufferedWriter_Unit.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        System.out.println("有unitNeedAnalysisAPP数量:"+count);
        System.out.println("unitNeedAnalysis数量:"+count2);
    }
}
