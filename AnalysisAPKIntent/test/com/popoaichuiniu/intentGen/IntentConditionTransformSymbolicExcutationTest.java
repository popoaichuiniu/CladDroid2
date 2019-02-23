package com.popoaichuiniu.intentGen;

import org.javatuples.Quartet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class IntentConditionTransformSymbolicExcutationTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void doAnalysisOnUnit() {
    }

    @Test
    public void runSolvingPhase() {
    }

    @Test
    public void testJoinTwoIntent() {
        Intent intent1 = new Intent();
        intent1.myExtras.add(new IntentExtraKey("ttt", "float", "5.0"));
        //intent1.action = "aa";
      // intent1.categories.add("tttt");
      //  intent1.categories.add("zzzz");
        Intent intent2 = new Intent();
        //intent2.action = "ZMS!aa";
     //  intent2.categories.add("ZMS!ttttq");
        intent2.myExtras.add(new IntentExtraKey("ttt", "int", "9.0"));
        Intent intent = IntentConditionTransformSymbolicExcutation.joinTwoIntent(intent1, intent2);
        System.out.println(intent);
    }

    @Test
    public void testAddAssert() {
        Set<String> dels = new HashSet<>();


        dels.add("(declare-const $r13_java.lang.String_a_com.taobao.accs.internal.ServiceImpl_196 String )");
        //dels.add("(declare-const $r13_java.lang.String_a_com.taobao.accs.internal.ServiceImpl_199 String )");
        dels.add("(declare-const $r6_java.lang.String_a_com.taobao.accs.internal.ServiceImpl_196 String )");
        dels.add("(declare-const $r6_java.lang.String_a_com.taobao.accs.internal.ServiceImpl_198 String )");
        dels.add("(declare-const $r6_java.lang.String_a_com.taobao.accs.internal.ServiceImpl_200 String )");
        Set<String> pathConds = new HashSet<>();

        pathConds.add("(assert (= $r13_java.lang.String_a_com.taobao.accs.internal.ServiceImpl_196 $r6_java.lang.String_a_com.taobao.accs.internal.ServiceImpl_196))");
        pathConds.add("(assert (= $r13_java.lang.String_a_com.taobao.accs.internal.ServiceImpl_196 \"org.agoo.android.intent.action.PING_V4\" ))");
        pathConds.add("(assert (= $r6_java.lang.String_a_com.taobao.accs.internal.ServiceImpl_198 (str.++ \"ZMS!\" \"android.action.zms\")))");
        pathConds.add("(assert (= $r6_java.lang.String_a_com.taobao.accs.internal.ServiceImpl_198 (str.++ \"ZMS!\" $r13_java.lang.String_a_com.taobao.accs.internal.ServiceImpl_199)))");
        //pathConds.add()
        System.out.println(IntentConditionTransformSymbolicExcutation.addAssertToBlockRandomStringValue(dels, pathConds));
    }

    @Test
    public void testModifyValue() {
        String str = null;
        System.out.println(IntentConditionTransformSymbolicExcutation.modifyValue(str));
    }


    @Test
    public void testJoinIntentSet() {
        Set<Intent> intentSet = new HashSet<>();
//        Intent intent1=new Intent();
//        intent1.action="xxx";
//        intent1.extras.add(new Quartet<>("IntentKey","float","4","7.5"));
//        intent1.extras.add(new Quartet<>("IntentKey","Int","4","10"));
//        intentSet.add(intent1);


        Intent intent2 = new Intent();
//       intent2.action = "yyy";
        intent2.myExtras.add(new IntentExtraKey("ttt", "java.lang.String", "xxx"));
       // intent2.categories.add("cate");

        intentSet.add(intent2);

        Intent intent4 = new Intent();
//        intent4.action = "yyy";
      intent4.myExtras.add(new IntentExtraKey("ttt", "java.lang.String", "ttt"));
       // intent4.categories.add("wwww");
        intentSet.add(intent4);


        Intent intent5 = new Intent();
//       intent5.action = "zzz";
      // intent5.myExtras.add(new IntentExtraKey("ttt", "float", "6.0"));
       // intent5.categories.add("ZMS!wwww");
        intentSet.add(intent5);





    }

    @Test
    public void testIntent()//ok
    {
        Set<Intent> intentSet = new HashSet<>();
        Intent intent = new Intent();
        //intent.action="";
        // intent.myExtras.add(new IntentExtraKey("id","int","4"));
        //  intent.myExtras.add(new IntentExtraKey("id","int","5"));
        intent.categories.add("cc");
        intent.categories.add("dd");
        intentSet.add(intent);

        Intent intent2 = new Intent();
        // intent2.myExtras.add(new IntentExtraKey("id","int","4"));
//
        intent2.categories.add("dd");
        intent2.categories.add("cc");
        intentSet.add(intent2);


        for (Intent oneIntent : intentSet) {
            System.out.println(oneIntent);
        }


    }


    @Test
    public  void testStringValue()
    {

        String ttt=IntentConditionTransformSymbolicExcutation.getStringValueOfIntent("ZMS!00##ZMS!!0##ZMS!!11##ZMS!44");

        System.out.println(ttt);

    }


    @Test
    public  void testPreProcess()
    {
        Set<IntentConditionTransformSymbolicExcutation.IntentUnit > oldSet=new HashSet<>();

        IntentConditionTransformSymbolicExcutation.IntentUnit intentUnit=new IntentConditionTransformSymbolicExcutation.IntentUnit();

        IntentConditionTransformSymbolicExcutation.IntentUnit intentUnit2=new IntentConditionTransformSymbolicExcutation.IntentUnit();
        intentUnit.intent=new Intent();
        intentUnit.intent.action="ZMS!55";
        intentUnit.intent.myExtras.add(new IntentExtraKey("xxx","int","5##8##9"));
        intentUnit.intent.myExtras.add(new IntentExtraKey("xxx","java.lang.String","ZMS!tt##ZMS!aa##bb"));

        intentUnit2.intent=new Intent();
        intentUnit2.intent.myExtras.add(new IntentExtraKey("yyy","int","5##8##9"));
        intentUnit2.intent.myExtras.add(new IntentExtraKey("yyy","java.lang.String","ZMS!tt##ZMS!aa##bb"));
        oldSet.add(intentUnit);
        oldSet.add(intentUnit2);
        Set<IntentConditionTransformSymbolicExcutation.IntentUnit > newSet=IntentConditionTransformSymbolicExcutation.preProcess(oldSet);

        for(IntentConditionTransformSymbolicExcutation.IntentUnit oneIntentUnit:newSet)
        {
            System.out.println(oneIntentUnit);
        }

    }


}