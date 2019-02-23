package com.popoaichuiniu.experiment;

import com.popoaichuiniu.util.ExcelWrite;
import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.ReadFileOrInputStream;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

public class ComputePlPermssions {


    private static String appDir="/home/zms/logger_file/testlog/old_results/secondAnalysis/02APP_test_to_kill_other_nexta";
    private static Logger logger=new MyLogger("AnalysisAPKIntent/ComputePlPermssions","exceptionLogger").getLogger();

    public static void main(String[] args) {
        ReadFileOrInputStream readFileOrInputStream=new ReadFileOrInputStream(appDir+"/ZMSInstrument.log",logger);
        List<String> listString=readFileOrInputStream.getAllContentList();
        Map<String,Set<String>> permissionCount=new HashMap<>();
        Map<String,Set<String>> permissionAPPCount=new HashMap<>();
        HashSet<String> apps=new HashSet<>();
        for(String str:listString)
        {
            String [] strArray=str.split("#");
            String permissionStr=strArray[strArray.length-1].substring(1,strArray[strArray.length-1].length());
            String appName=strArray[2];

            apps.add(appName);

           // String appType= FindAPPCategory.findAPPType(new File(appName).getName());

//            if(appType.equals("zms"))//adb log has problem
//            {
//                System.out.println(appName);
//
//
//            }

            String [] permissionArray=permissionStr.split(",");

            for(String permission:permissionArray)
            {
                if(!permission.startsWith("android"))
                {
                    continue;
                }

                //
                Set<String> unitSet=permissionCount.get(permission);
                if(unitSet==null)
                {
                    unitSet=new HashSet<>();


                }
                String unitStr=strArray[6]+strArray[8];
                unitSet.add(unitStr);
                permissionCount.put(permission,unitSet);


                //

                Set<String> appSet=permissionAPPCount.get(permission);
                if(appSet==null)
                {
                    appSet=new HashSet<>();

                }

                appSet.add(appName);


                permissionAPPCount.put(permission,appSet);










            }

        }


        ExcelWrite excelWritePermissionCount=new ExcelWrite(appDir+"/permissionAllCount.xlsx");

        excelWritePermissionCount.addRow(new Object[]{"permission","allCount"});
        for(Map.Entry<String,Set<String>> entry:permissionCount.entrySet())
        {
            excelWritePermissionCount.addRow(new Object[]{entry.getKey(),entry.getValue().size()});
        }

        excelWritePermissionCount.closeFile();




        ExcelWrite appExcelWritePermissionCount=new ExcelWrite(appDir+"/permissionAPPUseCount.xlsx");

        appExcelWritePermissionCount.addRow(new Object[]{"permission","appUseCount"});
        for(Map.Entry<String,Set<String>> entry:permissionAPPCount.entrySet())
        {
            appExcelWritePermissionCount.addRow(new Object[]{entry.getKey(),entry.getValue().size()});
        }

        appExcelWritePermissionCount.closeFile();






        System.out.println("over");

        System.out.println(apps.size());
    }
}
