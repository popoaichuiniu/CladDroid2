package com.popoaichuiniu.experiment;

import com.popoaichuiniu.util.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

public class ComputePlPermissions {


    private   String appDir= "/home/zms/logger_file/DynamicSE/testLog_select_code_2019_4_12";
    private  Logger logger=null;
    public HashSet<String> apps=new HashSet<>();
    public Map<String,Map<String,Set<String>>> permissionAppPLCountMap=new HashMap<>();
    public Map<String,Map<String,Set<String>>> appPermissionPLCountMap=new HashMap<>();


    public ComputePlPermissions(String appDir) {
        this.appDir = appDir;
        logger= new MyLogger(appDir+"/ComputePlPermissions","exceptionLogger").getLogger();

    }

    public ComputePlPermissions() {

        logger= new MyLogger(appDir+"/ComputePlPermissions","exceptionLogger").getLogger();
    }

    public  void computePl() {
        ReadFileOrInputStream readFileOrInputStream=new ReadFileOrInputStream(appDir+"/permissionLeakResults.log",logger);
        List<String> listString=readFileOrInputStream.getAllContentList();


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

                //String unitStr=strArray[4]+"#"+strArray[6];//method+unit
                String unitStr=strArray[6];//unit

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

        WriteFile writeFilePermissionResultsPermission=new WriteFile(appDir+"/"+"permissionLeakConclusion.txt",false,logger);
        for(Map.Entry<String,Map<String,Set<String>>> entryPermissionAppPLCount: permissionAppPLCountMap.entrySet())
        {
            String permission=entryPermissionAppPLCount.getKey();

            writeFilePermissionResultsPermission.writeStr(permission+":\n");

            Set<String> points=new HashSet<>();
            for(Map.Entry<String,Set<String>>  entryAppPLCount: entryPermissionAppPLCount.getValue().entrySet())
            {
                points.addAll(entryAppPLCount.getValue());
            }


            for(String point:points)
            {
                writeFilePermissionResultsPermission.writeStr(point+"\n");
            }

            writeFilePermissionResultsPermission.writeStr("\n\n");
        }

        writeFilePermissionResultsPermission.writeStr("permission leak type count: "+permissionAppPLCountMap.keySet().size()+"\n");
        writeFilePermissionResultsPermission.writeStr("--------------------------------------------------------\n");


        for(Map.Entry<String,Map<String,Set<String>>> entryAppPermissionPLCount: appPermissionPLCountMap.entrySet())
        {
            String app=entryAppPermissionPLCount.getKey();

            writeFilePermissionResultsPermission.writeStr(app+":\n");

            Set<String> points=new HashSet<>();
            for(Map.Entry<String,Set<String>>  entryPermissionPLCount: entryAppPermissionPLCount.getValue().entrySet())
            {
                points.addAll(entryPermissionPLCount.getValue());
            }


            for(String point:points)
            {
                writeFilePermissionResultsPermission.writeStr(point+"\n");
            }

            writeFilePermissionResultsPermission.writeStr("\n\n");
        }

        writeFilePermissionResultsPermission.writeStr("permission leak app count: "+appPermissionPLCountMap.keySet().size()+"\n");

        writeFilePermissionResultsPermission.close();
    }

    public static void main(String[] args) {
        ComputePlPermissions computePlPermissions=new ComputePlPermissions();
        computePlPermissions.computePl();
    }


}
