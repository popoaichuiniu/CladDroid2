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
        String toolDynamicSEResultDir= Config.DynamicSE_logDir+"/"+"testLog_2019_3_26_wandoujia_all_app_dynamicSE";

        String toolIntentFuzzer=Config.DynamicSE_logDir+"/"+"testLog_2019_3_30_test_intentFuzzer";

        ComputePlPermissions computePlPermissionsDYSE=new ComputePlPermissions(toolDynamicSEResultDir);
        computePlPermissionsDYSE.computePl();

        ComputePlPermissions computePlPermissionsIntentFuzzer=new ComputePlPermissions(toolIntentFuzzer);
        computePlPermissionsIntentFuzzer.computePl();

        Set<String> allPermissionsSet=new HashSet<>();
        allPermissionsSet.addAll(computePlPermissionsDYSE.permissionAppPLCountMap.keySet());
        allPermissionsSet.addAll(computePlPermissionsIntentFuzzer.permissionAppPLCountMap.keySet());

        Set<String> allAppSet=new HashSet<>();

        allAppSet.addAll(computePlPermissionsDYSE.appPermissionPLCountMap.keySet());
        allAppSet.addAll(computePlPermissionsIntentFuzzer.appPermissionPLCountMap.keySet());

        int allAPPPermission=0;
        float allGradeSum=0;

        float max=Integer.MIN_VALUE;
        float min=Integer.MAX_VALUE;

        WriteFile writeFile=new WriteFile(Config.DynamicSE_logDir+"/ComparePIPermissionSetWithTwoTool"+"/"+"compareToolDYSE_IntentFuzzer.txt",false,logger);
        for(String app:allAppSet)
        {
            for(String permission:allPermissionsSet)
            {


                Set<String> plPointSetDYSE=getPLPointSet(computePlPermissionsDYSE.appPermissionPLCountMap,app,permission);

                Set<String> plPointSetIntentFuzzer=getPLPointSet(computePlPermissionsIntentFuzzer.appPermissionPLCountMap,app,permission);

                Set<String> intersection=new HashSet<>();
                intersection.addAll(plPointSetDYSE);
                intersection.retainAll(plPointSetIntentFuzzer);

                Set<String> plPointSetDYSEOnly=new HashSet<>(plPointSetDYSE);

                plPointSetDYSEOnly.removeAll(intersection);

                Set<String> plPointSetIntentFuzzerOnly=new HashSet<>(plPointSetIntentFuzzer);
                plPointSetIntentFuzzerOnly.removeAll(intersection);


                int den =plPointSetDYSE.size()+plPointSetIntentFuzzer.size()-intersection.size();
                int num= 0-(plPointSetDYSEOnly.size()-plPointSetIntentFuzzerOnly.size());
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
                    writeFile.writeStr("111111111111111111111111111\n");
                    writeFile.writeStr(plPointSetDYSEOnly+"\n");
                    writeFile.writeStr(plPointSetIntentFuzzerOnly+"\n");
                    writeFile.writeStr(oneGrade+"\n");
                    writeFile.writeStr("222222222222222222222222222\n");
                    writeFile.flush();
                }






            }


        }
        writeFile.close();


        float aveGrade=allGradeSum/allAPPPermission;

        System.out.println("aver grade:"+aveGrade);

        System.out.println("min grade:"+min);

        System.out.println("max grade:"+max);






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
