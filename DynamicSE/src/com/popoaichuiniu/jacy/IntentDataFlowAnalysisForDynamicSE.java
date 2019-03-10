package com.popoaichuiniu.jacy;

import com.popoaichuiniu.intentGen.MyArraySparseSet;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.*;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.*;
import java.util.regex.Pattern;

public class IntentDataFlowAnalysisForDynamicSE extends ForwardFlowAnalysis<Unit, FlowSet<Value>> {//终极ok
    public static int ID = 0;
    private IntentDataTransfer initialIntentDataTransfer = null;

    public IntentDataFlowAnalysisForDynamicSE(DirectedGraph<Unit> graph, IntentDataTransfer intentDataTransfer) {

        super(graph);
        this.initialIntentDataTransfer = intentDataTransfer;
        doAnalysis();


    }


    private Map<Value, IntentDataTransfer> intentDataTransferMap = new HashMap<>();

    public Map<Unit,IntentDataTransfer> instrumentUnitIntentDataFlowIn=new HashMap<>();


//终止是不再变化.

    @Override
    protected void flowThrough(FlowSet<Value> in, Unit d, FlowSet<Value> out) {//1.从方法中传入的intent属性数据多不多统计一下
        //2.intent属性数据和intent从方法的返回值多不多
        //3.intent属性数据从类属性中取。（未考虑，考虑了intent来自field）


        in.copy(out);

        System.out.println("%%%%%%%" + d);


        if (d instanceof IfStmt) {
            IfStmt ifStmt = (IfStmt) d;
            List<ValueBox> valueBoxList= ifStmt.getUseBoxes();
        }


        if (d instanceof DefinitionStmt) {
            DefinitionStmt definitionStmt = (DefinitionStmt) d;

            if (definitionStmt.getRightOp() instanceof ParameterRef) {//-----------------加入传入的参数---？？？？
                if (definitionStmt.getLeftOp().toString().contains(String.valueOf(initialIntentDataTransfer.targetParameter))) {


                    out.add(definitionStmt.getLeftOp());

                    intentDataTransferMap.put(definitionStmt.getLeftOp(), initialIntentDataTransfer);


                }
            }


            if (definitionStmt.getRightOp().getType().toString().equals("android.content.Intent")) {
                if (definitionStmt.getRightOp() instanceof FieldRef) {//Intent 来自域 保守的认为这个是外部Intent

                    out.add(definitionStmt.getLeftOp());
                    out.add(definitionStmt.getRightOp());


                    IntentDataTransfer intentDataTransfer = new IntentDataTransfer();
                    intentDataTransfer.type = IntentDataTransfer.TYPE_INTENT;
                    intentDataTransfer.value = String.valueOf(ID);
                    ID++;
                    intentDataTransferMap.put(definitionStmt.getLeftOp(), intentDataTransfer);


                    IntentDataTransfer intentDataTransfer2 = new IntentDataTransfer();
                    intentDataTransfer2.type = IntentDataTransfer.TYPE_INTENT;
                    intentDataTransfer2.value = String.valueOf(ID);
                    ID++;
                    intentDataTransferMap.put(definitionStmt.getRightOp(), intentDataTransfer2);


                } else if (definitionStmt.getRightOp() instanceof JVirtualInvokeExpr) {
                    JVirtualInvokeExpr jVirtualInvokeExpr = (JVirtualInvokeExpr) definitionStmt.getRightOp();
                    if (jVirtualInvokeExpr.getMethod().getName().equals("getIntent")) {//intent从getIntent方法中来

                        IntentDataTransfer intentDataTransfer = new IntentDataTransfer();
                        intentDataTransfer.type = IntentDataTransfer.TYPE_INTENT;
                        intentDataTransfer.value = String.valueOf(ID);
                        ID++;
                        intentDataTransferMap.put(definitionStmt.getLeftOp(), intentDataTransfer);

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
                                intentDataTransfer.type = IntentDataTransfer.TYPE_EXTRA;
                                intentDataTransfer.value = String.valueOf(ID);
                                ID++;
                                intentDataTransferMap.put(definitionStmt.getLeftOp(), intentDataTransfer);
                                out.add(definitionStmt.getLeftOp());
                            }

                            if (invokeExpr.getMethod().getName().equals("getAction")) {

                                IntentDataTransfer intentDataTransfer = new IntentDataTransfer();
                                intentDataTransfer.type = IntentDataTransfer.TYPE_ACTION;
                                intentDataTransfer.value = String.valueOf(ID);
                                ID++;
                                intentDataTransferMap.put(definitionStmt.getLeftOp(), intentDataTransfer);
                                out.add(definitionStmt.getLeftOp());
                            }

                            if (invokeExpr.getMethod().getName().equals("hasCategory")) {
                                IntentDataTransfer intentDataTransfer = new IntentDataTransfer();
                                intentDataTransfer.type = IntentDataTransfer.TYPE_CATEGORY;
                                intentDataTransfer.value = String.valueOf(ID);
                                ID++;
                                intentDataTransferMap.put(definitionStmt.getLeftOp(), intentDataTransfer);
                                out.add(definitionStmt.getLeftOp());
                            }

                        }


                    }

                }
            }

            if (in.contains(definitionStmt.getRightOp()))//赋值
            {

                intentDataTransferMap.put(definitionStmt.getLeftOp(), intentDataTransferMap.get(definitionStmt.getRightOp()));

                out.add(definitionStmt.getLeftOp());
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



