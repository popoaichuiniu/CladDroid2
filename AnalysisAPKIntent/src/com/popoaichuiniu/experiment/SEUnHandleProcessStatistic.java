package com.popoaichuiniu.experiment;

import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.WriteFile;
import org.apache.log4j.Logger;

import java.io.File;


public class SEUnHandleProcessStatistic {


    private String appPath="";

    private long handledCount =0;

    private long unHandleCount=0;


    public static Logger exceptionLogger=new MyLogger(Config.statisticDir,"exceptionLogger").getLogger();
    private static WriteFile writeFileResult=null;


    static {

        File file=new File(Config.statisticDir+"/SEUnHandleProcessStatistic_result.csv");
        if(file.exists())
        {
            writeFileResult=new WriteFile(Config.statisticDir+"/SEUnHandleProcessStatistic_result.csv",true,exceptionLogger);
        }
        else
        {
            writeFileResult=new WriteFile(Config.statisticDir+"/SEUnHandleProcessStatistic_result.csv",false,exceptionLogger);
            writeFileResult.writeStr("appPath,handledCount,allCount,handledCount/allCount\n");
        }


    }


    public static void closeFile()
    {
        if(writeFileResult!=null)
        {
            writeFileResult.close();
        }
    }
    public SEUnHandleProcessStatistic(String appPath) {
        this.appPath = appPath;
    }

    public void addOKHandledCount()
    {
        handledCount++;
    }


    public  void addUnHandleCount()
    {
        unHandleCount++;
    }

    public void saveData()
    {

        long allCount=handledCount+unHandleCount;
        if(allCount!=0)
        {
            writeFileResult.writeStr(appPath+","+ handledCount +","+allCount+","+((double)handledCount)/ allCount +"\n");

        }


        writeFileResult.flush();

    }




}
