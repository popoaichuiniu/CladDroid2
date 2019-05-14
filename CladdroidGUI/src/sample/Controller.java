package sample;

import com.popoaichuiniu.intentGen.GenerateUnitNeedInstrumentLog;
import com.popoaichuiniu.intentGen.GenerateUnitNeedToAnalysis;
import com.popoaichuiniu.intentGen.IntentConditionTransformSymbolicExcutation;
import com.popoaichuiniu.util.*;
import com.zhou.ApkSigner;
import com.zhou.InstrumentAPPBeforePermissionInvoke;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.util.List;

import static com.popoaichuiniu.util.Util.exeCmd;


class AnalysisStatus{//boolean变量无法在子线程中拷贝
    private boolean isStart = false;

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
    }
}
public class Controller {

    @FXML
    private Label filePathLabel;

    @FXML
    private TextArea progressTextArea;

    @FXML
    private TextArea logTextArea;


    private String chooseFilePath = null;

    private boolean isEvironmentCorrect = false;

    private AnalysisStatus analysisStatus=new AnalysisStatus();

    private static Logger exceptionLogger = new MyLogger(Config.cladDroidLogDir, "exception").getLogger();

    private static Logger infoLogger = new MyLogger(Config.cladDroidLogDir, "info").getLogger();


    @FXML
    private void initialize()
    {

        logTextArea.setWrapText(true);
    }

    @FXML
    private void detection() {
        isEvironmentCorrect = true;
        JOptionPane.showMessageDialog(null, "Your runtime environment is correct!");
    }

    @FXML
    private void openApkOrDir() {
        String filePath = getFilePath();
        if (filePath != null) {
            chooseFilePath = filePath;
            filePathLabel.setText("File Path:  " + filePath);

        }

    }

    private String getFilePath() {
        String filePath = null;
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FileFilter filter = new FileFilter() {

            public boolean accept(File file) {

                if (file.isDirectory()) {
                    return true;
                } else {
                    if (file.getName().endsWith(".apk")) {
                        return true;
                    }
                }


                return false;
            }

            @Override
            public String getDescription() {
                return "apk & apk dir";
            }
        };

        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println("You chose to open this file: " +
                    chooser.getSelectedFile().getName());
            filePath = chooser.getSelectedFile().getAbsolutePath();
        }


        if (filePath == null || filePath.trim().equals("")) {
            return null;
        }


        System.out.println(filePath);
        return filePath;
    }

    @FXML
    private void startAnalyse() {
        if (chooseFilePath == null) {
            JOptionPane.showMessageDialog(null, "You must choose file firstly!", "warning", JOptionPane.WARNING_MESSAGE);
        } else {


            if (!isEvironmentCorrect) {
                JOptionPane.showMessageDialog(null, "You must detect your execution environment firstly!", "warning", JOptionPane.WARNING_MESSAGE);

            } else {

                if (!analysisStatus.isStart()) {
                    analysisStatus.setStart(true);
                    doAnalysis();


                } else {
                    JOptionPane.showMessageDialog(null, "You must wait for current analysis to complete!", "warning", JOptionPane.WARNING_MESSAGE);
                }


            }


        }

    }

    private void doAnalysis() {

        Config.isTest=false;
        Config.defaultAppDirPath = chooseFilePath;

        Thread childThreadAnalysis = new Thread(new Runnable() {
            @Override
            public void run() {

                long startTime=System.nanoTime();

                String logPaths []={Config.unitNeedAnalysisGenerate+"/"+"GenerateUnitNeedToAnalysisInfo.log",
                        Config.logDir+"/"+"com.popoaichuiniu.intentGen.IntentConditionTransformSymbolicExcutation.log",
                        Config.instrument_logDir+"/"+"InstrumentInfo.log",
                        Config.apkSignerLog+"/"+"info.log",
                        Config.cladDroidLogDir+"/"+"info.log"
                };

                for(String path:logPaths)
                {
                    File unitNeedAnalysisGenerateLog=new File(path);
                    if(unitNeedAnalysisGenerateLog.exists())
                    {
                        unitNeedAnalysisGenerateLog.delete();
                    }
                }

                ReadLogThread readLogThread=new ReadLogThread(logPaths);
                readLogThread.start();

                progressTextArea.appendText("1. Start construct CG and CFG,\n");
                progressTextArea.appendText("\tfind potential privilege leak units\n");
                GenerateUnitNeedToAnalysis.main(null);
                GenerateUnitNeedInstrumentLog.main(null);
                progressTextArea.appendText("\tPart 1 complete!\n\n");




                progressTextArea.appendText("2. Start find paths and get intent conditions\n");
                IntentConditionTransformSymbolicExcutation.main(null);
                progressTextArea.appendText("\tPart 2 complete!\n\n");


                progressTextArea.appendText("3. Start instrument and sign app\n");
                InstrumentAPPBeforePermissionInvoke.main(null);
                ApkSigner.main(null);
                progressTextArea.appendText("\tPart 3 complete!\n\n");


                progressTextArea.appendText("4. Start test app\n");
                try {
                    int status = exeCmd(new File("testAPP"), infoLogger,exceptionLogger,"../anaconda3/bin/python3", "testAPP.py", chooseFilePath,"../"+Config.dynamicTestLogDir,Config.instrumented_name_SE);
                    if (status != 0) {
                        exceptionLogger.error(status + " exeCmd error");
                    }
                } catch (IOException e) {
                    exceptionLogger.error(e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e));
                } catch (InterruptedException e) {
                    exceptionLogger.error(e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e));
                }
                progressTextArea.appendText("\tPart 4 complete!\n\n");

                analysisStatus.setStart(false);
                generateResult();

                long endTime=System.nanoTime();
                long useTime=endTime-startTime;
                double time=((double)useTime/1E9)/60;
                progressTextArea.appendText("Use Time: "+time+" minutes");
                JOptionPane.showMessageDialog(null, "Analyse completely!");


            }
        });
        childThreadAnalysis.start();


    }

    private void generateResult() {

        WriteFile writeFile = new WriteFile(Config.dynamicTestLogDir + "/" + "AnalysisResult.txt", false, exceptionLogger);
        String result = getResult();
        writeFile.writeStr(result);
        writeFile.close();
    }

    private String getResult() {

        String logPath = Config.dynamicTestLogDir + "/" + "ZMSInstrument.log";

        ReadFileOrInputStream readFileOrInputStream = new ReadFileOrInputStream(logPath, exceptionLogger);
        List<String> listString = readFileOrInputStream.getAllContentList();

        String result = "";
        for (String str : listString) {
            try {
                String[] strArray = str.split("#");
                String permissionStr = strArray[strArray.length - 1].substring(1, strArray[strArray.length - 1].length());
                String appName = strArray[2];
                String unit=strArray[6];

                result = result + appName +"\n"+unit +"\n" + permissionStr + "\n\n\n";
            } catch (Exception e) {
                exceptionLogger.error(e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e) + "##" + "analyse ZMSInstrument.log error!");
            }


        }

        return result;

    }


    @FXML
    private void viewResult() {

        try {

            String analysisResultFile = Config.dynamicTestLogDir + "/" + "AnalysisResult.txt";
            exeCmd(new File("."), infoLogger,exceptionLogger,"gedit", analysisResultFile);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }


    }

    @FXML
    private void viewLog() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    exeCmd(new File("."), infoLogger,exceptionLogger,"nautilus", Config.logDir);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }).start();


    }




    class ReadLogThread extends Thread {


        private boolean isStop = false;

        private String [] logPaths = null;

        private int index=0;

        public ReadLogThread(String [] logPaths) {
            this.logPaths = logPaths;
        }

        @Override
        public void run() {

            long lastTimeFileSize = 0;
            int count=0;

            while (!isStop) {

                while (true)
                {
                    File logFile=new File(logPaths[index]);
                    if(logFile.exists())
                    {
                        break;
                    }
                }

                System.out.println("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
                try {

                    RandomAccessFile randomFile = new RandomAccessFile(logPaths[index], "r");
                    randomFile.seek(lastTimeFileSize);

                    String oneLine = randomFile.readLine();
                    if(oneLine!=null)
                    {
                        byte [] lineBytes=oneLine.getBytes("ISO-8859-1");
                        if(logTextArea.getLength()>5000)
                        {
                            logTextArea.clear();
                        }
                        logTextArea.appendText(new String(lineBytes,"utf-8"));
                        lastTimeFileSize = lastTimeFileSize+lineBytes.length+1;//注意需要加1，跳过\n
                        count=0;
                    }
                    else
                    {
                        System.out.println("------------------------------");
                        count++;
                    }

                    randomFile.close();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        exceptionLogger.error("ReadLogThread" + e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e) + "##");
                    }

                    if(count>150)//15秒内不变化，终止读！换个文件读
                    {
                        index++;
                        if(index>=logPaths.length)
                        {
                            isStop=true;
                        }
                    }


                } catch (IOException e) {
                    exceptionLogger.error(e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e) + "##" + logPaths);
                }

            }


        }


    }




}


