package com.popoaichuiniu.util;

import org.apache.log4j.Logger;

public class UnHandleWriter {

    private static final int MAX_COUNT=100;
    private static int count=0;
    private static Logger logger=new MyLogger(Config.intentConditionSymbolicExcutationResults+"/"+"unhandledWriter","exceptionLogger").getLogger();
    private static WriteFile writeFile=new WriteFile(Config.intentConditionSymbolicExcutationResults+"/"+"unhandledWriter"+"/"+"unhandledSituation.txt",false,logger);
    public static void write(String message) {
        writeFile.writeStr(message);
        count++;
        if(count>MAX_COUNT)
        {
            writeFile.flush();
            count=0;
        }

    }

    public static  void closeFile()
    {
        writeFile.close();
    }


}
