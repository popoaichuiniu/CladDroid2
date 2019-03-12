package com.popoaichuiniu.jacy;

import com.popoaichuiniu.jacy.statistic.AndroidCallGraph;
import com.popoaichuiniu.jacy.statistic.AndroidCallGraphProxy;
import com.popoaichuiniu.jacy.statistic.AndroidInfo;
import com.popoaichuiniu.util.*;
import org.apache.log4j.Logger;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

import java.io.*;
import java.util.*;


class InstrumentInfo{
    SootMethod sootMethod;
    Unit point;
    String name;
    String type;
    boolean isLocal;
    String id;//action extra, category

    public InstrumentInfo(SootMethod sootMethod, Unit point, String name, String type, boolean isLocal, String id) {
        this.sootMethod = sootMethod;
        this.point = point;
        this.name = name;
        this.type = type;
        this.isLocal = isLocal;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstrumentInfo that = (InstrumentInfo) o;
        return isLocal == that.isLocal &&
                Objects.equals(sootMethod, that.sootMethod) &&
                Objects.equals(point, that.point) &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sootMethod, point, name, type, isLocal, id);
    }
}


public class GenerateIntentIfUnitToGetInfo {//日志设置合理


    //private static boolean isTest = Config.isTest;
    private static boolean isTest = false;

    private static Logger exceptionLogger = new MyLogger(Config.DynamicSE_logDir, "generateUnitNeedToAnalysisAppException").getLogger();

    private static Logger infoLogger = new MyLogger(Config.DynamicSE_logDir, "GenerateUnitNeedToAnalysisInfo").getLogger();


    private static boolean isJustThinkDangerous = false;//是否只考虑危险API

    private static Set<String> dangerousPermissions = null;
    private static Map<String, Set<String>> apiPermissionMap = AndroidInfo.getPermissionAndroguardMethods();

    private static void generateUnitToAnalysis(List<SootMethod> ea_entryPoints, CallGraph cg, String appPath) {


        //WriteFileForUnitNeedAnalysis(ea_entryPoints, cg, appPath);

        WriteFileForUnitInstrumentAnalysis(ea_entryPoints, cg, appPath);


    }

    private static void WriteFileForUnitInstrumentAnalysis(List<SootMethod> ea_entryPoints, CallGraph cg, String appPath) {

        IntentDataFlowAnalysisForDynamicSE.clearIntentDataFlowAnalysisForDynamicSE();//
        for (SootMethod sootMethod : ea_entryPoints) {


            IntentDataTransfer initialIntentDataTransfer = new IntentDataTransfer();
            initialIntentDataTransfer.targetSootMethod = sootMethod;

            int count = 0;
            for (Type type : sootMethod.getParameterTypes()) {
                if (type.toString().equals("android.content.Intent")) {
                    initialIntentDataTransfer.type = IntentDataTransfer.TYPE_INTENT;
                    initialIntentDataTransfer.targetParameter = count;
                }

                count++;
            }


            if (Util.isApplicationMethod(initialIntentDataTransfer.targetSootMethod) && initialIntentDataTransfer.targetSootMethod.hasActiveBody()) {

                MethodSummary methodSummary = new MethodSummary(initialIntentDataTransfer.targetSootMethod, initialIntentDataTransfer.targetParameter);
                IntentDataFlowAnalysisForDynamicSE.methodSummarySet.add(methodSummary);

                BriefUnitGraph briefUnitGraph = new BriefUnitGraph(initialIntentDataTransfer.targetSootMethod.getActiveBody());
                IntentDataFlowAnalysisForDynamicSE intentDataFlowAnalysisForDynamicSE = new IntentDataFlowAnalysisForDynamicSE(briefUnitGraph, initialIntentDataTransfer, exceptionLogger);

                IntentDataFlowAnalysisForDynamicSE.allInstrumentUnitIntentDataFlowIn.putAll(intentDataFlowAnalysisForDynamicSE.instrumentUnitIntentDataFlowIn);


            }


        }

        Set<InstrumentInfo> allInstrumentInfo=new HashSet<>();
        WriteFile allWriteFile = new WriteFile(Config.DynamicSE_logDir + "/" + "instrument_unit.log", true, exceptionLogger);
        for (Map.Entry<MethodUnit, IntentDataTransfer> entry : IntentDataFlowAnalysisForDynamicSE.allInstrumentUnitIntentDataFlowIn.entrySet()) {

//            allWriteFile.writeStr(appPath + "##" + entry.getKey() + "##" + entry.getValue().type + "##" + entry.getValue().id + "\n");
//            allWriteFile.flush();


            if (!entry.getValue().type.equals(IntentDataTransfer.TYPE_INTENT))//intent data doesn't need instrument
            {
                SootMethod sootMethod = entry.getKey().sootMethod;
                IfStmt ifStmt = (IfStmt) entry.getKey().unit;
                ConditionExpr condition = (ConditionExpr) ifStmt.getCondition();
                Value conditionLeft = condition.getOp1();
                if (conditionLeft instanceof Local) {
                    Local local = (Local) conditionLeft;
                    BriefUnitGraph briefUnitGraph = new BriefUnitGraph(sootMethod.getActiveBody());
                    SimpleLocalDefs simpleLocalDefs = new SimpleLocalDefs(briefUnitGraph);
                    for (Unit defUnit : simpleLocalDefs.getDefsOfAt(local, ifStmt))//
                    {
                        DefinitionStmt definitionStmt = (DefinitionStmt) defUnit;
                        List<ValueBox> valueBoxList = definitionStmt.getRightOp().getUseBoxes();
                        for (ValueBox valueBox : valueBoxList) {

                            if (entry.getValue().type.equals(IntentDataTransfer.TYPE_ACTION) || entry.getValue().type.equals(IntentDataTransfer.TYPE_CATEGORY)) {
                                if (valueBox.getValue().getType().toString().equals("java.lang.String")) {
                                    if(valueBox.getValue() instanceof Local)
                                    {
                                        allInstrumentInfo.add(new InstrumentInfo(entry.getKey().sootMethod,definitionStmt,valueBox.getValue().toString(),valueBox.getValue().getType().toString(),true,entry.getValue().id));
                                    }
                                    else if(valueBox.getValue() instanceof Constant)
                                    {
                                        allInstrumentInfo.add(new InstrumentInfo(entry.getKey().sootMethod,definitionStmt,valueBox.getValue().toString(),valueBox.getValue().getType().toString(),false,entry.getValue().id));
                                    }
                                    else
                                    {
                                        exceptionLogger.warn("can't handle -----not local or constant ");
                                    }



                                }
                            }

                            if (entry.getValue().type.equals(IntentDataTransfer.TYPE_EXTRA)) {
                                Unit extraUnit=entry.getValue().whereGenUnit;
                                SootMethod extraSootMethod=entry.getValue().whereGenSootMethod;

                                if (valueBox.getValue().getType() instanceof PrimType || valueBox.getValue().getType().toString().equals("java.lang.String")) {

                                    if(valueBox.getValue() instanceof Local)
                                    {
                                        allInstrumentInfo.add(new InstrumentInfo(entry.getKey().sootMethod,definitionStmt,valueBox.getValue().toString(),valueBox.getValue().getType().toString(),true,entry.getValue().id));
                                    }
                                    else if(valueBox.getValue() instanceof Constant)
                                    {
                                        allInstrumentInfo.add(new InstrumentInfo(entry.getKey().sootMethod,definitionStmt,valueBox.getValue().toString(),valueBox.getValue().getType().toString(),false,entry.getValue().id));
                                    }
                                    else
                                    {
                                        exceptionLogger.warn("can't handle -----not local or constant ");
                                    }

                                }
                            }

                            allWriteFile.writeStr(valueBox.getValue().getType().toString() + "\n");
                            allWriteFile.flush();
                        }
                    }
                }
            }


        }

        allWriteFile.close();


    }



    private static void WriteFileForUnitNeedAnalysis(List<SootMethod> ea_entryPoints, CallGraph cg, String appPath) {
        List<SootMethod> roMethods = Util.getMethodsInReverseTopologicalOrder(ea_entryPoints, cg);


        WriteFile writeFileUnitsNeedAnalysis = null;

        writeFileUnitsNeedAnalysis = new WriteFile(appPath + "_" + "UnitsNeedAnalysis.txt", false, exceptionLogger);


        for (SootMethod sootMethod : roMethods) {

            List<Unit> unitsNeedToAnalysis = new ArrayList<>();

            Body body = sootMethod.getActiveBody();
            if (body != null) {
                PatchingChain<Unit> units = body.getUnits();
                for (Unit unit : units) {

                    SootMethod calleeSootMethod = Util.getCalleeSootMethodAt(unit);
                    if (calleeSootMethod == null) {
                        continue;
                    }
                    Set<String> permissionSet = apiPermissionMap.get(calleeSootMethod.getBytecodeSignature());
                    if (permissionSet != null && isExistSimilarItem(permissionSet, dangerousPermissions)) {
                        unitsNeedToAnalysis.add(unit);
                        infoLogger.info(appPath + " ##" + unit + " need analysis!");

                    }


                }

                for (Unit unit : unitsNeedToAnalysis) {

                    Stmt stmt = (Stmt) unit;
                    InvokeExpr invokeExpr = stmt.getInvokeExpr();
                    if (invokeExpr == null) {
                        throw new RuntimeException("illegal unitNeedAnalysis" + unit.toString());
                    } else {
                        writeFileUnitsNeedAnalysis.writeStr(sootMethod.getBytecodeSignature() + "#" + unit.getTag("BytecodeOffsetTag") + "#" + unit.toString() + "#" + invokeExpr.getMethod().getBytecodeSignature() + "\n");
                    }


                }

            }
        }

        writeFileUnitsNeedAnalysis.close();
    }


    public static void main(String[] args) {
        //dangerousPermissions是考虑的自己设定的需要考虑的权限提升
        dangerousPermissions = new ReadFileOrInputStream("AnalysisAPKIntent" + "/" + "think_dangerousPermission.txt", exceptionLogger).getAllContentLinSet();
        for (Iterator<String> dangerousPermissionsIterator = dangerousPermissions.iterator(); ((Iterator) dangerousPermissionsIterator).hasNext(); ) {
            String dangerousPermission = dangerousPermissionsIterator.next();
            if (dangerousPermission.startsWith("#")) {
                dangerousPermissionsIterator.remove();//
            }
        }

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

    private static boolean isExistSimilarItem(Set<String> permissionSet, Set<String> dangerousPermissions) {
        if (isJustThinkDangerous) {
            for (String permission : permissionSet) {
                if (dangerousPermissions.contains(permission)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }

    }
}

