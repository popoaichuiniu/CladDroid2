package com.popoaichuiniu.experiment;

import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.ExcelWrite;
import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.ReadFileOrInputStream;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

public class ComputePlPermssions {


    private static String appDir= Config.DynamicSE_logDir+"/"+"testLog";
    private static Logger logger=new MyLogger(appDir+"/ComputePlPermssions","exceptionLogger").getLogger();

    public static void main(String[] args) {
        ReadFileOrInputStream readFileOrInputStream=new ReadFileOrInputStream(appDir+"/permissionLeakResults.log",logger);
        List<String> listString=readFileOrInputStream.getAllContentList();

        HashSet<String> apps=new HashSet<>();
        Map<String,Map<String,Set<String>>> permissionAppPLCountMap=new HashMap<>();
        Map<String,Map<String,Set<String>>> appPermissionPLCountMap=new HashMap<>();
        for(String str:listString)
        {
            String [] strArray=str.split("#");
            String permissionStr=strArray[strArray.length-1].substring(1,strArray[strArray.length-1].length());
            String appName=strArray[2];

            apps.add(appName);

            Map<String,Set<String>> permissionPLCountMap= appPermissionPLCountMap.get(appName);
            if(permissionPLCountMap ==null)
            {
                permissionPLCountMap=new HashMap<>();
            }

            String [] permissionArray=permissionStr.split(",");

            for(String permission:permissionArray)
            {
                if(!permission.startsWith("android"))
                {
                    continue;
                }

                String unitStr=strArray[6]+strArray[8];


                //------------------------------
                Set<String> PLUnitStrSet=permissionPLCountMap.get(permission);
                if(PLUnitStrSet==null)
                {
                    PLUnitStrSet=new HashSet<>();

                }
                PLUnitStrSet.add(unitStr);

                permissionPLCountMap.put(permission,PLUnitStrSet);



                //------------------------
                Map<String,Set<String>>  appPLCountMap=permissionAppPLCountMap.get(permission);
                if(appPLCountMap==null)
                {
                    appPLCountMap=new HashMap<>();
                }

                Set<String> unitStrSet=appPLCountMap.get(appName);
                if(unitStrSet==null)
                {
                    unitStrSet=new HashSet<>();
                }
                unitStrSet.add(unitStr);
                appPLCountMap.put(appName,unitStrSet);

                permissionAppPLCountMap.put(permission,appPLCountMap);






            }

            appPermissionPLCountMap.put(appName,permissionPLCountMap);

        }



        ExcelWrite appExcelWritePermissionLeakCount=new ExcelWrite(appDir+"/permissionLeakCount.xlsx");

        appExcelWritePermissionLeakCount.addRow(new Object[]{"permission","appUseCount","allCount"});
        for(Map.Entry<String,Map<String,Set<String>>> entryPermission:permissionAppPLCountMap.entrySet())
        {
            int appUseCount=0;
            int allCount=0;
            for(Map.Entry<String,Set<String>> entryApp :entryPermission.getValue().entrySet())
            {
                appUseCount=appUseCount+1;
                allCount=allCount+entryApp.getValue().size();

            }

            appExcelWritePermissionLeakCount.addRow(new Object[]{entryPermission.getKey(),appUseCount,allCount});


        }

        appExcelWritePermissionLeakCount.closeFile();


        System.out.println("over");

        System.out.println(apps.size());
    }
}
