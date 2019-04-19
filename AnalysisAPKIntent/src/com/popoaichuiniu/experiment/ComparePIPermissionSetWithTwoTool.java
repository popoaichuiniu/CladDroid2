package com.popoaichuiniu.experiment;

import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.WriteFile;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ComparePIPermissionSetWithTwoTool {

    public static Logger logger= new MyLogger(Config.DynamicSE_logDir+"/ComparePIPermissionSetWithTwoTool","exceptionLogger").getLogger();
    public static void main(String[] args) {
        String toolOldDynamicSEResultDir=Config.DynamicSE_logDir+"/"+"testLog_2019_3_26_wandoujia_all_app_dynamicSE";
        String toolDynamicSEResultDir= Config.DynamicSE_logDir+"/"+"testLog_2019_4_7_wandoujia_dynamicSE";

        String toolIntentFuzzer=Config.DynamicSE_logDir+"/"+"testLog_2019_3_30_test_intentFuzzer";

        String toolOldStaticSE="/home/zms/logger_file/old_result_2019_2_23/testlog/old_results/all_app_nexta";
        String toolStaticSE="/home/zms/logger_file/testLog_2017_4_9_se";

        ComputePlPermissions computePlPermissionsOldDYSE=new ComputePlPermissions(toolOldDynamicSEResultDir);
        computePlPermissionsOldDYSE.computePl();

        ComputePlPermissions computePlPermissionsDYSE=new ComputePlPermissions(toolDynamicSEResultDir);
        computePlPermissionsDYSE.computePl();

        ComputePlPermissions computePlPermissionsIntentFuzzer=new ComputePlPermissions(toolIntentFuzzer);
        computePlPermissionsIntentFuzzer.computePl();

        ComputePlPermissions computePlPermissionsOldStaticSE=new ComputePlPermissions(toolOldStaticSE);
        computePlPermissionsOldStaticSE.computePl();

        ComputePlPermissions computePlPermissionsStaticSE=new ComputePlPermissions(toolStaticSE);
        computePlPermissionsStaticSE.computePl();

        compareTwoTools(computePlPermissionsOldDYSE, computePlPermissionsDYSE,"compareToolOldDYSE_DYSE");

        compareTwoTools(computePlPermissionsOldDYSE, computePlPermissionsIntentFuzzer,"compareToolOldDYSE_IntentFuzzer");

        compareTwoTools(computePlPermissionsDYSE, computePlPermissionsIntentFuzzer,"compareToolDYSE_IntentFuzzer");

        compareTwoTools(computePlPermissionsDYSE, computePlPermissionsStaticSE,"compareToolDYSE_StaticSE");

        compareTwoTools(computePlPermissionsOldStaticSE, computePlPermissionsStaticSE,"compareToolOldStaticSE_StaticSE");

        compareTwoTools(computePlPermissionsStaticSE, computePlPermissionsIntentFuzzer,"compareToolStaticSE_IntentFuzzer");

        compareTwoTools(computePlPermissionsOldStaticSE, computePlPermissionsOldDYSE,"compareToolOldStaticSE_OldDYSE");


        //

        compareTwoTools(computePlPermissionsStaticSE, computePlPermissionsOldDYSE,"compareToolStaticSE_OldDYSE");
    }

    private static void compareTwoTools(ComputePlPermissions computePlPermissionsTool1, ComputePlPermissions computePlPermissionsTool2,String resultFileName) {
        Set<String> allPermissionsSet=new HashSet<>();
        allPermissionsSet.addAll(computePlPermissionsTool1.permissionAppPLCountMap.keySet());
        allPermissionsSet.addAll(computePlPermissionsTool2.permissionAppPLCountMap.keySet());

        Set<String> allAppSet=new HashSet<>();

        allAppSet.addAll(computePlPermissionsTool1.appPermissionPLCountMap.keySet());
        allAppSet.addAll(computePlPermissionsTool2.appPermissionPLCountMap.keySet());

        int allAPPPermission=0;
        float allGradeSum=0;

        float max=Integer.MIN_VALUE;
        float min=Integer.MAX_VALUE;

        WriteFile writeFile=new WriteFile(Config.logDir+"/ComparePIPermissionSetWithTwoTool"+"/"+resultFileName+".csv",false,logger);
        for(String app:allAppSet)
        {
            for(String permission:allPermissionsSet)
            {


                Set<String> plPointSetDYSE=getPLPointSet(computePlPermissionsTool1.appPermissionPLCountMap,app,permission);

                Set<String> plPointSetIntentFuzzer=getPLPointSet(computePlPermissionsTool2.appPermissionPLCountMap,app,permission);

                Set<String> intersection=new HashSet<>();
                intersection.addAll(plPointSetDYSE);
                intersection.retainAll(plPointSetIntentFuzzer);

                Set<String> plPointSetDYSEOnly=new HashSet<>(plPointSetDYSE);

                plPointSetDYSEOnly.removeAll(intersection);

                Set<String> plPointSetIntentFuzzerOnly=new HashSet<>(plPointSetIntentFuzzer);
                plPointSetIntentFuzzerOnly.removeAll(intersection);


                int den =plPointSetDYSE.size()+plPointSetIntentFuzzer.size()-intersection.size();
                int num= (plPointSetDYSEOnly.size()-plPointSetIntentFuzzerOnly.size());
                if(den>0)
                {   allAPPPermission=allAPPPermission+1;
                    float oneGrade=((float)num)/den;
                    allGradeSum=allGradeSum+oneGrade;
                    if(oneGrade>max)
                    {
                        max=oneGrade;
                    }

                    if(oneGrade<min)
                    {
                        min=oneGrade;
                    }
                    System.out.println(oneGrade);
                    //writeFile.writeStr("111111111111111111111111111\n");
                    writeFile.writeStr(plPointSetDYSEOnly.size()+",");
                    writeFile.writeStr(plPointSetIntentFuzzerOnly.size()+",");
                    writeFile.writeStr(den+",");
                    writeFile.writeStr(oneGrade+"\n");
                    //writeFile.writeStr("222222222222222222222222222\n");
                    writeFile.flush();
                }






            }


        }



        float aveGrade=allGradeSum/allAPPPermission;

        System.out.println("aver grade,"+aveGrade);

        writeFile.writeStr("aver grade,"+aveGrade+"\n");

        System.out.println("min grade,"+min);

        writeFile.writeStr("min grade,"+min+"\n");

        System.out.println("max grade,"+max);

        writeFile.writeStr("max grade,"+max+"\n");


        writeFile.close();
    }

    public static Set<String> getPLPointSet(Map<String, Map<String, Set<String>>> appPermissionPLCountMap, String app, String permission) {

        Map<String, Set<String>> permissionPLCountMap=appPermissionPLCountMap.get(app);
        if(permissionPLCountMap!=null)
        {
            Set<String> plPointSet=permissionPLCountMap.get(permission);

            if(plPointSet!=null)
            {
                return plPointSet;
            }
        }

        return new HashSet<>();
    }
}
