package com.zhou;

import com.popoaichuiniu.util.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class ApkSigner {

    public static boolean isTest = Config.isTest;


    public static  String instrumented_dir_name=Config.default_instrumented_name;

    private static Logger exceptionLogger = new MyLogger(Config.apkSignerLog, "exception").getLogger();
    private static Logger infoLogger = new MyLogger(Config.apkSignerLog, "info").getLogger();


    public static void main(String[] args) {


        String appDir = null;
        if (isTest) {
            appDir = new File(Config.testAppPath).getParentFile().getAbsolutePath() + "/" + instrumented_dir_name+"/" + new File(Config.testAppPath).getName();
        } else {
            File dirFile = new File(Config.defaultAppDirPath);
            if (dirFile.isDirectory()) {
                appDir = Config.defaultAppDirPath + "/" + instrumented_dir_name;
            } else {
                appDir = dirFile.getParentFile().getAbsolutePath() + "/" + instrumented_dir_name+"/" + dirFile.getName();
            }

        }

        File appDirFile = new File(appDir);
        if (appDirFile.isDirectory()) {

            File hasSignedAppsFile = new File(Config.apkSignerLog + "/" + appDirFile.getName() + "_hasSignedApps.txt");
            if (!hasSignedAppsFile.exists()) {
                File parentDir = hasSignedAppsFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }
                try {
                    hasSignedAppsFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException("无法创建hasSignedAppsFile！");
                }

            }
            Set<String> hasSignedApps = new ReadFileOrInputStream(hasSignedAppsFile.getAbsolutePath(), exceptionLogger).getAllContentLinSet();
            WriteFile writeFileHasSigned = new WriteFile(hasSignedAppsFile.getAbsolutePath(), true, exceptionLogger);
            WriteFile writeFileException = new WriteFile(Config.apkSignerLog + "/exceptionSignedApps.txt", true, exceptionLogger);
            for (File apkFile : appDirFile.listFiles()) {
                if ((apkFile.getName().endsWith(".apk")) && (!apkFile.getName().endsWith("_signed_zipalign.apk"))) {
                    if (hasSignedApps.contains(apkFile.getAbsolutePath())) {
                        continue;
                    }

                    try {
                        singleAppAnalysis(apkFile);
                        writeFileHasSigned.writeStr(apkFile.getAbsolutePath() + "\n");
                        writeFileHasSigned.flush();
                    } catch (Exception e) {

                        writeFileException.writeStr(apkFile.getAbsolutePath() + "##" + e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e) + "\n");
                        writeFileException.flush();


                    }


                }
            }

            writeFileException.close();
            writeFileHasSigned.close();


        } else {

            try {
                singleAppAnalysis(appDirFile);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private static void singleAppAnalysis(File appDirFile) throws InterruptedException, IOException {

        infoLogger.info("start APK Sign:" + appDirFile.getAbsolutePath());

        ProcessBuilder processBuilder = new ProcessBuilder("InstrumentAPK/src/com/zhou/apkSigner.sh", appDirFile.getAbsolutePath());
        // ProcessBuilder processBuilder=new ProcessBuilder();


        processBuilder.redirectErrorStream(true);


        System.out.println("InstrumentAPK/src/com/zhou/apkSigner.sh" + "  " + appDirFile.getAbsolutePath());

        Process process = processBuilder.start();


        Thread childThread = new Thread(new Runnable() {//must start thread to read process output
            @Override
            public void run() {

                ReadFileOrInputStream readFileOrInputStreamReturnString = new ReadFileOrInputStream(process.getInputStream(), exceptionLogger);
                System.out.println(readFileOrInputStreamReturnString.getContent() + "&&&");
                infoLogger.info(readFileOrInputStreamReturnString.getContent() + "&&&");

            }
        });

        childThread.start();
        int status = process.waitFor();//


        if (status != 0) {

            throw new RuntimeException("sign apk failed!\n");
        }

        infoLogger.info("apk sign over:" + appDirFile.getAbsolutePath());
    }
}
