package com.popoaichuiniu.jacy.statistic.TestJava;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReadLogThreadByTail extends  Thread{


    private String logPath = null;

    private Process process=null;

    public ReadLogThreadByTail(String logPath) {
        this.logPath = logPath;
    }


    @Override
    public void run() {
        while (true)
        {
            File logFile=new File(logPath);
            if(logFile.exists())
            {
                break;
            }
        }
        String[] command = {"tail", "-f", logPath};
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File("."));
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            this.process=process;
            Thread childThread = new Thread(new Runnable() {//must start thread to read process output
                @Override
                public void run() {


                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = null;

                    try {
                        while ((line = bufferedReader.readLine()) != null) {
                            System.out.print(line + "\n");

                        }
                        bufferedReader.close();

                    } catch (IOException e) {


                    }



                }
            });
            childThread.start();
            int status = process.waitFor();//

        } catch (IOException e) {

        }
        catch (InterruptedException e)
        {

        }


    }

    public void stopProcessTail()//
    {


        try{
            Thread.sleep(5000);//在等待10秒，将日志读完
        }

        catch (InterruptedException e)
        {

        }

        process.destroy();


    }

    public static void main(String[] args) {

    }
}
