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
        WriteFile writeFileResult=new WriteFile(Config.statisticDir+"/SEUnHandleProcessStatistic_result.csv",true,exceptionLogger);
        if(handledCount !=0&& unHandleCount!=0)
        {
            writeFileResult.writeStr(appPath+","+ handledCount +","+(unHandleCount+handledCount)+","+((double)handledCount)/ (handledCount+unHandleCount) +"\n");

        }

        writeFileResult.close();

    }


}
