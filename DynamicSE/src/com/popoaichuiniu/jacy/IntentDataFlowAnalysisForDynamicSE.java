package com.popoaichuiniu.jacy;

import com.popoaichuiniu.intentGen.MyArraySparseSet;
import com.popoaichuiniu.util.Util;
import org.apache.log4j.Logger;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.*;
import java.util.regex.Pattern;

class MethodSummary {
    SootMethod sootMethod;
    int paraIndex;

    boolean isParaFlowToReturn;
    IntentDataTransfer returnIntentDataTransfer = null;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodSummary that = (MethodSummary) o;
        return paraIndex == that.paraIndex &&
                Objects.equals(sootMethod, that.sootMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sootMethod, paraIndex);
    }

    public MethodSummary(SootMethod sootMethod, int paraIndex) {
        this.sootMethod = sootMethod;
        this.paraIndex = paraIndex;
    }
}

class MethodUnit {
    SootMethod sootMethod;
    Unit unit;

    public MethodUnit(SootMethod sootMethod, Unit unit) {
        this.sootMethod = sootMethod;
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodUnit that = (MethodUnit) o;
        return Objects.equals(sootMethod, that.sootMethod) &&
                Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sootMethod, unit);
    }

    @Override
    public String toString() {
        return "MethodUnit{" +
                "sootMethod=" + sootMethod +
                ", unit=" + unit +
                '}';
    }
}

public class IntentDataFlowAnalysisForDynamicSE extends ForwardFlowAnalysis<Unit, FlowSet<Value>> {//终极ok
    public static int ID = 0;
    public static Set<MethodSummary> methodSummarySet = new HashSet<>();
    public static Map<MethodUnit, IntentDataTransfer> allInstrumentUnitIntentDataFlowIn = new HashMap<>();


    public static void clearIntentDataFlowAnalysisForDynamicSE() {
        ID = 0;
        methodSummarySet.clear();
        allInstrumentUnitIntentDataFlowIn.clear();
    }

    private IntentDataTransfer initialIntentDataTransfer = null;
    private Logger logger = null;
    private SootMethod sootMethod = null;

    public Map<String, IntentDataTransfer> intentDataTransferMap = new HashMap<>();//传输的Intent相关数据集合
    public Map<MethodUnit, IntentDataTransfer> instrumentUnitIntentDataFlowIn = new HashMap<>();//需要插桩的语句 包括if和extra
    //public Set<IntentDataTransfer> calleeMethodSetWithIntentData = new HashSet<>();
    public boolean isParaFlowToReturn = false;

    public IntentDataTransfer returnIntentDataTransfer = null;

    public IntentDataFlowAnalysisForDynamicSE(DirectedGraph<Unit> graph, IntentDataTransfer intentDataTransfer, Logger logger) {

        super(graph);
        this.initialIntentDataTransfer = intentDataTransfer;
        this.logger = logger;
        this.sootMethod = initialIntentDataTransfer.targetSootMethod;

        doAnalysis();


    }


    @Override
    protected void flowThrough(FlowSet<Value> in, Unit d, FlowSet<Value> out) {


        in.copy(out);

        System.out.println("%%%%%%%" + d);

        Stmt stmt = (Stmt) d;

        if (stmt.containsInvokeExpr() && (!stmt.getInvokeExpr().getMethod().getDeclaringClass().getType().toString().equals("android.content.Intent")))//传播到一个方法里
        {

            if (Util.isApplicationMethod(stmt.getInvokeExpr().getMethod()) && stmt.getInvokeExpr().getMethod().hasActiveBody()) {

                IntentDataTransfer intentDataTransfer = new IntentDataTransfer();// no para about intent data
                intentDataTransfer.targetSootMethod = stmt.getInvokeExpr().getMethod();

                List<Value> args = stmt.getInvokeExpr().getArgs();
                int count = 0;
                for (int index = 0; index < args.size(); index++) {
                    if (in.contains(args.get(index))) {//para about intent data
                        intentDataTransfer = new IntentDataTransfer(intentDataTransferMap.get(args.get(index).toString()));//must new instance
                        intentDataTransfer.targetSootMethod = stmt.getInvokeExpr().getMethod();
                        intentDataTransfer.targetParameter = index;

                        count++;
                    }


                }

                if (count > 1) {
                    logger.error("method para has more than one intent data");
                }

                MethodSummary methodSummary = new MethodSummary(intentDataTransfer.targetSootMethod, intentDataTransfer.targetParameter);
                if (!methodSummarySet.contains(methodSummary)) {

                    methodSummarySet.add(methodSummary);
                    BriefUnitGraph briefUnitGraph = new BriefUnitGraph(intentDataTransfer.targetSootMethod.getActiveBody());
                    IntentDataFlowAnalysisForDynamicSE intentDataFlowAnalysisForDynamicSE = new IntentDataFlowAnalysisForDynamicSE(briefUnitGraph, intentDataTransfer, logger);


                    methodSummary.isParaFlowToReturn = intentDataFlowAnalysisForDynamicSE.isParaFlowToReturn;
                    methodSummary.returnIntentDataTransfer = intentDataFlowAnalysisForDynamicSE.returnIntentDataTransfer;

                    allInstrumentUnitIntentDataFlowIn.putAll(intentDataFlowAnalysisForDynamicSE.instrumentUnitIntentDataFlowIn);


                }

                if (stmt instanceof DefinitionStmt) {

                    DefinitionStmt definitionStmt = (DefinitionStmt) stmt;

                    if (methodSummary.isParaFlowToReturn) {
                        out.add(definitionStmt.getLeftOp());

                        intentDataTransferMap.put(definitionStmt.getLeftOp().toString(), methodSummary.returnIntentDataTransfer);
                    }

                }
            }


        }

        if (stmt instanceof ReturnStmt)// return id
        {
            ReturnStmt returnStmt = (ReturnStmt) stmt;
            if (in.contains(returnStmt.getOp())) {

                isParaFlowToReturn = true;
                returnIntentDataTransfer = intentDataTransferMap.get(returnStmt.getOp().toString());
                //logger.error("return intent data  doesn't handle!" + sootMethod);
            }
        }


        if (d instanceof IfStmt) {//传播到if语句里
            IfStmt ifStmt = (IfStmt) d;
            ConditionExpr condition = (ConditionExpr) ifStmt.getCondition();
            Value conditionLeft = condition.getOp1();
            Value conditionRight = condition.getOp2();
            boolean flag1 = false;
            boolean flag2 = false;
            if (in.contains(conditionLeft)) {
                IntentDataTransfer intentDataTransfer = new IntentDataTransfer(intentDataTransferMap.get(conditionLeft.toString()));

                instrumentUnitIntentDataFlowIn.put(new MethodUnit(sootMethod, ifStmt), intentDataTransfer);
                flag1 = true;
            }

            if (in.contains(conditionRight)) {
                IntentDataTransfer intentDataTransfer = new IntentDataTransfer(intentDataTransferMap.get(conditionRight.toString()));

                flag2 = true;
            }

            if (flag1 && flag2) {
                logger.error("if conditionLeft and conditionRight are have intent data!");
            }
        }


        if (d instanceof DefinitionStmt) {//intent data 数据传播
            DefinitionStmt definitionStmt = (DefinitionStmt) d;

            if (initialIntentDataTransfer.targetParameter != -2) {
                if (definitionStmt.getRightOp() instanceof ParameterRef) {
                    if (definitionStmt.getRightOp().toString().contains(String.valueOf("@parameter" + initialIntentDataTransfer.targetParameter))) {


                        out.add(definitionStmt.getLeftOp());
                        intentDataTransferMap.put(definitionStmt.getLeftOp().toString(), initialIntentDataTransfer);


                    }
                }
            }


            if (definitionStmt.getRightOp().getType().toString().equals("android.content.Intent")) {
                if (definitionStmt.getRightOp() instanceof FieldRef) {//Intent 来自域 保守的认为这个是外部Intent

                    IntentDataTransfer intentDataTransfer = new IntentDataTransfer();
                    intentDataTransfer.type = IntentDataTransfer.TYPE_INTENT;
                    intentDataTransfer.id = String.valueOf(ID);
                    intentDataTransfer.whereGenUnit = definitionStmt;
                    intentDataTransfer.whereGenSootMethod = sootMethod;
                    ID++;
                    intentDataTransferMap.put(definitionStmt.getLeftOp().toString(), intentDataTransfer);
                    out.add(definitionStmt.getLeftOp());


                    IntentDataTransfer intentDataTransfer2 = new IntentDataTransfer();
                    intentDataTransfer2.type = IntentDataTransfer.TYPE_INTENT;
                    intentDataTransfer2.id = String.valueOf(ID);
                    intentDataTransfer2.whereGenUnit = definitionStmt;//这有点不对，但是无所谓，忽略
                    intentDataTransfer2.whereGenSootMethod = sootMethod;//这有点不对，但是无所谓，忽略
                    ID++;
                    intentDataTransferMap.put(definitionStmt.getRightOp().toString(), intentDataTransfer2);
                    out.add(definitionStmt.getRightOp());


                } else if (definitionStmt.getRightOp() instanceof JVirtualInvokeExpr) {
                    JVirtualInvokeExpr jVirtualInvokeExpr = (JVirtualInvokeExpr) definitionStmt.getRightOp();
                    if (jVirtualInvokeExpr.getMethod().getName().equals("getIntent")) {//intent从getIntent方法中来

                        IntentDataTransfer intentDataTransfer = new IntentDataTransfer();
                        intentDataTransfer.type = IntentDataTransfer.TYPE_INTENT;
                        intentDataTransfer.id = String.valueOf(ID);
                        intentDataTransfer.whereGenUnit = definitionStmt;
                        intentDataTransfer.whereGenSootMethod = sootMethod;
                        ID++;
                        intentDataTransferMap.put(definitionStmt.getLeftOp().toString(), intentDataTransfer);
                        out.add(definitionStmt.getLeftOp());


                    }

                }

            } else if (definitionStmt.containsInvokeExpr()) {// intent attribute加入
                if (definitionStmt.getInvokeExpr() instanceof JVirtualInvokeExpr) {
                    JVirtualInvokeExpr invokeExpr = (JVirtualInvokeExpr) definitionStmt.getInvokeExpr();
                    if (invokeExpr.getBase().getType().toString().equals("android.content.Intent")) {


                        if (in.contains(invokeExpr.getBase()))//intent from in
                        {

                            if (Pattern.matches("get.*Extra", invokeExpr.getMethod().getName())) {
                                IntentDataTransfer intentDataTransfer = new IntentDataTransfer();
                                if (invokeExpr.getMethod().getName().equals("getBooleanExtra")) {
                                    intentDataTransfer.type = IntentDataTransfer.TYPE_RETURN_BOOLEAN_EXTRA;
                                } else if (invokeExpr.getMethod().getName().equals("getByteExtra")) {
                                    intentDataTransfer.type = IntentDataTransfer.TYPE_RETURN_BYTE_EXTRA;
                                } else if (invokeExpr.getMethod().getName().equals("getShortExtra")) {
                                    intentDataTransfer.type = IntentDataTransfer.TYPE_RETURN_SHORT_EXTRA;
                                } else if (invokeExpr.getMethod().getName().equals("getIntExtra")) {
                                    intentDataTransfer.type = IntentDataTransfer.TYPE_RETURN_INT_EXTRA;
                                } else if (invokeExpr.getMethod().getName().equals("getFloatExtra")) {
                                    intentDataTransfer.type = IntentDataTransfer.TYPE_RETURN_FLOAT_EXTRA;
                                } else if (invokeExpr.getMethod().getName().equals("getLongExtra")) {
                                    intentDataTransfer.type = IntentDataTransfer.TYPE_RETURN_LONG_EXTRA;
                                } else if (invokeExpr.getMethod().getName().equals("getDoubleExtra")) {
                                    intentDataTransfer.type = IntentDataTransfer.TYPE_RETURN_DOUBLE_EXTRA;
                                } else if (invokeExpr.getMethod().getName().equals("getStringExtra")) {
                                    intentDataTransfer.type = IntentDataTransfer.TYPE_RETURN_STRING_EXTRA;
                                } else {
                                    intentDataTransfer.type = IntentDataTransfer.TYPE_EXTRA;
                                }

                                intentDataTransfer.id = String.valueOf(ID);
                                intentDataTransfer.whereGenUnit = definitionStmt;
                                intentDataTransfer.whereGenSootMethod = sootMethod;
                                ID++;
                                intentDataTransferMap.put(definitionStmt.getLeftOp().toString(), intentDataTransfer);
                                out.add(definitionStmt.getLeftOp());


                            }

                            if (invokeExpr.getMethod().getName().equals("getAction")) {

                                IntentDataTransfer intentDataTransfer = new IntentDataTransfer();
                                intentDataTransfer.type = IntentDataTransfer.TYPE_ACTION;
                                intentDataTransfer.id = "action";
                                intentDataTransfer.whereGenUnit = definitionStmt;
                                intentDataTransfer.whereGenSootMethod = sootMethod;
                                intentDataTransferMap.put(definitionStmt.getLeftOp().toString(), intentDataTransfer);
                                out.add(definitionStmt.getLeftOp());
                            }

                            if (invokeExpr.getMethod().getName().equals("hasCategory")) {
                                IntentDataTransfer intentDataTransfer = new IntentDataTransfer();
                                intentDataTransfer.type = IntentDataTransfer.TYPE_CATEGORY;
                                intentDataTransfer.id = "category";
                                intentDataTransfer.whereGenUnit = definitionStmt;
                                intentDataTransfer.whereGenSootMethod = sootMethod;
                                intentDataTransferMap.put(definitionStmt.getLeftOp().toString(), intentDataTransfer);
                                out.add(definitionStmt.getLeftOp());
                            }

                        }


                    }

                }
            }


            if (in.contains(definitionStmt.getRightOp()))//assign
            {
                out.add(definitionStmt.getLeftOp());
                intentDataTransferMap.put(definitionStmt.getLeftOp().toString(), intentDataTransferMap.get(definitionStmt.getRightOp().toString()));
            }

            //number
            if (isNumberOperation(definitionStmt)) {

                int count = 0;
                for (ValueBox valueBox : definitionStmt.getRightOp().getUseBoxes()) {
                    if (in.contains(valueBox.getValue())) {
                        out.add(definitionStmt.getLeftOp());
                        intentDataTransferMap.put(definitionStmt.getLeftOp().toString(), intentDataTransferMap.get(valueBox.getValue().toString()));
                        count++;
                    }
                }

                if (count > 1) {
                    logger.error("number operate intent data >1");
                }


            }


            // String
            if (isStringOperation(definitionStmt)) {

                if (definitionStmt.containsInvokeExpr()) {
                    int count = 0;
                    for (ValueBox valueBox : definitionStmt.getRightOp().getUseBoxes()) {
                        if (in.contains(valueBox.getValue())) {
                            out.add(definitionStmt.getLeftOp());
                            intentDataTransferMap.put(definitionStmt.getLeftOp().toString(), intentDataTransferMap.get(valueBox.getValue().toString()));
                            count++;
                        }
                    }

                    if (count > 1) {
                        logger.error("String operate intent data >1");
                    }
                }

            }


            //kill

            if (in.contains(definitionStmt.getLeftOp())) {//in中的值被重新赋值，所以可能他就不是intent相关的呢


                for (ValueBox valueBox : definitionStmt.getUseBoxes()) {
                    Value value = valueBox.getValue();
                    if (in.contains(value)) {//使用了in中的值
                        return;
                    }
                }


                if (definitionStmt.getRightOp().getType().toString().equals("android.content.Intent")) {
                    if (definitionStmt.getRightOp() instanceof FieldRef) {//Intent 来自域 保守的认为这个是外部Intent

                        return;


                    } else if (definitionStmt.getRightOp() instanceof JVirtualInvokeExpr) {
                        JVirtualInvokeExpr jVirtualInvokeExpr = (JVirtualInvokeExpr) definitionStmt.getRightOp();
                        if (jVirtualInvokeExpr.getMethod().getName().equals("getIntent")) {//intent从getIntent方法中来

                            return;


                        }

                    }
                }


                //this unit is not about intent,so kill
                intentDataTransferMap.remove(definitionStmt.getLeftOp());
                out.remove(definitionStmt.getLeftOp());


            }
        }
    }

    private boolean isStringOperation(DefinitionStmt definitionStmt) {

        if (definitionStmt.containsInvokeExpr()) {
            InvokeExpr invokeExpr = definitionStmt.getInvokeExpr();
            if (invokeExpr.getMethod().getDeclaringClass().getType().toString().equals("java.lang.StringBuilder")) {

                return true;

            }

            if (invokeExpr.getMethod().getDeclaringClass().getType().toString().equals("java.lang.StringBuffer")) {

                return true;

            }
            if (invokeExpr.getMethod().getDeclaringClass().getType().toString().equals("java.lang.String")) {

                return true;

            }
        }

        return false;

    }

    private boolean isNumberOperation(DefinitionStmt definitionStmt) {

        if (definitionStmt.containsInvokeExpr()) {
            InvokeExpr invokeExpr = definitionStmt.getInvokeExpr();
            if (invokeExpr.getMethod().getDeclaringClass().hasSuperclass()) {
                if (invokeExpr.getMethod().getDeclaringClass().getSuperclass().getType().toString().equals("java.lang.Number")) {
                    return true;
                }
            }


        }

        if (definitionStmt.getRightOp() instanceof AddExpr) {
            return true;
        }

        if (definitionStmt.getRightOp() instanceof SubExpr) {
            return true;
        }
        if (definitionStmt.getRightOp() instanceof MulExpr) {
            return true;
        }

        if (definitionStmt.getRightOp() instanceof DivExpr) {
            return true;
        }

        return false;


    }


    @Override
    protected void merge(FlowSet<Value> in1, FlowSet<Value> in2, FlowSet<Value> out) {

        in1.union(in2, out);

    }

    @Override
    protected void copy(FlowSet<Value> source, FlowSet<Value> dest) {

        source.copy(dest);

    }

    @Override
    protected FlowSet<Value> newInitialFlow() {
        return new MyArraySparseSet<>();
    }

    @Override
    protected FlowSet<Value> entryInitialFlow() {

        MyArraySparseSet myArraySparseSet = new MyArraySparseSet<>();


        return myArraySparseSet;
    }
}



