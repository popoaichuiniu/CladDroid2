package com.popoaichuiniu.experiment;

import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.ReadFileOrInputStream;
import com.popoaichuiniu.util.WriteFile;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FalsePositiveRateOfStatic {

    private static Logger logger = new MyLogger(Config.unitNeedAnalysisGenerate + "/FalsePositiveRateOfStatic", "exceptionLogger").getLogger();

    public static void main(String[] args) {
        EAExportUnitUsePermission.main(null);
        Map<String, Map<String, Set<String>>> export = EAExportUnitUsePermission.export;
        ComputePlPermissions computePlPermissions = new ComputePlPermissions("/home/zms/logger_file/testLog_2017_4_9_se");
        computePlPermissions.computePl();
        Map<String, Map<String, Set<String>>> appPermissionPLCountMap = computePlPermissions.appPermissionPLCountMap;

        ReadFileOrInputStream readFileOrInputStreamDangerous = new ReadFileOrInputStream("/home/zms/logger_file/testLog_2017_4_9_se/successTest_apk_list", logger);
        Set<String> testSuccessAppContent = readFileOrInputStreamDangerous.getAllContentLinSet();
        Set<String> testSuccessApp = new HashSet<>();
        for (String app : testSuccessAppContent) {
            String app_origin = app.replaceAll("/instrumented", "").replaceAll("_signed_zipalign", "");
            testSuccessApp.add(app_origin);
        }

        double allAppFalsePositive = 0;
        int appCount = 0;
//        long allLeak=0;
//        long allALl=0;
        WriteFile writeFile=new WriteFile(Config.unitNeedAnalysisGenerate + "/FalsePositiveRateOfStatic"+"/"+"FalsePositiveRateOfStatic.csv",false,logger);
        for (String app : testSuccessApp) {
            Map<String, Set<String>> permissionUnitStrSetMap = export.get(app);
            long all = 0;
            long leak = 0;
            double oneAppFalsePositive = 0;
            int permissionKind = 0;
            if (permissionUnitStrSetMap != null) {//这个app 测试成功了
                appCount = appCount + 1;

                permissionKind = permissionUnitStrSetMap.keySet().size();

                Map<String, Set<String>> permissionLeakUnitStrSetMap = appPermissionPLCountMap.get(app);

                if (permissionLeakUnitStrSetMap != null) {//没有app的权限泄露
                    for (Map.Entry<String, Set<String>> entryPermissionUnitStrSetMap : permissionUnitStrSetMap.entrySet()) {
                        String permission = entryPermissionUnitStrSetMap.getKey();
                        Set<String> permissionLeakUnitStrSet = permissionLeakUnitStrSetMap.get(permission);
                        all = entryPermissionUnitStrSetMap.getValue().size();
                        if (permissionLeakUnitStrSet != null) {
                            if (entryPermissionUnitStrSetMap.getValue().containsAll(permissionLeakUnitStrSet)) {
                                leak = permissionLeakUnitStrSet.size();


                            } else {
                                permissionKind=permissionKind-1;
                                continue;

                            }
                        } else {
                            leak = 0;
                        }

                        oneAppFalsePositive = oneAppFalsePositive + ((float) (all-leak)) / all;
                        System.out.println(permission+","+oneAppFalsePositive);

                    }
                } else {
                    for (Map.Entry<String, Set<String>> entryPermissionUnitStrSetMap : permissionUnitStrSetMap.entrySet()) {
                        leak = 0;
                        all = entryPermissionUnitStrSetMap.getValue().size();
                        oneAppFalsePositive = oneAppFalsePositive + 1;
                        System.out.println(entryPermissionUnitStrSetMap.getKey()+","+oneAppFalsePositive); System.out.println("all"+","+allAppFalsePositive);
                    }
                }

                oneAppFalsePositive = oneAppFalsePositive / permissionKind;
                writeFile.writeStr(app+","+oneAppFalsePositive+"\n");
                System.out.println(app+","+oneAppFalsePositive);

                allAppFalsePositive = allAppFalsePositive + oneAppFalsePositive;

            }


        }

        allAppFalsePositive = allAppFalsePositive/appCount;
        System.out.println("all"+","+allAppFalsePositive);
        writeFile.close();


    }
}
