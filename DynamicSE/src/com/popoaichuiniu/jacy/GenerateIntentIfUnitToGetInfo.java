package com.popoaichuiniu.jacy;

import com.popoaichuiniu.intentGen.*;
import com.popoaichuiniu.jacy.statistic.AndroidCallGraph;
import com.popoaichuiniu.jacy.statistic.AndroidCallGraphProxy;
import com.popoaichuiniu.jacy.statistic.AndroidInfo;
import com.popoaichuiniu.util.*;
import org.apache.log4j.Logger;
import soot.*;
import soot.jimple.*;
import soot.jimple.infoflow.android.axml.AXmlAttribute;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

import java.io.*;
import java.util.*;


public class GenerateIntentIfUnitToGetInfo {//日志设置合理


    private static boolean isTest = Config.isDynamicSETest;

    private static Logger exceptionLogger = new MyLogger(Config.DynamicSE_logDir, "generateUnitNeedToAnalysisAppException").getLogger();

    private static Logger infoLogger = new MyLogger(Config.DynamicSE_logDir, "GenerateUnitNeedToAnalysisInfo").getLogger();


    private static boolean isJustThinkDangerous = false;//是否只考虑危险API

    private static Set<String> dangerousPermissions = null;
    private static Map<String, Set<String>> apiPermissionMap = AndroidInfo.getPermissionAndroguardMethods();

    private static void generateUnitToAnalysis(List<SootMethod> ea_entryPoints, CallGraph cg, String appPath) {


        WriteFileForUnitNeedAnalysis(ea_entryPoints, cg, appPath);

        WriteFileForUnitInstrumentAnalysis(ea_entryPoints, cg, appPath);


    }

    private static void WriteFileForUnitInstrumentAnalysis(List<SootMethod> ea_entryPoints, CallGraph cg, String appPath) {

        //******************************生成initialIntent
        AndroidInfo androidInfo = new AndroidInfo(appPath, exceptionLogger);
        String packageName = androidInfo.getPackageName(appPath);
        Map<String, AXmlNode> eas = androidInfo.getEAs();

        Set<IntentInfo> intentInfoSet = new HashSet<>();
        for (SootMethod sootMethod : ea_entryPoints) {
            AXmlNode aXmlNode = eas.get(sootMethod.getDeclaringClass().getName());
            if (aXmlNode == null) {
                continue;

            }

            Set<Intent> intentSet=new LinkedHashSet<>();

            String componentName = sootMethod.getDeclaringClass().getName();
            String componentType = aXmlNode.getTag();

            Intent intent = new Intent();
            intent.targetComponent = componentName;
            intentSet.add(intent);

            List<AXmlNode> aXmlNodeIntentFilterList = aXmlNode.getChildrenWithTag("intent-filter");



            for (AXmlNode aXmlNodeIntentFilter : aXmlNodeIntentFilterList)//充分利用intentFilter的信息
            {



                Set<String> actionSet = new HashSet<>();
                actionSet.add(null);
                Set<String> categorySet = new HashSet<>();
                Set<IntentData> dataSet = new LinkedHashSet<>();
                dataSet.add(null);

                List<AXmlNode> aXmlNodeActionList = aXmlNodeIntentFilter.getChildrenWithTag("action");
                for (AXmlNode aXmlNodeAction : aXmlNodeActionList) {
                    AXmlAttribute<?> actionNameAttribute = aXmlNodeAction.getAttribute("name");

                    if (actionNameAttribute != null) {
                        String action = ("" + actionNameAttribute.getValue()).trim();
                        actionSet.add(action);
                    }

                }

                List<AXmlNode> aXmlNodeCategoryList = aXmlNodeIntentFilter.getChildrenWithTag("category");

                for (AXmlNode aXmlNodeCategory : aXmlNodeCategoryList) {
                    AXmlAttribute<?> categoryNameAttribute = aXmlNodeCategory.getAttribute("name");

                    if (categoryNameAttribute != null) {
                        String category = ("" + categoryNameAttribute.getValue()).trim();
                        categorySet.add(category);
                    }

                }

                List<AXmlNode> aXmlNodeDataList = aXmlNodeIntentFilter.getChildrenWithTag("data");

                for (AXmlNode aXmlNodeData : aXmlNodeDataList) {
                    String dataString = "";
                    AXmlAttribute<?> schemeAttribute = aXmlNodeData.getAttribute("scheme");

                    String scheme = null;

                    if (schemeAttribute != null) {
                        scheme = ("" + schemeAttribute.getValue()).trim();
                        dataString = dataString + scheme;
                    }

                    if (scheme != null && (!scheme.equals(""))) {
                        String host = null;
                        AXmlAttribute<?> hostAttribute = aXmlNodeData.getAttribute("host");

                        if (hostAttribute != null) {
                            host = ("" + hostAttribute.getValue()).trim();
                            dataString = dataString + "://" + host;
                        }

                        if (host != null && (!host.equals(""))) {
                            String port = null;
                            AXmlAttribute<?> portAttribute = aXmlNodeData.getAttribute("port");
                            if (portAttribute != null) {
                                port = ("" + portAttribute.getValue()).trim();
                                dataString = dataString + ":" + port;

                            }


                            String path = null;
                            AXmlAttribute<?> pathAttribute = aXmlNodeData.getAttribute("path");
                            if (pathAttribute != null) {
                                path = ("" + pathAttribute.getValue()).trim();
                                dataString = dataString + "/" + path;

                            }


                        }
                    }

                    String mimeType = "";

                    AXmlAttribute<?> mimeTypeAttribute = aXmlNodeData.getAttribute("mimeType");


                    if (mimeTypeAttribute != null) {
                        mimeType = ("" + mimeTypeAttribute.getValue()).trim();
                    }

                    if ((!mimeType.equals("")) || (!dataString.equals(""))) {

                        dataSet.add(new IntentData(dataString, mimeType));
                    }


                }


                for(String action:actionSet)
                {
                    for(IntentData intentData:dataSet)

                    {
                        Intent tempIntent = new Intent();
                        tempIntent.targetComponent = componentName;
                        tempIntent.data=intentData;
                        tempIntent.action=action;
                        tempIntent.categories.addAll(categorySet);

                        intentSet.add(tempIntent);
                    }
                }



            }

            for(Intent oneIntent : intentSet)//
            {
                IntentConditionTransformSymbolicExcutation.IntentUnit intentUnit = new IntentConditionTransformSymbolicExcutation.IntentUnit(oneIntent, null, componentType, componentName);
                intentInfoSet.add(IntentConditionTransformSymbolicExcutation.getIntentInfo(intentUnit, appPath, packageName));
            }




        }

        IntentInfoFileGenerate.generateIntentInfoFile(appPath, new ArrayList<>(intentInfoSet), exceptionLogger);

        //******************************生成initialIntent


        IntentDataFlowAnalysisForDynamicSE.clearIntentDataFlowAnalysisForDynamicSE();//

        for (SootMethod sootMethod : ea_entryPoints) {


            IntentDataTransfer initialIntentDataTransfer = new IntentDataTransfer();
            initialIntentDataTransfer.targetSootMethod = sootMethod;


            for (int index = 0; index < sootMethod.getParameterTypes().size(); index++) {
                if (sootMethod.getParameterTypes().get(index).toString().equals("android.content.Intent")) {
                    initialIntentDataTransfer.type = IntentDataTransfer.TYPE_INTENT;
                    initialIntentDataTransfer.targetParameter = index;
                }


            }


            if (Util.isApplicationMethod(initialIntentDataTransfer.targetSootMethod) && initialIntentDataTransfer.targetSootMethod.hasActiveBody()) {

                MethodSummary methodSummary = new MethodSummary(initialIntentDataTransfer.targetSootMethod, initialIntentDataTransfer.targetParameter);
                IntentDataFlowAnalysisForDynamicSE.methodSummarySet.add(methodSummary);

                BriefUnitGraph briefUnitGraph = new BriefUnitGraph(initialIntentDataTransfer.targetSootMethod.getActiveBody());
                IntentDataFlowAnalysisForDynamicSE intentDataFlowAnalysisForDynamicSE = new IntentDataFlowAnalysisForDynamicSE(briefUnitGraph, initialIntentDataTransfer, exceptionLogger);

                IntentDataFlowAnalysisForDynamicSE.allInstrumentUnitIntentDataFlowIn.putAll(intentDataFlowAnalysisForDynamicSE.instrumentUnitIntentDataFlowIn);


            }


        }

        Set<InstrumentInfo> allInstrumentInfo = new HashSet<>();
        WriteFile allWriteFile = new WriteFile(Config.DynamicSE_logDir + "/" + "instrument_unit_get_info.log", true, exceptionLogger);
        for (Map.Entry<MethodUnit, IntentDataTransfer> entry : IntentDataFlowAnalysisForDynamicSE.allInstrumentUnitIntentDataFlowIn.entrySet()) {

//            allWriteFile.writeStr(appPath + "##" + entry.getKey() + "##" + entry.getValue().type + "##" + entry.getValue().id + "\n");
//            allWriteFile.flush();


            if (!entry.getValue().type.equals(IntentDataTransfer.TYPE_INTENT))//intent data doesn't need instrument
            {
                SootMethod sootMethod = entry.getKey().sootMethod;
                IfStmt ifStmt = (IfStmt) entry.getKey().unit;
                ConditionExpr condition = (ConditionExpr) ifStmt.getCondition();
                Value conditionLeft = condition.getOp1();
                Value conditionRight = condition.getOp2();
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
                                    if (valueBox.getValue() instanceof Local) {
                                        allInstrumentInfo.add(new InstrumentInfo(entry.getKey().sootMethod, definitionStmt, valueBox.getValue().toString(), valueBox.getValue().getType().toString(), true, entry.getValue().id, true));
                                    } else if (valueBox.getValue() instanceof Constant) {
                                        allInstrumentInfo.add(new InstrumentInfo(entry.getKey().sootMethod, definitionStmt, valueBox.getValue().toString(), valueBox.getValue().getType().toString(), false, entry.getValue().id, true));
                                    } else {
                                        exceptionLogger.warn("can't handle -----not local or constant ");
                                    }


                                }
                            }

                            if (entry.getValue().type.contains(IntentDataTransfer.TYPE_EXTRA)) {
                                Unit extraUnit = entry.getValue().whereGenUnit;
                                SootMethod extraSootMethod = entry.getValue().whereGenSootMethod;
                                DefinitionStmt definitionStmtExtra = (DefinitionStmt) extraUnit;
                                InvokeExpr invokeExprGetExtra = definitionStmtExtra.getInvokeExpr();
                                //for (int i = 0; i < invokeExprGetExtra.getArgs().size(); i++) {
                                if (invokeExprGetExtra.getArgs().size() >= 1)//extra default value is not considered
                                {
                                    Value arg = invokeExprGetExtra.getArgs().get(0);
                                    if (arg instanceof Local) {
                                        allInstrumentInfo.add(new InstrumentInfo(extraSootMethod, definitionStmtExtra, arg.toString(), arg.getType().toString(), true, entry.getValue().id, false));
                                    } else if (arg instanceof Constant) {
                                        allInstrumentInfo.add(new InstrumentInfo(extraSootMethod, definitionStmtExtra, arg.toString(), arg.getType().toString(), false, entry.getValue().id, false));
                                    } else {
                                        exceptionLogger.warn("can't handle -----not local or constant ");
                                    }
                                }

                                // }


                                if (valueBox.getValue().getType() instanceof PrimType || valueBox.getValue().getType().toString().equals("java.lang.String")) {

                                    if (valueBox.getValue() instanceof Local) {
                                        allInstrumentInfo.add(new InstrumentInfo(entry.getKey().sootMethod, definitionStmt, valueBox.getValue().toString(), valueBox.getValue().getType().toString(), true, entry.getValue().id, true));
                                    } else if (valueBox.getValue() instanceof Constant) {
                                        allInstrumentInfo.add(new InstrumentInfo(entry.getKey().sootMethod, definitionStmt, valueBox.getValue().toString(), valueBox.getValue().getType().toString(), false, entry.getValue().id, true));
                                    } else {
                                        exceptionLogger.warn("can't handle -----not local or constant ");
                                    }


                                }
                            }

                            allWriteFile.writeStr(valueBox.getValue().getType().toString() + "\n");
                            allWriteFile.flush();
                        }
                    }
                }

                if (conditionRight instanceof Constant) {
                    allInstrumentInfo.add(new InstrumentInfo(entry.getKey().sootMethod, ifStmt, conditionRight.toString(), conditionRight.getType().toString(), false, entry.getValue().id, true));
                }

                if (conditionRight instanceof Local) {
                    allInstrumentInfo.add(new InstrumentInfo(entry.getKey().sootMethod, ifStmt, conditionRight.toString(), conditionRight.getType().toString(), true, entry.getValue().id, true));
                }
            }


        }

        allWriteFile.close();

        WriteFile writeUnitsInstrumentGetInfo = new WriteFile(appPath + "_" + "UnitsInstrumentGetInfo.txt", false, exceptionLogger);


        for (InstrumentInfo instrumentInfo : allInstrumentInfo) {


            if (instrumentInfo.point.getTag("BytecodeOffsetTag") == null) {
                exceptionLogger.error("instrumentInfo.point.getTag(\"BytecodeOffsetTag\")==null" + instrumentInfo.point);
            } else {
                writeUnitsInstrumentGetInfo.writeStr(instrumentInfo.sootMethod.getBytecodeSignature() + "#" + instrumentInfo.point.getTag("BytecodeOffsetTag") + "#" + instrumentInfo.name + "#" + instrumentInfo.type + "#" + instrumentInfo.isLocal + "#" + instrumentInfo.id + "#" + instrumentInfo.isIf + "#" + instrumentInfo.point.toString() + "\n");
            }

        }
        writeUnitsInstrumentGetInfo.close();


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

            appDirPath = Config.dynamicSEAppDir;

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

            WriteFile writeFileGetInfoTimeUse=new WriteFile(Config.DynamicSE_logDir + "/" + appDir.getName() + "_GetInfo_timeUse.csv",false,exceptionLogger);
            for (File apkFile : appDir.listFiles()) {
                if (apkFile.getName().endsWith(".apk") && (!apkFile.getName().contains("_signed_zipalign"))) {
                    if (hasGenerateAppSet.contains(apkFile.getAbsolutePath())) {
                        continue;
                    }


                    Thread childThread = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {

                                long start_time=System.nanoTime();
                                singleAPPAnalysis(apkFile.getAbsolutePath());//分析每一个app
                                long end_time=System.nanoTime();

                                writeFileGetInfoTimeUse.writeStr(apkFile.getAbsolutePath()+","+(end_time-start_time)+"\n");

                                writeFileGetInfoTimeUse.flush();

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

            writeFileGetInfoTimeUse.close();

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

        //List<SootMethod> all_entryPoints=Util.getAll_EntryPoints(androidCallGraph, androidInfo);
        
        //generateAllString(all_entryPoints,cGraph, appPath);

        generateUnitToAnalysis(ea_entryPoints, cGraph, appPath);

        infoLogger.info("Part1's analysis completed! " + appPath);
    }

    private static void generateAllString(List<SootMethod> all_entryPoints, CallGraph cGraph, String appPath) {

        List<SootMethod> roMethods = Util.getMethodsInReverseTopologicalOrder(all_entryPoints, cGraph);


        WriteFile writeFileUnitsAllString = null;

        writeFileUnitsAllString = new WriteFile(appPath + "_" + "allStrMayContainAction.txt", false, exceptionLogger);

        Set<String> stringSet=new HashSet<>();
        for(SootMethod sootMethod:roMethods)
        {
            if(sootMethod.hasActiveBody())
            {
                for(Unit unit:sootMethod.getActiveBody().getUnits())
                {
                    for(ValueBox valueBox:unit.getUseBoxes())
                    {
                        if(valueBox.getValue() instanceof StringConstant)
                        {
                            StringConstant stringConstant= (StringConstant) valueBox.getValue();
                            String str=stringConstant.value;
                            if(str.startsWith("com.") || str.startsWith("android."))
                            {
                                stringSet.add(str);
                            }

                        }
                    }
                }
            }
        }

        for(String str:stringSet)
        {
            writeFileUnitsAllString.writeStr(str+"\n");
        }

        writeFileUnitsAllString.close();

    }


    private static boolean strContainsTwoPoint(String str) {
        int count = 0;
        int index = -1;
        while (true) {


            index = str.indexOf(".", index + 2);


            if (index != -1) {
                count++;
            }
            else
            {
                break;
            }
        }

        if (count >= 2) {
            return true;
        } else {
            return false;
        }


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

