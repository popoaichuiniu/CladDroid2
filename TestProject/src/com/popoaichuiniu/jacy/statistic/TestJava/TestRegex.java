package com.popoaichuiniu.jacy.statistic.TestJava;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {

    public static void main (String [] args)
    {
        String expr="(declare-datatypes () ((Object Null NotNull)))\n" +
                "(declare-fun containsKey (Object String) Bool)\n" +
                "(declare-fun containsKey (String String) Bool)\n" +
                "(declare-fun containsKey (Int String) Bool)\n" +
                "(declare-fun containsKey (Real String) Bool)\n" +
                "(declare-fun getAction (Object) String)\n" +
                "(declare-fun fromIntent (Object) Object)\n" +
                "(declare-fun fromIntent (String) Object)\n" +
                "(declare-fun fromIntent (Int) Object)\n" +
                "(declare-fun fromIntent (Real) Object)\n" +
                "(declare-fun fromBundle (Object) Object)\n" +
                "(declare-fun fromBundle (String) Object)\n" +
                "(declare-fun fromBundle (Int) Object)\n" +
                "(declare-fun fromBundle (Real) Object)\n" +
                "(declare-datatypes () ((ParamRef (mk-paramref (index Int) (type String) (method String)))))\n" +
                "(declare-fun hasParamRef (Object) ParamRef)\n" +
                "(declare-fun hasParamRef (String) ParamRef)\n" +
                "(declare-fun hasParamRef (Int) ParamRef)\n" +
                "(declare-fun hasParamRef (Real) ParamRef)\n" +
                "(declare-fun isNull (String) Bool)\n" +
                "(declare-fun isNull (Object) Bool)\n" +
                "(declare-fun oEquals (String Object) Bool)\n" +
                "(declare-fun oEquals (Object String) Bool)\n" +
                "(declare-const cats (Array Int String))\n" +
                "(declare-const keys (Array Int String))\n" +
                "(declare-const $r3_java.lang.String_test7_com.example.lab418.testwebview2.TestIFConGenActivity_8 String )\n" +
                "(declare-const $r1_android.content.Intent_test7_com.example.lab418.testwebview2.TestIFConGenActivity Object )\n" +
                "(declare-const pr0_android.content.Intent_test7_com.example.lab418.testwebview2.TestIFConGenActivity ParamRef)\n" +
                "(declare-const $r2_android.os.Bundle_test7_com.example.lab418.testwebview2.TestIFConGenActivity_2 Object )\n" +
                "(assert (= $r3_java.lang.String_test7_com.example.lab418.testwebview2.TestIFConGenActivity_8 \"sss\"))\n" +
                "(assert (= (containsKey $r2_android.os.Bundle_test7_com.example.lab418.testwebview2.TestIFConGenActivity_2 \"xxx\") true))\n" +
                "(assert (= (fromBundle $r3_java.lang.String_test7_com.example.lab418.testwebview2.TestIFConGenActivity_8) $r2_android.os.Bundle_test7_com.example.lab418.testwebview2.TestIFConGenActivity_2))\n" +
                "(assert (= (fromIntent $r2_android.os.Bundle_test7_com.example.lab418.testwebview2.TestIFConGenActivity_2) $r1_android.content.Intent_test7_com.example.lab418.testwebview2.TestIFConGenActivity))\n" +
                "(assert ( = (index pr0_android.content.Intent_test7_com.example.lab418.testwebview2.TestIFConGenActivity) 0))\n" +
                "(assert ( = (type pr0_android.content.Intent_test7_com.example.lab418.testwebview2.TestIFConGenActivity) \"android.content.Intent\"))\n" +
                "(assert ( = (method pr0_android.content.Intent_test7_com.example.lab418.testwebview2.TestIFConGenActivity) \"com.example.lab418.testwebview2.TestIFConGenActivity.test7\"))\n" +
                "(assert (= (hasParamRef $r1_android.content.Intent_test7_com.example.lab418.testwebview2.TestIFConGenActivity) pr0_android.content.Intent_test7_com.example.lab418.testwebview2.TestIFConGenActivity))\n" +
                "(assert (= (containsKey $r1_android.content.Intent_test7_com.example.lab418.testwebview2.TestIFConGenActivity \"ttt\") true))\n" +
                "(check-sat-using (then qe smt))\n" +
                "(get-model)";
        Pattern p = Pattern.compile("\\(assert\\s+\\(=\\s+\\(fromIntent\\s+(\\S+)\\)\\s+(\\S+)\\)\\)");
        Matcher m = p.matcher(expr);
        while (m.find())
        {

            String all=m.group(0);
            System.out.println(all);
            String dataSymbol=m.group(1);

            System.out.println(dataSymbol);
//
            String  intentSymbol=m.group(2);

            System.out.println(intentSymbol);

        }


    }
}
