package com.popoaichuiniu.jacy;

import com.popoaichuiniu.jacy.statistic.AndroidCallGraph;
import com.popoaichuiniu.jacy.statistic.AndroidCallGraphProxy;
import com.popoaichuiniu.jacy.statistic.AndroidInfo;
import com.popoaichuiniu.util.*;
import org.apache.log4j.Logger;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;

import java.io.*;
import java.util.*;



public class GenerateIntentIfUnitToGetInfo {//日志设置合理


    private static boolean isTest = Config.isTest;


    private static Logger exceptionLogger = new MyLogger(Config.DynamicSE_logDir, "generateUnitNeedToAnalysisAppException").getLogger();

    private static Logger infoLogger = new MyLogger(Config.DynamicSE_logDir, "GenerateUnitNeedToAnalysisInfo").getLogger();



    private static void generateUnitToAnalysis(List<SootMethod> ea_entryPoints, CallGraph cg, String appPath) throws IOException {





        WriteFile writeFileUnitsNeedAnalysis = null;

        writeFileUnitsNeedAnalysis = new WriteFile(appPath + "_" + "UnitsNeedInstrumentBefore.txt", false, exceptionLogger);


        for (Unit unit : unitsNeedToAnalysis) {

            Stmt stmt = (Stmt) unit;
            InvokeExpr invokeExpr = stmt.getInvokeExpr();
            if (invokeExpr == null) {
                throw new RuntimeException("illegal unitNeedAnalysis" + unit.toString());
            } else {
                writeFileUnitsNeedAnalysis.writeStr(sootMethod.getBytecodeSignature() + "#" + unit.getTag("BytecodeOffsetTag") + "#" + unit.toString() + "#" + invokeExpr.getMethod().getBytecodeSignature() + "\n");
            }


        }


        writeFileUnitsNeedAnalysis.close();


    }


    public static void main(String[] args) {


        String appDirPath = null;
        if (isTest) {
            appDirPath = Config.testAppPath;
        } else {

            appDirPath = Config.defaultAppDirPath;

        }


        File appDir = new File(appDirPath);


        if (appDir.isDirectory()) {


            File hasGeneratedAPPFile = new File(Config.DynamicSE_logDir + "/" + appDir.getName() + "_hasGeneratedAPP.txt");
            if (!hasGeneratedAPPFile.exists()) {
                try {
                    hasGeneratedAPPFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            Set<String> hasGenerateAppSet = new ReadFileOrInputStream(Config.DynamicSE_logDir + "/" + appDir.getName() + "_hasGeneratedAPP.txt", exceptionLogger).getAllContentLinSet();
            WriteFile writeFileHasGenerateUnitNeedAnalysis = new WriteFile(Config.DynamicSE_logDir + "/" + appDir.getName() + "_hasGeneratedAPP.txt", true, exceptionLogger);//分析一个目录中途断掉，可以继续重新分析


            for (File apkFile : appDir.listFiles()) {
                if (apkFile.getName().endsWith(".apk") && (!apkFile.getName().contains("_signed_zipalign"))) {
                    if (hasGenerateAppSet.contains(apkFile.getAbsolutePath())) {
                        continue;
                    }


                    Thread childThread = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {


                                singleAPPAnalysis(apkFile.getAbsolutePath());//分析每一个app

                            } catch (Exception e) {
                                exceptionLogger.error(apkFile.getAbsolutePath() + "&&" + "Exception" + "###" + e.getMessage() + "###" + ExceptionStackMessageUtil.getStackTrace(e));
                            }


                        }
                    });

                    childThread.start();

                    try {
                        childThread.join();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    writeFileHasGenerateUnitNeedAnalysis.writeStr(apkFile.getAbsolutePath() + "\n");//分析完整成功才写进去。
                    writeFileHasGenerateUnitNeedAnalysis.flush();


                }

            }


            writeFileHasGenerateUnitNeedAnalysis.close();

        } else {

            try {
                singleAPPAnalysis(appDirPath);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    public static void singleAPPAnalysis(String appPath) throws IOException {

        infoLogger.info("start Part1's analysis " + appPath);
        AndroidInfo androidInfo = new AndroidInfo(appPath, exceptionLogger);


        AndroidCallGraph androidCallGraph = new AndroidCallGraphProxy(appPath, Config.androidJar, exceptionLogger).androidCallGraph;

        CallGraph cGraph = androidCallGraph.getCg();

        infoLogger.info("CG and CFG has constructed!");


        List<SootMethod> ea_entryPoints = Util.getEA_EntryPoints(androidCallGraph, androidInfo);


        generateUnitToAnalysis(ea_entryPoints, cGraph, appPath);

        infoLogger.info("Part1's analysis completed! " + appPath);
    }
}

