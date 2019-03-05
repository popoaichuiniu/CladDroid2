package com.popoaichuiniu.experiment;

import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.WriteFile;
import org.apache.log4j.Logger;


public class SEUnHandleProcessStatistic {


    private String appPath="";

    private long handledCount =0;

    private long unHandleCount=0;


    public static Logger exceptionLogger=new MyLogger(Config.statisticDir,"exceptionLogger").getLogger();
    private static WriteFile writeFileResult=new WriteFile(Config.statisticDir+"/SEUnHandleProcessStatistic_result.csv",false,exceptionLogger);


    static {
        writeFileResult.writeStr("appPath,handledCount,allCount,handledCount/allCount\n");
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

        if(handledCount !=0&& unHandleCount!=0)
        {
            writeFileResult.writeStr(appPath+","+ handledCount +","+(unHandleCount+handledCount)+","+((double)handledCount)/ (handledCount+unHandleCount) +"\n");

        }

        writeFileResult.flush();

    }




}
