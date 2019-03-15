package com.popoaichuiniu.jacy;

import com.popoaichuiniu.jacy.statistic.AndroidInfo;
import com.popoaichuiniu.util.*;
import com.zhou.InstrumentUnit;
import org.apache.log4j.Logger;
import soot.*;
import soot.jimple.*;
import soot.options.Options;
import soot.tagkit.BytecodeOffsetTag;
import org.javatuples.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DynamicSE extends BodyTransformer {
    private static boolean isTest = Config.isDynamicSETest;

    private static Map<String, Set<String>> apiPermissionMap = AndroidInfo.getPermissionAndroguardMethods();

    private static Logger exceptionLogger = new MyLogger(Config.DynamicSE_logDir, "exceptionDynamicSE").getLogger();

    private static Logger infoLogger = new MyLogger(Config.DynamicSE_logDir, "infoDynamicSE").getLogger();

    private int varName = 1000;

    @Override
    protected void internalTransform(Body b, String phaseName, Map<String, String> options) {

        SootMethod sootMethod = b.getMethod();
        if (sootMethod.getBytecodeSignature().equals("com.example.lab418.testwebview2.ExampleReceiver")) {
            System.out.println("11111111111111111111111111111111111111" + "com.example.lab418.testwebview2.ExampleReceiver");
        }
        varName = 1000;
        if (!Util.isApplicationMethod(b.getMethod())) {
            return;
        }

        PatchingChain<Unit> units = b.getUnits();

        List<InstrumentUnit> instrumentUnitsCustomMessageList = new ArrayList<InstrumentUnit>();
        List<InstrumentInfo> instrumentInfoLogConstantList = new ArrayList<>();
        List<InstrumentInfo> instrumentInfoLogVarList = new ArrayList<>();


        for (Unit unit : units) {

            if (unitNeedAnalysis(unit, b.getMethod())) {
                //Body b, Unit point, String message
                String permission = getPermissionString(unit);
                String message = "#Instrument#" + appPath +
                        "#method#" + b.getMethod().getSignature() + "#unitPoint_before#" + unit.toString() + "#lineNumber#" + unit.getJavaSourceStartLineNumber() + "#unitPoint_permission#" + permission;

                instrumentUnitsCustomMessageList.add(new InstrumentUnit(b, unit, message));
            }


            Set<InstrumentInfo> instrumentInfoSet = unitNeedInstrumentToGetInfo(unit, b.getMethod());

            if(instrumentInfoSet!=null&&instrumentInfoSet.size()>0)
            {
                for(InstrumentInfo instrumentInfo:instrumentInfoSet)
                {
                    if (instrumentInfo.isLocal) {
                        instrumentInfoLogVarList.add(instrumentInfo);
                    } else {
                        instrumentInfoLogConstantList.add(instrumentInfo);
                    }
                }
            }


        }

        try {

            for (InstrumentUnit instrumentUnit : instrumentUnitsCustomMessageList) {
                addInstrumentBeforeStatementCustomMessage(instrumentUnit.body, instrumentUnit.point, instrumentUnit.message);
            }

            for (InstrumentInfo instrumentInfoVar : instrumentInfoLogVarList) {//
                addInstrumentBeforeStatementInstrumentInfoVar(instrumentInfoVar);
            }

            for (InstrumentInfo instrumentInfoConstant : instrumentInfoLogConstantList) {
                addInstrumentBeforeStatementInstrumentInfoConstant(instrumentInfoConstant);
            }

        } catch (RuntimeException e) {
            writeFileAppInstrumentException.writeStr(e.getMessage() + " " + ExceptionStackMessageUtil.getStackTrace(e) + "###" + appPath + "\n");
            writeFileAppInstrumentException.flush();
        }


    }

    private void addInstrumentBeforeStatementInstrumentInfoConstant(InstrumentInfo instrumentInfoConstant) {

        //insert a log.i instrument statement

        JimpleBody body = (JimpleBody) instrumentInfoConstant.sootMethod.getActiveBody();

        Scene.v().forceResolve("android.util.Log", SootClass.SIGNATURES);
        Scene.v().forceResolve("java.lang.String", SootClass.SIGNATURES);

        SootMethod logMethod = Scene.v().getMethod("<android.util.Log: int i(java.lang.String,java.lang.String)>");


        String logTag = "ZMSGetInfo";
        logTag = logTag + "_" + instrumentInfoConstant.id + "_" + instrumentInfoConstant.isIf+"_"+instrumentInfoConstant.type;
        Value logType = StringConstant.v(logTag);

        Value logMessage = StringConstant.v(instrumentInfoConstant.name.replaceAll("\"", ""));


        //make new static invokement
        StaticInvokeExpr newInvokeExpr = Jimple.v().newStaticInvokeExpr(logMethod.makeRef(), logType, logMessage);
        // turn it into an invoke statement

        InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(newInvokeExpr);

        body.getUnits().insertBefore(invokeStmt, instrumentInfoConstant.point);
        //check that we did not mess up the Jimple
        body.validate();

        infoLogger.info("insert unit " + invokeStmt);


    }

    private void addInstrumentBeforeStatementInstrumentInfoVar(InstrumentInfo instrumentInfoVar) {

        //insert a log.i instrument statement

        JimpleBody body = (JimpleBody) instrumentInfoVar.sootMethod.getActiveBody();

        Scene.v().forceResolve("android.util.Log", SootClass.SIGNATURES);
        Scene.v().forceResolve("java.lang.String", SootClass.SIGNATURES);

        SootMethod logMethod = Scene.v().getMethod("<android.util.Log: int i(java.lang.String,java.lang.String)>");

        SootMethod stringMethodBoolean = Scene.v().getMethod("<java.lang.String: java.lang.String valueOf(boolean)>");

        SootMethod stringMethodChar = Scene.v().getMethod("<java.lang.String: java.lang.String valueOf(char)>");

        SootMethod stringMethodInt = Scene.v().getMethod("<java.lang.String: java.lang.String valueOf(int)>");

        SootMethod stringMethodFloat = Scene.v().getMethod("<java.lang.String: java.lang.String valueOf(float)>");

        SootMethod stringMethodDouble = Scene.v().getMethod("<java.lang.String: java.lang.String valueOf(double)>");

        SootMethod stringMethodLong = Scene.v().getMethod("<java.lang.String: java.lang.String valueOf(long)>");

        String logTag = "ZMSGetInfo";
        logTag = logTag + "_" + instrumentInfoVar.id + "_" + instrumentInfoVar.isIf+"_"+instrumentInfoVar.type;
        Value logType = StringConstant.v(logTag);

        Value logMessage = null;
        Type localType = Scene.v().getType(instrumentInfoVar.type);
        Unit pointUnit = instrumentInfoVar.point;
        DefinitionStmt definitionStmt = (DefinitionStmt) pointUnit;
        Local local = null;
        for (ValueBox valueBox : definitionStmt.getRightOp().getUseBoxes()) {
            if (valueBox.getValue() instanceof Local && valueBox.getValue().getType().toString().equals(instrumentInfoVar.type) && valueBox.getValue().toString().equals(instrumentInfoVar.name)) {
                local = (Local) valueBox.getValue();
            }
        }

        if (local == null) {
            exceptionLogger.error("can't find local");
            return;
        }


        if (local.getType().toString().equals("java.lang.String")) {

            logMessage = local;

        } else {

            //SootMethodRef whichStringValueOfSootMethod = stringMethodBoolean.makeRef();

            SootMethodRef whichStringValueOfSootMethod = null;
            switch (local.getType().toString()) {
                case "boolean":
                    whichStringValueOfSootMethod = stringMethodBoolean.makeRef();
                    break;
                case "char":
                    whichStringValueOfSootMethod = stringMethodChar.makeRef();
                    break;
                case "int":
                    whichStringValueOfSootMethod = stringMethodInt.makeRef();
                    break;
                case "float":
                    whichStringValueOfSootMethod = stringMethodFloat.makeRef();
                    break;
                case "long":
                    whichStringValueOfSootMethod = stringMethodLong.makeRef();
                    break;
                case "double":
                    whichStringValueOfSootMethod = stringMethodDouble.makeRef();
                    break;
                case "byte":
                    whichStringValueOfSootMethod = stringMethodChar.makeRef();
                    break;
                case "short":
                    whichStringValueOfSootMethod = stringMethodInt.makeRef();
                    break;
                default:
                    exceptionLogger.error("can't handle this type " + local.getType().toString());


            }

            if (whichStringValueOfSootMethod != null) {
                StaticInvokeExpr stringValueOfInvokeExpr = Jimple.v().newStaticInvokeExpr(whichStringValueOfSootMethod, local);
                Local localStringValueOfReturn = Jimple.v().newLocal("r" + varName, Scene.v().getType("java.lang.String"));
                body.getLocals().insertAfter(localStringValueOfReturn, local);
                varName++;
                //AssignStmt表示赋值语句；而IdentityStmt表示将参数赋值给Local这样的语句。
                AssignStmt assignStmt = Jimple.v().newAssignStmt(localStringValueOfReturn, stringValueOfInvokeExpr);
                logMessage = localStringValueOfReturn;
                body.getUnits().insertBefore(assignStmt, instrumentInfoVar.point);
                //check that we did not mess up the Jimple
                body.validate();
                infoLogger.info("insert unit " + assignStmt);
            }


        }

        if (logMessage != null) {
            //make new static invokement
            StaticInvokeExpr newInvokeExpr = Jimple.v().newStaticInvokeExpr(logMethod.makeRef(), logType, logMessage);
            // turn it into an invoke statement

            InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(newInvokeExpr);


            body.getUnits().insertBefore(invokeStmt, instrumentInfoVar.point);
            //check that we did not mess up the Jimple
            body.validate();
            infoLogger.info("insert unit " + invokeStmt);
        } else {
            exceptionLogger.error("instrument error " + instrumentInfoVar.sootMethod.getBytecodeSignature() + "##" + instrumentInfoVar.point);
        }

    }

    /**
     * appPath是应用的绝对路径
     * platforms是SDK中platforms的路径
     */
    //static String testAppPath = "/home/lab418/AndroidStudioProjects/TestIntrument3/app/build/outputs/apk/debug/app-debug.apk";
    private String appPath = null;
    //private String platforms = "/home/zms/platforms";//设置最低版本为android5.0，app就插桩失败  签名的jarsigner不行了？soot太老了？

    private Set<Pair<Integer, String>> targets = null;

    private static WriteFile writeFile_instrument_content = null;
    private static WriteFile writeFile_app_has_Instrumented = null;

    private static WriteFile writeFileAppInstrumentException = null;//记录出现插桩失败的app

    private static volatile int inStrumentCount = 0;

    private static int targetCount = 0;


    //------------------------------------------getInfoInstrument


    private Set<InstrumentInfoByte> instrumentUnitInfoByteSet = null;

    public DynamicSE(String appPath, String targetsFile, String instrumentUnitInfoFile) {
        this.appPath = appPath;
        targets = new LinkedHashSet<Pair<Integer, String>>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(targetsFile));

            String content = null;
            while ((content = bufferedReader.readLine()) != null) {

                String[] str = content.split("#");
                String methodString = str[0];
                String byteTag = str[1];

                targets.add(new Pair<>(Integer.valueOf(byteTag), methodString));

            }

            targetCount = targets.size();
            inStrumentCount = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }


        //------------------------------------------getInfoInstrument


//        SootMethod sootMethod;
//        Unit point;
//        String name;
//        String type;
//        boolean isLocal;
//        String id;//action extra, category
//        boolean isIf;

        instrumentUnitInfoByteSet = new LinkedHashSet<InstrumentInfoByte>();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(instrumentUnitInfoFile));

            String content = null;
            while ((content = bufferedReader.readLine()) != null) {

                String[] str = content.split("#");
                String methodString = str[0];
                String byteTag = str[1];
                String name = str[2];
                String type = str[3];
                boolean isLocal = Boolean.parseBoolean(str[4]);
                String id = str[5];
                boolean isIf = Boolean.parseBoolean(str[6]);
                InstrumentInfoByte instrumentInfoByte = new InstrumentInfoByte(methodString, byteTag, name, type, isLocal, id, isIf);
                instrumentUnitInfoByteSet.add(instrumentInfoByte);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private boolean unitNeedAnalysis(Unit unit, SootMethod sootMethod) {
        BytecodeOffsetTag tag = Util.extractByteCodeOffset(unit);
        if (tag == null) {
            return false;
        }

        for (Pair<Integer, String> item : targets) {
            if (item.getValue0() == tag.getBytecodeOffset() && item.getValue1().equals(sootMethod.getBytecodeSignature())) {
                return true;
            }

        }
        return false;
    }

    private Set<InstrumentInfo> unitNeedInstrumentToGetInfo(Unit unit, SootMethod sootMethod) {

        Set<InstrumentInfo> instrumentInfoSet=new HashSet<>();
        BytecodeOffsetTag tag = Util.extractByteCodeOffset(unit);
        if (tag == null) {
            return null;
        }

        for (InstrumentInfoByte instrumentInfoByte : instrumentUnitInfoByteSet) {
            if (Integer.valueOf(instrumentInfoByte.byteTag) == tag.getBytecodeOffset() && instrumentInfoByte.methodString.equals(sootMethod.getBytecodeSignature())) {
                instrumentInfoSet.add(new InstrumentInfo(sootMethod, unit, instrumentInfoByte.name, instrumentInfoByte.type, instrumentInfoByte.isLocal, instrumentInfoByte.id, instrumentInfoByte.isIf)) ;
            }

        }
        return instrumentInfoSet;
    }

    public static void main(String[] args) {

        String[] instrumentArgs = new String[4];


        String appDir = null;
        if (isTest) {
            appDir = Config.testAppPath;
        } else {
            appDir = Config.defaultAppDirPath;
        }


        File appDirFile = new File(appDir);

        writeFileAppInstrumentException = new WriteFile(Config.DynamicSE_logDir + "/" + appDirFile.getName() + "_instrumentException.log", true, exceptionLogger);

        if (appDirFile.isDirectory()) {

            File hasInstrumentedFile = new File(Config.DynamicSE_logDir + "/" + appDirFile.getName() + "_has_Instrumented.txt");

            if (!hasInstrumentedFile.exists()) {
                try {
                    hasInstrumentedFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException("创建hasInstrumentedFile失败！");
                }

            }

            Set<String> hasInstrumentAPP = new ReadFileOrInputStream(hasInstrumentedFile.getAbsolutePath(), exceptionLogger).getAllContentLinSet();

            if (hasInstrumentAPP == null) {
                throw new RuntimeException("读取app_has_Instrumented.txt失败！");
            }


            writeFile_app_has_Instrumented = new WriteFile(hasInstrumentedFile.getAbsolutePath(), true, exceptionLogger);


            for (File file : appDirFile.listFiles()) {
                if (file.getName().endsWith(".apk")) {
                    if (hasInstrumentAPP.contains(file.getAbsolutePath())) {
                        continue;
                    }
                    File unitedAnalysis = new File(file.getAbsolutePath() + "_UnitsNeedAnalysis.txt");
                    File unitInstrumentToGetInfo = new File(file.getAbsolutePath() + "_" + "UnitsInstrumentGetInfo.txt");
                    if (unitedAnalysis.exists()) {

                        Thread childThread = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub

                                Long startTime = System.nanoTime();
                                instrumentArgs[0] = file.getAbsolutePath();
                                instrumentArgs[1] = "/home/zms/platforms";
                                instrumentArgs[2] = unitedAnalysis.getAbsolutePath();
                                instrumentArgs[3] = unitInstrumentToGetInfo.getAbsolutePath();

                                soot.G.reset();
                                //try {
                                singleAPPAnalysis(instrumentArgs);
//                                } catch (RuntimeException e) {
//                                    writeFileAppInstrumentException.writeStr(e.getMessage() + " " + instrumentArgs[0] + "\n");
//                                    writeFileAppInstrumentException.flush();
//                                }


                                writeFile_app_has_Instrumented.writeStr(instrumentArgs[0] + "\n");
                                writeFile_app_has_Instrumented.flush();


                                Long stopTime = System.nanoTime();
                                System.out.println("运行时间:" + ((stopTime - startTime) / 1000 / 1000 / 1000 / 60) + "分钟");
                            }
                        });
                        childThread.start();

                        try {
                            childThread.join();
                        } catch (InterruptedException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }


                    }


                }
            }

            writeFileAppInstrumentException.close();


            writeFile_app_has_Instrumented.close();
        } else {
            File unitedAnalysis = new File(appDirFile.getAbsolutePath() + "_UnitsNeedAnalysis.txt");
            File unitInstrumentToGetInfo = new File(appDirFile.getAbsolutePath() + "_" + "UnitsInstrumentGetInfo.txt");
            instrumentArgs[0] = appDirFile.getAbsolutePath();
            instrumentArgs[1] = "/home/zms/platforms";
            instrumentArgs[2] = unitedAnalysis.getAbsolutePath();
            instrumentArgs[3] = unitInstrumentToGetInfo.getAbsolutePath();
            soot.G.reset();
            singleAPPAnalysis(instrumentArgs);
        }


    }

    private static void singleAPPAnalysis(String[] args) {

        infoLogger.info(args[0] + " startInstrument");
        String outputDir = new File(args[0]).getParentFile().getAbsolutePath() + "/" + "instrumented";
        File outputDirFile = new File(outputDir);

        if (!outputDirFile.exists()) {
            outputDirFile.mkdir();

        }
        if (!outputDirFile.exists()) {
            throw new RuntimeException("instrumented目录创建失败！");
        }
        String sootArgs[] = {
                "-process-dir", args[0],
                "-android-jars", args[1],
                "-allow-phantom-refs",
                "-output-dir", outputDir,


        };


        //prefer Android APK files// -src-prec apk
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_keep_line_number(true);
        Options.v().set_coffi(true);
        Options.v().set_keep_offset(true);

        //output as APK, too//-f J
        Options.v().set_output_format(Options.output_format_dex);
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().set_force_overwrite(true);

        Scene.v().addBasicClass("android.util.Log", SootClass.SIGNATURES);


        writeFile_instrument_content = new WriteFile(args[0] + "_instrument.log", false, exceptionLogger);


        DynamicSE dynamicSE = new DynamicSE(args[0], args[2], args[3]);

        PackManager.v().getPack("jtp").add(new Transform("jtp.myDynamicSE", dynamicSE));


        soot.Main.main(sootArgs);

        writeFile_instrument_content.close();


        if (targetCount != inStrumentCount) {

            exceptionLogger.error(args[0] + "##" + "插桩数量和需要插桩的数量不匹配！");

        }

        infoLogger.info(args[0] + " instrument over!");

    }


    public void addInstrumentBeforeStatementCustomMessage(Body b, Unit point, String message) {


        //insert a log.i instrument statement

        Scene.v().forceResolve("android.util.Log", SootClass.SIGNATURES);

        SootMethod log = Scene.v().getMethod("<android.util.Log: int i(java.lang.String,java.lang.String)>");

        Value logType = StringConstant.v("ZMSInstrument");
        Value logMessage = StringConstant.v(message);

        //Jimple.v().newLocal()
        //Scene.v().getRefType()
        //Scene.v().getType()


        //make new static invokement
        StaticInvokeExpr newInvokeExpr = Jimple.v().newStaticInvokeExpr(log.makeRef(), logType, logMessage);
        // turn it into an invoke statement

        InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(newInvokeExpr);

        b.getUnits().insertBefore(invokeStmt, point);

        //check that we did not mess up the Jimple
        b.validate();
        infoLogger.info("insert unit " + invokeStmt);
        inStrumentCount = inStrumentCount + 1;


        writeFile_instrument_content.writeStr(logMessage.toString() + "\n");
        writeFile_instrument_content.flush();


    }

    private String getPermissionString(Unit point) {

        if (point == null) {
            throw new RuntimeException("point unit is null");
        } else {
            Stmt stmt = (Stmt) point;
            InvokeExpr invokeExpr = stmt.getInvokeExpr();
            if (invokeExpr == null) {
                throw new RuntimeException("point unit don't have invokeExpr");
            } else {
                Set<String> permissionSet = apiPermissionMap.get(invokeExpr.getMethod().getBytecodeSignature());

                if (permissionSet == null) {
                    throw new RuntimeException("point unit isn't protected by permission");
                } else {
                    String allPermission = "";
                    for (String permission : permissionSet) {
                        allPermission = allPermission + "," + permission;
                    }

                    return allPermission;

                }
            }
        }
    }

}
