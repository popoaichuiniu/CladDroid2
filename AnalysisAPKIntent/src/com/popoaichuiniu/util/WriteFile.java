package com.popoaichuiniu.util;



import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class WriteFile {

    private String fileName=null;

    private BufferedWriter bufferedWriter=null;

    private Logger logger=null;

    public WriteFile(String fileName,boolean append,Logger logger) {
        this.fileName = fileName;
        this.logger=logger;
        File parentDir=new File(fileName).getParentFile();
        if(!parentDir.exists())
        {
            parentDir.mkdirs();//bufferedWriter不能创建父目录,会报file not found错误
        }
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(fileName, append));//
        }
        catch (IOException e)
        {
            logger.error(e.getMessage()+"##"+ExceptionStackMessageUtil.getStackTrace(e));
        }
    }
    public void writeStr(String str)
    {
        try {
            bufferedWriter.write(str);
        } catch (IOException e) {
            logger.error(e.getMessage()+"##"+ExceptionStackMessageUtil.getStackTrace(e));
        }
    }

    public  void  flush()
    {
        try {
            bufferedWriter.flush();
        } catch (IOException e) {
            logger.error(e.getMessage()+"##"+ExceptionStackMessageUtil.getStackTrace(e));
        }
    }

    public void close()
    {
        try {
            bufferedWriter.close();
        } catch (IOException e) {
            logger.error(e.getMessage()+"##"+ExceptionStackMessageUtil.getStackTrace(e));
        }
    }
}
