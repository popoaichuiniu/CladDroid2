package com.popoaichuiniu.jacy.statistic;

import com.popoaichuiniu.intentGen.IntentPropagateAnalysis;
import com.popoaichuiniu.util.*;
import org.apache.log4j.Logger;
import soot.*;
import soot.jimple.*;
import soot.jimple.infoflow.android.axml.AXmlAttribute;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;

import java.io.File;
import java.util.*;

class TargetComponent {
    public static int ACTION = 0;
    public static int COMPONENT_NAME = 1;
    public static int HYBIRD = 2;
    int type = -1;

    String[] hybirdValues = new String[2];

    public TargetComponent(int type, String value) {
        this.type = type;
        hybirdValues[type] = value;
    }

    public TargetComponent() {

    }

    public TargetComponent(TargetComponent targetComponent) {
        this.type = targetComponent.type;
        for (int i = 0; i < 2; i++) {
            hybirdValues[i] = targetComponent.hybirdValues[i];
        }
    }

    public int isInternalIntent(Set<String> actions, Set<String> components)//actions 自定义action值集合
    {
        if (type == -1) {
            return -1;
        }

        if (type == TargetComponent.HYBIRD || type == TargetComponent.COMPONENT_NAME) {

            String componentStringSet = hybirdValues[TargetComponent.COMPONENT_NAME].replaceAll("/", ".");

            Set<String> componentSet = new HashSet<>();

            for (String str : componentStringSet.split("#")) {
                componentSet.add(str);
            }


            for (String oneComponent : componentSet) {
                if (components.contains(oneComponent)) {
                    return 1;
                }
            }


        }


        if (type == TargetComponent.ACTION) {
            Set<String> actionSet = new HashSet<>();
            for (String str : hybirdValues[TargetComponent.ACTION].split("#")) {
                actionSet.add(str);
            }

            for (String act : actionSet) {

                if (actions.contains(act)) {
                    return 1;
                }

            }
        }


        return 0;


    }

    public void mixTargetComponent(TargetComponent targetComponent) {
        if (targetComponent != null && targetComponent.type != -1) {

            if (targetComponent.type != TargetComponent.HYBIRD) {
                if (this.hybirdValues[targetComponent.type] == null) {
                    this.hybirdValues[targetComponent.type] = targetComponent.hybirdValues[targetComponent.type];
                } else {
                    if (targetComponent.hybirdValues[targetComponent.type] != null) {
                        this.hybirdValues[targetComponent.type] = this.hybirdValues[targetComponent.type] + "#" + targetComponent.hybirdValues[targetComponent.type];
                    }
                }

            } else {
                for (int i = 0; i < targetComponent.type; i++) {
                    if (this.hybirdValues[i] == null) {
                        this.hybirdValues[i] = targetComponent.hybirdValues[i];
                    } else {
                        if (targetComponent.hybirdValues[i] != null) {
                            this.hybirdValues[i] = this.hybirdValues[i] + "#" + targetComponent.hybirdValues[i];
                        }
                    }
                }
            }

            if (hybirdValues[0] == null && hybirdValues[1] == null) {
                this.type = -1;
            }

            if (hybirdValues[0] != null && hybirdValues[1] == null) {
                this.type = 0;
            }

            if (hybirdValues[0] == null && hybirdValues[1] != null) {
                this.type = 1;
            }
            if (hybirdValues[0] != null && hybirdValues[1] != null) {
                this.type = 2;
            }


        }


    }


    public void updateTargetComponent(TargetComponent targetComponent) {
        if (targetComponent != null && targetComponent.type != -1) {

            if (targetComponent.type != TargetComponent.HYBIRD) {

                this.hybirdValues[targetComponent.type] = targetComponent.hybirdValues[targetComponent.type];


            } else {
                for (int i = 0; i < targetComponent.type; i++) {

                    this.hybirdValues[i] = targetComponent.hybirdValues[i];


                }
            }

            if (hybirdValues[0] == null && hybirdValues[1] == null) {
                this.type = -1;
            }

            if (hybirdValues[0] != null && hybirdValues[1] == null) {
                this.type = 0;
            }

            if (hybirdValues[0] == null && hybirdValues[1] != null) {
                this.type = 1;
            }
            if (hybirdValues[0] != null && hybirdValues[1] != null) {
                this.type = 2;
            }


        }

    }

    @Override
    public String toString() {

        String result = "";
        if (type == 2) {
            result = "TargetComponent{" +
                    "action=" + hybirdValues[0] +
                    ",componentName=" + hybirdValues[1] +
                    "}";

        } else if (type == 0) {
            result = "TargetComponent{" +
                    "action=" + hybirdValues[0] +
                    "}";
        } else if (type == 1) {
            result = "TargetComponent{" +
                    "componentName=" + hybirdValues[1] +
                    "}";
        } else {
            result = "null";
        }

        return result;


    }


    public boolean isHasInfo() {
        if (type == -1) {
            return false;
        } else {
            return true;
        }
    }
}

class IntentStatus {
    int isExplicit = -1;
    int isStartInternalComponet = -1;


    public IntentStatus() {

    }

    public int isExplicitInternal() {
        if (isExplicit == -1 || isStartInternalComponet == -1) {
            return -1;
        }

        if (isExplicit == 1 && isStartInternalComponet == 1) {
            return 1;
        } else {
            return 0;
        }
    }


}

public class IntentImplicitUse {

    private static String intentImplicitUseLogger=Config.logDir+"/IntentImplicitUse";
    private static Logger exceptionlogger = new MyLogger(intentImplicitUseLogger, "appException").getLogger();
    private static Logger error = new MyLogger(intentImplicitUseLogger, "appError").getLogger();
    private static Logger infoLogger = new MyLogger(intentImplicitUseLogger, "IntentImplicitUse_Info").getLogger();
    private static Logger targetComponentLogger = new MyLogger(intentImplicitUseLogger, "targetComponentLogger").getLogger();

    private static WriteFile startIntent = new WriteFile(intentImplicitUseLogger+"/startIntentUseStatus.txt", false, exceptionlogger);

    public static Set<String> iccMethods = new HashSet<>();

    public static Set<String> intentSetTargetMethods = new HashSet<>();

    public static Set<String> systemActions = new HashSet<>();

    private String appPath = null;

    private int allCount = 0;

    private int explicitCount = 0;

    private static long resultAllCount = 0;

    private static long resultExplicitCount = 0;


    private static WriteFile results = new WriteFile(intentImplicitUseLogger+"/result.txt", false, exceptionlogger);


    public Set<String> actions  = new HashSet<>();//自定义actions

    public Map<String, Set<String>> actionComponentName = new HashMap<>();//actionsMap

    public Set<String> componentsName = new HashSet<>();


    private CallGraph callGraph = null;


    public IntentImplicitUse(String appPath) {//单独使用
        this.appPath = appPath;
        AndroidCallGraphProxy androidCallGraphProxy = new AndroidCallGraphProxy(appPath, Config.androidJar, exceptionlogger);
        callGraph = androidCallGraphProxy.androidCallGraph.getCg();
        AndroidInfo androidInfo = new AndroidInfo(appPath, exceptionlogger);
        getComponentInfo(androidInfo);
    }


    public IntentImplicitUse(String appPath, boolean isUseInAndroidCallGraph) {
        this.appPath = appPath;
        AndroidInfo androidInfo = new AndroidInfo(appPath, exceptionlogger);
        getComponentInfo(androidInfo);
    }


    static {
        Set<String> iccContent = new ReadFileOrInputStream("EANotProper/ICC.txt", exceptionlogger).getAllContentLinSet();

        for (String str : iccContent) {
            String[] temp = str.split("\\(");
            iccMethods.add(temp[0].trim());
        }

        Set<String> intentMethodAboutTargetDataContent = new ReadFileOrInputStream("EANotProper/intentMethodAboutTargetData.txt", exceptionlogger).getAllContentLinSet();

        for (String str : intentMethodAboutTargetDataContent) {
            String[] temp = str.split("\\(");
            intentSetTargetMethods.add(temp[0].trim());
        }


        Set<String> systemActionsContent = new ReadFileOrInputStream("EANotProper/system_actions.txt", exceptionlogger).getAllContentLinSet();

        for (String str : systemActionsContent) {
            systemActions.add(str.trim());
        }
    }

    public static void main(String[] args) {


        String appDirPath = Config.wandoijiaAPP;
        File appDir = new File(appDirPath);
        for (File file : appDir.listFiles()) {
            if (file.getName().endsWith(".apk")) {

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {


                        try {
                            IntentImplicitUse intentImplicitUse = new IntentImplicitUse(file.getAbsolutePath());
                            startIntent.writeStr("************************" + file.getAbsolutePath() + "\n");
                            intentImplicitUse.doAnalysisOneApk();
                            startIntent.writeStr("******************************************************");
                        } catch (Throwable t) {
                            if (t instanceof Exception) {
                                Exception e = (Exception) t;
                                exceptionlogger.error(file.getAbsolutePath() + "##" + e.getMessage() + "&&" + ExceptionStackMessageUtil.getStackTrace(e));
                            } else {
                                error.error(file.getAbsolutePath() + "##" + t.getMessage() + "&&" + ExceptionStackMessageUtil.getStackTrace(t));
                            }

                        }


                    }
                });

                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException e) {

                }


            }
        }

        results.writeStr("result**********************************************************" + "\n");
        results.writeStr(resultExplicitCount + " " + resultAllCount + "\n");
        results.close();
        startIntent.close();


    }


    private void doAnalysisOneApk() {


        Set<SootMethod> sootMethodSet = new HashSet<>();
        for (Iterator<Edge> iterator = callGraph.iterator(); iterator.hasNext(); ) {
            Edge edge = iterator.next();
            sootMethodSet.add(edge.getSrc().method());
            sootMethodSet.add(edge.tgt().method());


        }

        for (SootMethod sootMethod : sootMethodSet) {

            doAnalysisOneSootMethod(sootMethod);

        }

        resultAllCount = resultAllCount + allCount;
        resultExplicitCount = resultExplicitCount + explicitCount;

        results.writeStr(explicitCount + " " + allCount + " " + appPath + "\n");
        results.flush();


    }

    private void getComponentInfo(AndroidInfo androidInfo) {


        Map<String, AXmlNode> components = androidInfo.getComponents();

        for (Map.Entry<String, AXmlNode> entry : components.entrySet()) {
            AXmlNode aXmlNode = entry.getValue();
            AXmlAttribute<?> componentName = aXmlNode.getAttribute("name");
            if (componentName != null) {
                componentsName.add(((String) componentName.getValue()).trim());
            }
            List<AXmlNode> intentFilterList = aXmlNode.getChildrenWithTag("intent-filter");
            for (AXmlNode aXmlNodeIntentFilter : intentFilterList) {
                List<AXmlNode> actionIntentFilterList = aXmlNodeIntentFilter.getChildrenWithTag("action");

                for (AXmlNode actionIntentFilter : actionIntentFilterList) {
                    AXmlAttribute<?> actionName = actionIntentFilter.getAttribute("name");
                    if (actionName != null) {
                        actions.add(((String) actionName.getValue()).trim());
                        Set<String> actComSet = actionComponentName.get(((String) actionName.getValue()).trim());
                        if (actComSet == null) {
                            actComSet = new HashSet<>();
                        }
                        actComSet.add(((String) componentName.getValue()).trim());
                        actionComponentName.put(((String) actionName.getValue()).trim(), actComSet);
                    }


                }


            }


        }

        actions.removeAll(systemActions);
        for (String sysAct : systemActions) {
            actionComponentName.remove(sysAct);
        }


    }


    private void doAnalysisOneSootMethod(SootMethod sootMethod) {


        if (!Util.isApplicationMethod(sootMethod)) {
            return;
        }
        Body body = null;
        try {
            body = sootMethod.getActiveBody();
        } catch (RuntimeException e) {
            //exceptionlogger.error(appPath + "##" + e.getMessage() + "&&" + ExceptionStackMessageUtil.getStackTrace(e));
            return;
        }

        for (Unit unit : body.getUnits()) {


            doAnalysisOnICC(sootMethod, unit);
        }
    }


    private void doAnalysisOnICC(SootMethod sootMethod, Unit unit) {
        Stmt stmt = (Stmt) unit;
        SootMethod calleeSootMethod = Util.getCalleeSootMethodAt(unit);
        if (calleeSootMethod != null) {
            if (iccMethods.contains(calleeSootMethod.getName())) {
                InvokeExpr invokeExpr = stmt.getInvokeExpr();
                for (Value value : invokeExpr.getArgs()) {
                    if (value.getType().toString().equals("android.content.Intent")) {


                        IntentStatus intentStatus = new IntentStatus();
                        startIntent.writeStr("Intent:\n");
                        //int flag=-1;
                        int flag = doAnalysisIntentIsExplicit(value, stmt, sootMethod);//有问题

                        intentStatus.isExplicit = flag;

                        if (flag != -1) {

                            if (flag == 1) {
                                startIntent.writeStr("vvvv" + "\n");
                            } else {
                                startIntent.writeStr("xxxx" + "\n");
                            }
                            startIntent.writeStr("\n\n");
                            startIntent.flush();
                        }


                        TargetComponent targetComponent = doAnalysisIntentGetTargetComponent(value, stmt, stmt, sootMethod);
                        if (targetComponent != null) {

                            targetComponentLogger.info(targetComponent.toString());

                            int result = targetComponent.isInternalIntent(actions, componentsName);

                            intentStatus.isStartInternalComponet = result;


                        }

                        if (intentStatus.isExplicitInternal() != -1) {
                            allCount++;

                            if (intentStatus.isExplicitInternal() == 1) {
                                explicitCount++;
                            }
                        }


                    }
                }
            }
        }
    }


    class MethodIntentUnit {//保存iccUnit逆向分析的终结点，也就是defUnit 和所属的SootMethod
        Unit unit;
        SootMethod sootMethod;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodIntentUnit that = (MethodIntentUnit) o;
            return Objects.equals(unit, that.unit);
        }

        @Override
        public int hashCode() {
            return Objects.hash(unit);
        }

        public MethodIntentUnit(Unit unit, SootMethod sootMethod) {
            this.unit = unit;
            this.sootMethod = sootMethod;
        }
    }


    class UnitAndLocal {//用于判断是否该方法是否已经分析过了,用于 从iccUnit到defUnit逆向传播过程中
        Local intentLocal;
        Unit useIntentLocalUnit;
        SootMethod sootMethod;//useIntentLocalUnit所在的方法

        public UnitAndLocal(Local intentLocal, Unit useIntentLocalUnit, SootMethod sootMethod) {
            this.intentLocal = intentLocal;
            this.useIntentLocalUnit = useIntentLocalUnit;
            this.sootMethod = sootMethod;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UnitAndLocal that = (UnitAndLocal) o;
            if (sootMethod.getBytecodeSignature().equals(that.sootMethod.getBytecodeSignature())) {
                return Objects.equals(intentLocal, that.intentLocal) &&
                        Objects.equals(useIntentLocalUnit, that.useIntentLocalUnit);

            } else {
                return false;
            }

        }

        @Override
        public int hashCode() {
            return Objects.hash(intentLocal, useIntentLocalUnit);
        }
    }


    class StartUnitOrParameterIndex {//用于判断是否该方法已经分析过了 从intent defUnit到iccUnit的正向分析过程中
        SootMethod sootMethod;
        Unit startUnit;
        int parameterIndex = -1;

        public StartUnitOrParameterIndex(SootMethod sootMethod, Unit startUnit, int parameterIndex) {
            this.sootMethod = sootMethod;
            this.startUnit = startUnit;
            this.parameterIndex = parameterIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StartUnitOrParameterIndex that = (StartUnitOrParameterIndex) o;
            if (sootMethod.getBytecodeSignature().equals(that.sootMethod.getBytecodeSignature())) {
                if (startUnit == that.startUnit || (parameterIndex != -1 && that.parameterIndex != -1 && parameterIndex == that.parameterIndex)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sootMethod);
        }
    }


    class IntentNode {
        SootMethod sootMethod;

        Set<IntentNode> nexts;

        public IntentNode(SootMethod sootMethod) {
            this.sootMethod = sootMethod;
            nexts = new HashSet<>();
        }

        @Override
        public String toString() {
            return sootMethod.getBytecodeSignature();
        }
    }

    class IntentNodeFactory {


        private Map<SootMethod, IntentNode> map = new HashMap<>();


        IntentNode getIntentNode(SootMethod sootMethod) {
            IntentNode intentNode = map.get(sootMethod);
            if (intentNode == null) {
                intentNode = new IntentNode(sootMethod);
                map.put(sootMethod, intentNode);
            }
            return intentNode;
        }


    }


    public TargetComponent doAnalysisIntentGetTargetComponent(Value value, Unit useIntentUnit, Unit iccUnit, SootMethod sootMethod) {

        if (!(value instanceof Local)) {
            throw new RuntimeException("!(value instanceof Local)");
        }
        Local local = (Local) value;

        TargetComponent targetComponentUl = new TargetComponent();
        try {
            TargetComponent targetComponentIntra = intraMethod(local, useIntentUnit, sootMethod);
            targetComponentUl.updateTargetComponent(targetComponentIntra);
        } catch (RuntimeException e) {
            exceptionlogger.error(e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e));
        }


//        //时间太长
//        long beforeInterTime = System.nanoTime();
//        try {
//            TargetComponent targetComponentInter = interMethod(local, useIntentUnit, iccUnit, sootMethod);//多态没有解决，导致从defUnit出发找不到iccUnit，造成漏报,callgraph的多态解决不知道精确不精确
//
//            targetComponentUl.mixTargetComponent(targetComponentInter);//两个解混合
//        } catch (RuntimeException e) {
//            exceptionlogger.error(e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e));
//        }
//        long afterInterTime = System.nanoTime();
//
//        infoLogger.info("interMethod-time:" + ((afterInterTime - beforeInterTime) / 1E9) + "seconds");


        return targetComponentUl;


    }

    private TargetComponent interMethod(Local local, Unit useIntentUnit, Unit iccUnit, SootMethod sootMethod) {


        TargetComponent targetComponentUl = new TargetComponent();

        if (!sootMethod.hasActiveBody()) {
            return targetComponentUl;
        }

        Set<MethodIntentUnit> methodIntentUnitSet = new HashSet<>();

        long beforeGetIntentSource = System.nanoTime();
        getIntentPropagatePathSootMethodSet(methodIntentUnitSet, new HashSet<>(), local, useIntentUnit, sootMethod);
        long afterGetIntentSource = System.nanoTime();

        infoLogger.info("GetIntentSource-time:" + ((afterGetIntentSource - beforeGetIntentSource) / 1E9) + "seconds");


        long beforeComputeIntentTargetValue = System.nanoTime();
        for (MethodIntentUnit entry : methodIntentUnitSet) {

            Unit startUnit = entry.unit;
            SootMethod startSootMethod = entry.sootMethod;

            //一个 defUnit到iccmethod可能会有多个路径，对应多个TargetComponent解
            Set<Unit> intentPathUnit = new LinkedHashSet<>();
            List<TargetComponent> targetComponentList = new ArrayList<>();
            getIntentTargetComponent(targetComponentList, intentPathUnit, new HashSet<>(), new TargetComponent(), startUnit, startSootMethod, -1, iccUnit);

            if (targetComponentList.size() == 0) {
                infoLogger.warn("异常：" + "没有找到iccUnit:" + iccUnit + "\n" + Util.getPrintCollectionStr(intentPathUnit));
            }

            for (TargetComponent targetComponent : targetComponentList) {
                targetComponentUl.mixTargetComponent(targetComponent);
            }


        }

        long afterComputeIntentTargetValue = System.nanoTime();
        infoLogger.info("ComputeIntentTargetValue-time:" + ((afterComputeIntentTargetValue - beforeComputeIntentTargetValue) / 1E9) + "seconds");
        return targetComponentUl;
    }

    private TargetComponent intraMethod(Local local, Unit iccUnit, SootMethod sootMethod) {


        TargetComponent targetComponentUl = new TargetComponent();

        if (sootMethod.hasActiveBody()) {
            BriefUnitGraph briefUnitGraph = new BriefUnitGraph(sootMethod.getActiveBody());

            SimpleLocalDefs simpleLocalDefs = new SimpleLocalDefs(briefUnitGraph);

            for (Unit defUnit : simpleLocalDefs.getDefsOfAt(local, iccUnit)) {
                TargetComponent targetComponentOneDefUnit = new TargetComponent();
                IntentPropagateAnalysis intentPropagateAnalysis = new IntentPropagateAnalysis(sootMethod, briefUnitGraph, defUnit, -1);
                for (Unit unit : intentPropagateAnalysis.useIntentUnitSet) {
                    Stmt useIntentStmt = (Stmt) unit;
                    if (useIntentStmt.containsInvokeExpr()) {
                        InvokeExpr invokeExpr = useIntentStmt.getInvokeExpr();
                        if (invokeExpr instanceof InstanceInvokeExpr) {
                            InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;
                            if (instanceInvokeExpr.getBase().getType().toString().equals("android.content.Intent")) {//intent.setXX()
                                TargetComponent targetComponent = getTargetInfoFromOneUnit(sootMethod, briefUnitGraph, simpleLocalDefs, iccUnit, iccUnit, invokeExpr);
                                targetComponentOneDefUnit.updateTargetComponent(targetComponent);//不断更新targetComponent
                            }

                        }
                    }
                }

                targetComponentUl.mixTargetComponent(targetComponentOneDefUnit);


            }


        }


        return targetComponentUl;


    }

    private void getIntentTargetComponent(List<TargetComponent> targetComponentList, Set<Unit> intentPathUnitSet, Set<StartUnitOrParameterIndex> visited, TargetComponent targetComponentAll, Unit startUnit, SootMethod curSootMethod, int parameterIndex, Unit iccUnit) {

        if (curSootMethod.getDeclaringClass().getName().startsWith("android.")) {
            return;
        }

        StartUnitOrParameterIndex startUnitOrParameterIndex = new StartUnitOrParameterIndex(curSootMethod, startUnit, parameterIndex);


        if (visited.contains(startUnitOrParameterIndex)) {
            return;
        }
        visited.add(startUnitOrParameterIndex);


        if (curSootMethod.hasActiveBody()) {

            BriefUnitGraph briefUnitGraph = new BriefUnitGraph(curSootMethod.getActiveBody());
            SimpleLocalDefs simpleLocalDefs = new SimpleLocalDefs(briefUnitGraph);
            IntentPropagateAnalysis intentPropagateAnalysis = new IntentPropagateAnalysis(curSootMethod, briefUnitGraph, startUnit, parameterIndex);
            Set<Unit> useIntentUnitSet = intentPropagateAnalysis.useIntentUnitSet;


            for (Unit useIntentUnit : useIntentUnitSet) {//对每个使用intent语句分析
                intentPathUnitSet.add(useIntentUnit);
                boolean handleFlag = false;

                Stmt useIntentStmt = (Stmt) useIntentUnit;
                if (useIntentUnit == iccUnit) {
                    handleFlag = true;
                    String str = "";
                    for (Unit unit : intentPathUnitSet) {
                        str = str + unit + "\n";
                    }
                    infoLogger.info("喜报：" + "找到一条路径到:" + iccUnit + "\n" + targetComponentAll + "\n\n" + str);

                    targetComponentList.add(targetComponentAll);//找到一条路径

                }
                if (useIntentStmt.containsInvokeExpr()) {
                    handleFlag = true;

                    InvokeExpr invokeExpr = useIntentStmt.getInvokeExpr();
                    if (invokeExpr instanceof InstanceInvokeExpr) {
                        InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;
                        if (instanceInvokeExpr.getBase().getType().toString().equals("android.content.Intent")) {//intent.setXX()
                            TargetComponent targetComponent = getTargetInfoFromOneUnit(curSootMethod, briefUnitGraph, simpleLocalDefs, useIntentUnit, iccUnit, invokeExpr);
                            targetComponentAll.updateTargetComponent(targetComponent);//不断更新targetComponent
                        }

                    }

                    if (!invokeExpr.getMethod().getDeclaringClass().getName().equals("android.content.Intent"))//xxx(intent)
                    {


                        int index = -1;
                        for (int i = 0; i < invokeExpr.getArgs().size(); i++) {
                            if (invokeExpr.getArg(i).getType().toString().equals("android.content.Intent")) {
                                index = i;
                                break;
                            }
                        }

                        if (index != -1) {


                            SootMethod calleeSootMethodUseIntentArg = getCalleeMethod(invokeExpr, curSootMethod);

                            getIntentTargetComponent(targetComponentList, intentPathUnitSet, visited, targetComponentAll, null, calleeSootMethodUseIntentArg, index, iccUnit);


                        }


                    }

                }

                if (useIntentStmt instanceof ReturnStmt)//返回值   return intent
                {
                    handleFlag = true;
                    CallGraph cg = Scene.v().getCallGraph();


                    for (Iterator<Edge> iterator = cg.edgesInto(curSootMethod); iterator.hasNext(); ) {//多个都能到iccUnit怎么办？
                        Edge edge = iterator.next();
                        Unit scrUnit = edge.srcUnit();
                        if (scrUnit != null) {
                            SootMethod srcMethod = edge.src();
                            {


                                getIntentTargetComponent(targetComponentList, new LinkedHashSet<>(intentPathUnitSet), new HashSet<>(visited), new TargetComponent(targetComponentAll), scrUnit, srcMethod, -1, iccUnit);
                            }

                        }
                    }


                }


                if (!handleFlag) {
                    infoLogger.warn("不能处理" + useIntentUnit);
                }
            }


        }


    }


    private void getIntentPropagatePathSootMethodSet(Set<MethodIntentUnit> methodIntentUnitSet, Set<UnitAndLocal> visited, Local intentLocal, Unit useIntentLocalUnit, SootMethod curSootMethod) {

        if(curSootMethod.getDeclaringClass().getName().startsWith("android."))
        {
            return;
        }
        if (!Util.isApplicationMethod(curSootMethod)) {
            return;
        }


        if (!curSootMethod.hasActiveBody()) {
            return;
        }

        UnitAndLocal unitAndLocal = new UnitAndLocal(intentLocal, useIntentLocalUnit, curSootMethod);

        if (visited.contains(unitAndLocal)) {
            return;
        }


        visited.add(unitAndLocal);


        BriefUnitGraph briefUnitGraph = new BriefUnitGraph(curSootMethod.getActiveBody());
        SimpleLocalDefs simpleLocalDefs = new SimpleLocalDefs(briefUnitGraph);
        for (Unit defUnit : simpleLocalDefs.getDefsOfAt(intentLocal, useIntentLocalUnit)) {
            boolean handleFlag = false;
            DefinitionStmt defStmt = (DefinitionStmt) defUnit;
            if (defStmt.getRightOp() instanceof NewExpr)// $r2=new android.content.Intent //ok
            {
                handleFlag = false;
                //什么都不做


            } else if (defStmt.getRightOp() instanceof Local)//$r6 = $r13  ok
            {
                handleFlag = true;
                Local assignLocal = (Local) defStmt.getRightOp();
                getIntentPropagatePathSootMethodSet(methodIntentUnitSet, visited, assignLocal, defUnit, curSootMethod);
            } else if (defStmt.getRightOp() instanceof ParameterRef)//$r1 := @parameter0: android.content.Intent  ok
            {
                handleFlag = true;
                ParameterRef parameterRef = (ParameterRef) defStmt.getRightOp();
                CallGraph cg = Scene.v().getCallGraph();
                for (Iterator<Edge> iterator = cg.edgesInto(curSootMethod); iterator.hasNext(); ) {
                    Edge edge = iterator.next();
                    Unit scrUnit = edge.srcUnit();
                    if (scrUnit != null) {
                        SootMethod srcMethod = edge.src();
                        Stmt srcStmt = (Stmt) scrUnit;
                        if (srcStmt.containsInvokeExpr()) {
                            int index = parameterRef.getIndex();
                            Value intentValue = srcStmt.getInvokeExpr().getArg(index);
                            if (intentValue.getType().toString().equals("android.content.Intent")) {
                                if (intentValue instanceof Local) {
                                    Local paraIntentLocal = (Local) intentValue;
                                    getIntentPropagatePathSootMethodSet(methodIntentUnitSet, visited, paraIntentLocal, scrUnit, srcMethod);
                                } else {
                                    exceptionlogger.error("未预料的错误！intentValue instanceof Local ");

                                }

                            } else {

                                exceptionlogger.error("未预料的错误！intentValue.getType().toString().equals(\"android.content.Intent\")");

                            }
                        } else {
                            exceptionlogger.error("未预料的错误！stmt.containsInvokeExpr()");
                        }

                    }

                }

            } else if (defStmt.getRightOp() instanceof InvokeExpr) {//ok
                InvokeExpr invokeExpr = (InvokeExpr) defStmt.getRightOp();
                if (invokeExpr.getMethod().getDeclaringClass().getName().equals("android.content.Intent")) {
                    if (invokeExpr.getMethod().getName().equals("<init>") && invokeExpr.getArgs().size() == 1 && invokeExpr.getArg(0).getType().toString().equals("android.content.Intent")) {//$r1= new intent(intent o)
                        handleFlag = true;
                        Local initIntentLocal = (Local) invokeExpr.getArg(0);
                        getIntentPropagatePathSootMethodSet(methodIntentUnitSet, visited, initIntentLocal, defUnit, curSootMethod);
                    }
                } else {//非Intent方法
                    SootMethod returnIntentMethod = getCalleeMethod(invokeExpr, curSootMethod);//$r1= xxx();

                    if (returnIntentMethod.hasActiveBody()) {
                        handleFlag = true;
                        Body body = returnIntentMethod.getActiveBody();
                        for (Unit unit : body.getUnits()) {
                            if (unit instanceof ReturnStmt) {
                                ReturnStmt returnStmt = (ReturnStmt) unit;
                                Value intentValue = returnStmt.getOp();
                                if (intentValue instanceof Local) {//可能返回null
                                    if (intentValue.getType().toString().equals("android.content.Intent")) {
                                        getIntentPropagatePathSootMethodSet(methodIntentUnitSet, visited, (Local) intentValue, returnStmt, returnIntentMethod);
                                    } else {

                                        exceptionlogger.error("未预料的错误！intentValue.getType().toString().equals(\"android.content.Intent\")");
                                    }

                                }

                            }
                        }
                    }
                }
            } else {


                infoLogger.info("intent逆向传播未处理intent defUnit：" + defUnit);

            }

            if (!handleFlag) {
                MethodIntentUnit methodIntentUnit = new MethodIntentUnit(defUnit, curSootMethod);

                methodIntentUnitSet.add(methodIntentUnit);

            }


        }

    }

    private SootMethod getCalleeMethod(InvokeExpr invokeExpr, SootMethod curMethod) {//多态导致,callgraph的有做指向分析。知道是具体哪一个类，但是可能不够精确，暂时不知道

        SootMethod invokeSootMethod = invokeExpr.getMethod();
        Hierarchy hierarchy = Scene.v().getActiveHierarchy();
        for (SootMethod calleeMethod : Util.cgOutOfSootMethods(curMethod)) {

            SootClass invokeSootClass = invokeSootMethod.getDeclaringClass();
            SootClass calleeMethodClass = calleeMethod.getDeclaringClass();
            if (invokeSootClass.isInterface()) {
                List<SootClass> subIncluding = hierarchy.getSubinterfacesOfIncluding(invokeSootClass);
                if (subIncluding == null || (!subIncluding.contains(calleeMethodClass))) {
                    continue;
                }

            } else {
                List<SootClass> subIncluding = hierarchy.getSubclassesOfIncluding(invokeSootClass);
                if (subIncluding == null || (!subIncluding.contains(calleeMethodClass))) {
                    continue;
                }
            }

            if (invokeSootMethod.getName().equals(calleeMethod.getName()))//方法名相同
            {
                if (invokeSootMethod.getBytecodeParms().equals(calleeMethod.getBytecodeParms()))//参数相同
                {
                    return calleeMethod;
                }
            }


        }

        return invokeSootMethod;//会不会存在一个方法同时调用父类的和子类的方法，那么这个方法就存在问题了，不考虑这个问题


    }


    private TargetComponent getTargetInfoFromOneUnit(SootMethod sootMethod, BriefUnitGraph briefUnitGraph, SimpleLocalDefs simpleLocalDefs, Unit useUnit, Unit iccUnit, InvokeExpr invokeExpr) {

        TargetComponent targetComponentAll = new TargetComponent();

        if (invokeExpr instanceof InstanceInvokeExpr) {

            SootMethod calleeSootMethod = invokeExpr.getMethod();
            InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;

            if (instanceInvokeExpr.getBase().getType().toString().equals("android.content.Intent")) {

                if (calleeSootMethod.getName().equals("<init>")) {

                    if (instanceInvokeExpr.getArgs().size() == 1) {
                        Value arg = instanceInvokeExpr.getArg(0);
                        if (arg.getType().toString().contains("android.content.Intent")) {//Intent(Intent o)

//                            TargetComponent targetComponent = doAnalysisIntentGetTargetComponent(arg, useUnit, iccUnit, sootMethod);//-------------------------------------这里有问题
//                            targetComponentAll.updateTargetComponent(targetComponent);
                            infoLogger.info("Intent(Intent o)");

                        }

                        if (arg.getType().toString().contains("java.lang.String")) {//Intent(String action)
                            TargetComponent targetComponent = doAnalysisOnStringTypeValue(TargetComponent.ACTION, arg, useUnit, sootMethod, simpleLocalDefs);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }

                    }

                    if (instanceInvokeExpr.getArgs().size() == 2) {
                        Value arg = instanceInvokeExpr.getArg(0);
                        if (arg.getType().toString().contains("java.lang.String")) {//Intent(String action, Uri uri)
                            TargetComponent targetComponent = doAnalysisOnStringTypeValue(TargetComponent.ACTION, arg, useUnit, sootMethod, simpleLocalDefs);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }

                        Value arg1 = instanceInvokeExpr.getArg(1);

                        if (arg1.getType().toString().contains("java.lang.Class")) {//Intent(Context packageContext, Class<?> cls)

                            TargetComponent targetComponent = doAnalysisOnClassTypeValue(arg1, useUnit, sootMethod, simpleLocalDefs);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }


                    }

                    if (instanceInvokeExpr.getArgs().size() == 4) {//Intent(String action, Uri uri, Context packageContext, Class<?> cls)
                        Value arg = instanceInvokeExpr.getArg(0);
                        if (arg.getType().toString().contains("java.lang.String")) {
                            TargetComponent targetComponent = doAnalysisOnStringTypeValue(TargetComponent.ACTION, arg, useUnit, sootMethod, simpleLocalDefs);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }

                        Value arg3 = instanceInvokeExpr.getArg(3);
                        if (arg3.getType().toString().contains("java.lang.Class")) {

                            TargetComponent targetComponent = doAnalysisOnClassTypeValue(arg3, useUnit, sootMethod, simpleLocalDefs);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }

                    }

                }

                if (calleeSootMethod.getName().equals("setClass")) {//	setClass(Context packageContext, Class<?> cls)
                    for (Value arg : instanceInvokeExpr.getArgs()) {
                        if (arg.getType().toString().contains("java.lang.Class")) {

                            TargetComponent targetComponent = doAnalysisOnClassTypeValue(arg, useUnit, sootMethod, simpleLocalDefs);
                            targetComponentAll.updateTargetComponent(targetComponent);

                        }


                    }
                }

                if (calleeSootMethod.getName().equals("setClassName")) {//1.	setClassName(String packageName, String className)
                    //2.  setClassName(Context packageContext, String className)


                    TargetComponent targetComponent = computeTwoStringPackageCLassName(sootMethod, simpleLocalDefs, useUnit, instanceInvokeExpr);
                    targetComponentAll.updateTargetComponent(targetComponent);


                }

                if (calleeSootMethod.getName().equals("setComponent")) {//	setComponent(ComponentName component)

                    TargetComponent targetComponent = doAnalysisOnComponentTypeValue(instanceInvokeExpr.getArg(0), useUnit, sootMethod, simpleLocalDefs, briefUnitGraph);
                    targetComponentAll.updateTargetComponent(targetComponent);


                }

                if (calleeSootMethod.getName().equals("setAction"))//Intent setAction (String action)
                {
                    TargetComponent targetComponent = doAnalysisOnStringTypeValue(TargetComponent.ACTION, instanceInvokeExpr.getArg(0), useUnit, sootMethod, simpleLocalDefs);
                    targetComponentAll.updateTargetComponent(targetComponent);
                }

            }


        }

        return targetComponentAll;
    }

    private TargetComponent computeTwoStringPackageCLassName(SootMethod sootMethod, SimpleLocalDefs simpleLocalDefs, Unit useUnit, InvokeExpr invokeExpr) {
        String packageName = "";
        String className = "";
        if (invokeExpr.getArg(0).getType().toString().equals("java.lang.String")) {
            TargetComponent targetComponentPackage = doAnalysisOnStringTypeValue(TargetComponent.COMPONENT_NAME, invokeExpr.getArg(0), useUnit, sootMethod, simpleLocalDefs);


            if (targetComponentPackage != null && targetComponentPackage.type != -1) {
                packageName = targetComponentPackage.hybirdValues[targetComponentPackage.type];
            }


        }

        if (invokeExpr.getArg(1).getType().toString().equals("java.lang.String")) {
            TargetComponent targetComponentClassName = doAnalysisOnStringTypeValue(TargetComponent.COMPONENT_NAME, invokeExpr.getArg(1), useUnit, sootMethod, simpleLocalDefs);
            if (targetComponentClassName != null && targetComponentClassName.type != -1) {
                className = targetComponentClassName.hybirdValues[targetComponentClassName.type];
            }
        }


        String result = null;
        if (className.length() != 0) {
            result = className;
        } else {
            if (packageName.length() != 0) {
                result = packageName;
            }
        }

        if (result != null) {
            TargetComponent targetComponent = new TargetComponent(TargetComponent.COMPONENT_NAME, result);
            return targetComponent;
        }

        return null;
    }

    private TargetComponent doAnalysisOnComponentTypeValue(Value arg, Unit useUnit, SootMethod sootMethod, SimpleLocalDefs simpleLocalDefs, BriefUnitGraph briefUnitGraph) {


        List<TargetComponent> targetComponentList = new ArrayList<>();
        if (arg instanceof Local) {

            Local local = (Local) arg;

            for (Unit defUnit : simpleLocalDefs.getDefsOfAt(local, useUnit)) {
                TargetComponent targetComponentAll = new TargetComponent();
                DefinitionStmt definitionStmt = (DefinitionStmt) defUnit;

                if (!(definitionStmt.getRightOp() instanceof NewExpr)) {

                    infoLogger.warn("!(definitionStmt.getRightOp() instanceof NewExpr) component " + definitionStmt);
                }

                SimpleLocalUses simpleLocalUses = new SimpleLocalUses(briefUnitGraph, simpleLocalDefs);

                List<UnitValueBoxPair> listUnitUse = simpleLocalUses.getUsesOf(defUnit);


                for (UnitValueBoxPair unitValueBoxPair : listUnitUse) {
                    Unit useComponentLocalUnit = unitValueBoxPair.unit;
                    Stmt useComponentLocalStmt = (Stmt) useComponentLocalUnit;
                    if (!useComponentLocalStmt.containsInvokeExpr()) {
                        continue;
                    }
                    InvokeExpr invokeExpr = useComponentLocalStmt.getInvokeExpr();
                    SootMethod calleeSootMethod = invokeExpr.getMethod();
                    if (invokeExpr instanceof InstanceInvokeExpr) {


                        InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;
                        if (instanceInvokeExpr.getBase() == definitionStmt.getLeftOp() && calleeSootMethod.getDeclaringClass().getName().equals("android.content.ComponentName")) {
                            if (calleeSootMethod.getName().equals("<init>")) {


                                if (instanceInvokeExpr.getArgs().size() == 2) {

                                    if (instanceInvokeExpr.getArg(1).getType().toString().equals("java.lang.Class")) {//ComponentName(Context pkg, Class<?> cls)
                                        TargetComponent targetComponent = doAnalysisOnClassTypeValue(instanceInvokeExpr.getArg(1), useComponentLocalUnit, sootMethod, simpleLocalDefs);
                                        targetComponentAll.updateTargetComponent(targetComponent);
                                    } else {//1. ComponentName(String pkg, String cls)   2. ComponentName(Context pkg, String cls)
                                        TargetComponent targetComponent = computeTwoStringPackageCLassName(sootMethod, simpleLocalDefs, useComponentLocalUnit, instanceInvokeExpr);
                                        targetComponentAll.updateTargetComponent(targetComponent);

                                    }


                                }
                            }
                        }


                    }

                    if (invokeExpr instanceof StaticInvokeExpr && calleeSootMethod.getName().equals("createRelative")) {//	createRelative(String pkg, String cls)
                        //	createRelative(Context pkg, String cls)

                        TargetComponent targetComponent = computeTwoStringPackageCLassName(sootMethod, simpleLocalDefs, useComponentLocalUnit, invokeExpr);
                        targetComponentAll.updateTargetComponent(targetComponent);

                    }


                }

                targetComponentList.add(targetComponentAll);


            }


        }


        TargetComponent targetComponentUl = new TargetComponent();
        for (TargetComponent targetComponent : targetComponentList) {
            targetComponentUl.mixTargetComponent(targetComponent);
        }

        return targetComponentUl;
    }


    private TargetComponent doAnalysisOnStringTypeValue(int valueKind, Value arg, Unit useUnit, SootMethod sootMethod, SimpleLocalDefs simpleLocalDefs) {

        if (arg instanceof StringConstant) {
            StringConstant stringConstant = (StringConstant) arg;
            TargetComponent targetComponent = new TargetComponent(valueKind, stringConstant.value);
            return targetComponent;
        } else if (arg instanceof Local) {
            Local local = (Local) arg;
            TargetComponent targetComponentUl = new TargetComponent();
            for (Unit defUnit : simpleLocalDefs.getDefsOfAt(local, useUnit)) {
                DefinitionStmt definitionStmt = (DefinitionStmt) defUnit;
                Value value = definitionStmt.getRightOp();
                TargetComponent targetComponent = doAnalysisOnStringTypeValue(valueKind, value, defUnit, sootMethod, simpleLocalDefs);
                targetComponentUl.mixTargetComponent(targetComponent);
            }

            return targetComponentUl;


        }

        return null;
    }

    private TargetComponent doAnalysisOnClassTypeValue(Value arg, Unit useUnit, SootMethod sootMethod, SimpleLocalDefs simpleLocalDefs) {

        if (arg instanceof ClassConstant) {

            ClassConstant classConstant = (ClassConstant) arg;
            TargetComponent targetComponent = new TargetComponent(TargetComponent.COMPONENT_NAME, classConstant.getValue());

            return targetComponent;


        } else if (arg instanceof Local) {
            Local local = (Local) arg;
            TargetComponent targetComponentUl = new TargetComponent();
            for (Unit defUnit : simpleLocalDefs.getDefsOfAt(local, useUnit)) {
                DefinitionStmt definitionStmt = (DefinitionStmt) defUnit;
                Value value = definitionStmt.getRightOp();
                TargetComponent targetComponent = doAnalysisOnClassTypeValue(value, defUnit, sootMethod, simpleLocalDefs);
                targetComponentUl.mixTargetComponent(targetComponent);
            }

            return targetComponentUl;


        }

        return null;
    }

    private int doAnalysisIntentIsExplicit(Value value, Unit unit, SootMethod sootMethod) {//只在一个方法里使用


        if (!sootMethod.hasActiveBody()) {
            return -1;
        }

        if (!(value instanceof Local)) {
            throw new RuntimeException("!(value instanceof Local)");
        }


        Local local = (Local) value;
        BriefUnitGraph briefUnitGraph = new BriefUnitGraph(sootMethod.getActiveBody());
        SimpleLocalDefs simpleLocalDefs = new SimpleLocalDefs(briefUnitGraph);
        for (Unit defUnit : simpleLocalDefs.getDefsOfAt(local, unit)) {
            if (!(defUnit instanceof DefinitionStmt)) {
                throw new RuntimeException("!(defUnit instanceof DefinitionStmt)");
            }

            IntentPropagateAnalysis intentPropagateAnalysis = new IntentPropagateAnalysis(sootMethod, briefUnitGraph, defUnit, -1);

            for (Unit useUnit : intentPropagateAnalysis.useIntentUnitSet) {

                Stmt useStmt = (Stmt) useUnit;
                if (!useStmt.containsInvokeExpr()) {
                    continue;
                }
                InvokeExpr invokeExpr = useStmt.getInvokeExpr();
                if (invokeExpr instanceof InstanceInvokeExpr) {

                    SootMethod calleeSootMethod = invokeExpr.getMethod();
                    InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;

                    if (calleeSootMethod.getDeclaringClass().getName().equals("android.content.Intent")) {

                        startIntent.writeStr(useStmt + "\n");
                        if (calleeSootMethod.getName().equals("<init>")) {

                            for (Value arg : instanceInvokeExpr.getArgs()) {
                                if (arg.getType().toString().contains("java.lang.Class")) {
                                    return 1;
                                }


                            }
                            for (Value arg : instanceInvokeExpr.getArgs()) {//看是否存在其的一个intent来自其他intent
                                if (arg.getType().toString().contains("android.content.Intent")) {
                                    //return doAnalysisIntentIsExplicit(arg, useUnit, sootMethod);//死循环了，已经分析的语句应该不再分析
                                    return -1;

                                }


                            }


                        }

                        if (calleeSootMethod.getName().equals("setClass") || calleeSootMethod.getName().equals("setClassName") || calleeSootMethod.getName().equals("setComponent")) {
                            return 1;
                        }

                    }


                }


            }
        }

        return 0;
    }
}
