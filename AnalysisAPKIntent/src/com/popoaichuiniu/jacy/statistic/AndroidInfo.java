package com.popoaichuiniu.jacy.statistic;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;


import com.popoaichuiniu.util.ExceptionStackMessageUtil;
import org.apache.commons.io.FileUtils;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import soot.jimple.infoflow.android.axml.AXmlAttribute;
import soot.jimple.infoflow.android.axml.AXmlNode;

public class AndroidInfo {//日志ok


    //private static String permissionMappingFilePath="./AnalysisAPKIntent/mapping_5.1.1.csv";//API=22  这个csv有问题,还有一个是别人修正的

    //private static String jellybean_allmappings="jellybean_allmappings.txt";//API=16,这个太老

    private static String permissionMethodMapping_25 = "AnalysisAPKIntent/permissions_25.json";//API=25 androguard提供
    private static String all_permissions_25 = "AnalysisAPKIntent/all_permissions_25.json";//API=25 androguard提供

    private static String dangerousOrSpecailPermissionFilePath = "AnalysisAPKIntent/dangerousOrSpecialPermission_lose.txt";//存在API>=26

    private static String thirdLibraryFilePath = "AnalysisAPKIntent/thirdLibrary.txt";
    private static Map<String, Set<String>> permissionAndroguardMethods = null; //android API 和权限映射


    private static Map<String, List<String>> permissionDangerousAndSpecialMethodsUltimulateString = null;//被危险或者特殊权限保护起来的API 和权限映射


    private static Set<String> allPermissions = null;//android所有权限

    private static Set<String> thirdLibraryPackageNameSet = null;//第三方库包名集合


    static {
        getAllPermissions();
        processJsonPermissionMapping();

    }


    public static Map<String, Set<String>> getPermissionAndroguardMethods() {
        return permissionAndroguardMethods;
    }

    public static Set<String> getThirdLibraryPackageNameSet() {

        if (thirdLibraryPackageNameSet == null || thirdLibraryPackageNameSet.size() == 0) {
            thirdLibraryPackageNameSet = new HashSet<>();
            try {

                BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(thirdLibraryFilePath)));
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {

                    if (line.split("\\.").length >= 3) {
                        thirdLibraryPackageNameSet.add(line);
                    }


                }


                bufferedReader.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return thirdLibraryPackageNameSet;
    }

    private static String convertAndroGuardMethodSignatureToSoot(String methodSignature) {
        //"Landroid/telephony/SmsManager;-sendTextMessage-(Ljava/lang/String; Ljava/lang/String; Ljava/lang/String; Landroid/app/PendingIntent; Landroid/app/PendingIntent;)V"
        //<android.telephony.SmsManager: sendTextMessage(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/app/PendingIntent;Landroid/app/PendingIntent;)V>
        if (methodSignature == null) {
            return null;
        } else {
            String temp = methodSignature.replaceAll("-", "");
            String packageStr = temp.substring(0, temp.indexOf(";")).substring(1).replaceAll("/", ".") + ": ";

            return "<" + packageStr + temp.substring(temp.indexOf(";") + 1).replace(" ", "") + ">";

        }


    }


    public static Set<String> getAllPermissions() {


        if (allPermissions == null || allPermissions.size() == 0) {
            try {
                allPermissions = new HashSet<>();
                File jsonFile = new File(all_permissions_25);
                String content = FileUtils.readFileToString(jsonFile, "UTF-8");
                JSONObject jsonObject = new JSONObject(content);
                allPermissions.addAll(jsonObject.keySet());

            } catch (IOException e) {
                Logger.getLogger(AndroidInfo.class).error(e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e));
            }
        }


        return allPermissions;


    }

    public static void processJsonPermissionMapping() {

        permissionAndroguardMethods = new HashMap<>();
        permissionDangerousAndSpecialMethodsUltimulateString = new HashMap<String, List<String>>();
        List<String> dangerousAndSpecialPermissions = getDangerousOrSpecialPermission();
        File jsonFile = new File(permissionMethodMapping_25);
        String content = null;
        try {
            content = FileUtils.readFileToString(jsonFile, "UTF-8");
            JSONObject jsonObject = new JSONObject(content);
            System.out.println("**********************JsonPermissionmapping********************************");
            for (Iterator<String> iterator = jsonObject.keys(); iterator.hasNext(); ) {
                String key = iterator.next();
                //System.out.println(key);
                //System.out.println(jsonObject.getJSONArray(key));
                JSONArray permissionArray = jsonObject.getJSONArray(key);
                Set<String> permissionSet = new HashSet<>();
                if (permissionArray != null) {

                    for (Iterator<Object> permissionIterator = permissionArray.iterator(); permissionIterator.hasNext(); ) {
                        permissionSet.add((String) permissionIterator.next());
                    }
                }
                String modifiedKey = convertAndroGuardMethodSignatureToSoot(key);
                System.out.println(modifiedKey);
                System.out.println(permissionSet);
                permissionAndroguardMethods.put(modifiedKey, permissionSet);

                //*********************permissionDangerousAndSpecialMethods*************************
                for (String permission : permissionSet) {
                    if (dangerousAndSpecialPermissions.contains(permission)) {
                        System.out.println("***************dangerousAndSpecialPermissions******************");
                        System.out.println(modifiedKey);
                        System.out.println("****************dangerousAndSpecialPermissions*****************");
                        List<String> arrPermission = permissionDangerousAndSpecialMethodsUltimulateString.get(modifiedKey);
                        if (arrPermission == null) {
                            arrPermission = new ArrayList<>();
                            arrPermission.add(permission);
                            permissionDangerousAndSpecialMethodsUltimulateString.put(modifiedKey, arrPermission);
                        } else {
                            arrPermission.add(permission);
                        }

                    }
                }


            }
            System.out.println("**********************JsonPermissionmapping********************************");


        } catch (IOException e) {
            Logger.getLogger(AndroidInfo.class).error(e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e));
        }


    }

    public static Map<String, List<String>> getPermissionDangerousAndSpecialMethods() {
        if (permissionDangerousAndSpecialMethodsUltimulateString == null) {
            processJsonPermissionMapping();
        }
        return permissionDangerousAndSpecialMethodsUltimulateString;
    }

    private static List<String> getDangerousOrSpecialPermission() {


        List<String> dangerousOrSpecialPermissions = new ArrayList<>();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(dangerousOrSpecailPermissionFilePath));
        } catch (FileNotFoundException e) {
            Logger.getLogger(AndroidInfo.class).error(e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e));


        }

        String line = null;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String permission = line.split(" ")[0];

                System.out.println("dangerousOrSpecialPermission :" + "****" + permission);

                dangerousOrSpecialPermissions.add(permission);

            }
        } catch (IOException e) {
            Logger.getLogger(AndroidInfo.class).error(e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e));
        }

        return dangerousOrSpecialPermissions;
    }


    private String appPath = null;
    private Logger exceptionLogger = null;
    
    
    private Map<String, AXmlNode> EAs = null;
    private List<String> string_EAs = null;
    private Map<String, List<String>> EAProtectedPermission = null;//EA的permission
   
    private Map<String, AXmlNode> components = null;
    private Map<String, Map<AXmlNode, String>> permissionProtectedEAs = null;
    private Map<String, Map<AXmlNode, String>> permissionProtectedComponents = null;

    private Map<String, AXmlNode> selfDefinePermissionsMap = null;


    


    public AndroidInfo(String appPath, Logger logger) {
        super();
        this.appPath = appPath;
        this.exceptionLogger = logger;
        caculateSelfDefinePermission();
        calculateEAs();
        calculatePermissionProtectedEAs();
        calculatePermissionProtectedComponents();

    }

    public Map<String, AXmlNode> getComponents() {
        return components;
    }


    public String getAppPath() {
        return appPath;
    }

    public String getPackageName(String appPath) {

        try {
            MyProcessManifest processMan = new MyProcessManifest(appPath);
            return processMan.getPackageName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
    

    public Map<String, AXmlNode> getSelfDefinePermissionsMap() {
        return selfDefinePermissionsMap;
    }

    private void caculateSelfDefinePermission() {
        try {
            MyProcessManifest processMan = new MyProcessManifest(appPath);
            selfDefinePermissionsMap = processMan.getSelfDefinePermissions();


        } catch (IOException e) {
            String message = appPath + "&&" + "IOException" + "###" + e.getMessage() + "###" + ExceptionStackMessageUtil.getStackTrace(e);
            exceptionLogger.error(message);

        } catch (XmlPullParserException e) {
            String message = appPath + "&&" + "XmlPullParserException" + "###" + e.getMessage() + "###" + ExceptionStackMessageUtil.getStackTrace(e);
            exceptionLogger.error(message);
        }

    }


    private void calculateEAs() {

        try {
            MyProcessManifest processMan = new MyProcessManifest(appPath);
            EAs = new HashMap<String, AXmlNode>();
            string_EAs = new ArrayList<String>();

            components = processMan.getComponentClasses();

            for (Iterator<Entry<String, AXmlNode>> iterator = components.entrySet().iterator(); iterator.hasNext(); ) {
                String componentName = iterator.next().getKey();
                AXmlNode node = components.get(componentName);
                if (node == null) {

                    exceptionLogger.warn("AndroidMainifest文件节点为空！");
                }

                try {
                    boolean flag = judgeEA(node);

                    if (flag) {
                        // System.out.println(componentName);
                        // System.out.println(node.getTag());
                        EAs.put(componentName, node);
                        string_EAs.add(componentName);

                    }
                } catch (ProcessUnexpectedException e) {

                    exceptionLogger.warn(e.getMessage());

                    EAs.put(componentName, node);//保守的认为也是暴露组件
                    string_EAs.add(componentName);

                }


            }

        } catch (IOException e) {

            String message = appPath + "&&" + "IOException" + "###" + e.getMessage() + "###" + ExceptionStackMessageUtil.getStackTrace(e);

            exceptionLogger.error(message);


        } catch (XmlPullParserException e) {
            String message = appPath + "&&" + "XmlPullParserException" + "###" + e.getMessage() + "###" + ExceptionStackMessageUtil.getStackTrace(e);

            exceptionLogger.error(message);
        }


    }

    public Map<String, AXmlNode> getEAs() {
        return EAs;
    }

    public List<String> getString_EAs() {
        return string_EAs;
    }

    public Map<String, Map<AXmlNode, String>> getPermissionProtectedEAs() {
        return permissionProtectedEAs;
    }


    private boolean judgeEA(AXmlNode node) throws ProcessUnexpectedException {

        AXmlAttribute<Boolean> exported = (AXmlAttribute<Boolean>) node.getAttribute("exported");

        if (exported != null) {

            if (exported.getValue() instanceof Boolean) {
                if (exported.getValue()) {
                    return true;

                } else {
                    return false;
                }
            } else {
                //存在异常


                throw new ProcessUnexpectedException(appPath + "&&" + node + "exported属性异常！:" + node.getAttribute("exported") + "\n");


            }

        } else {
            //对于content provider 默认为true

            if (node.getTag().equals("provider")) {
                return true;
            }

        }

        if (node.getChildrenWithTag("intent-filter").size() > 0) {
            return true;
        } else {
            return false;
        }

    }


    public void calculatePermissionProtectedEAs() {

        if (EAs == null) {
            calculateEAs();
        }
        permissionProtectedEAs = new HashMap<String, Map<AXmlNode, String>>();

        EAProtectedPermission = new HashMap<>();
        for (Iterator<Map.Entry<String, AXmlNode>> iterator = EAs.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, AXmlNode> eaNode = iterator.next();
            String componentName = eaNode.getKey();

            AXmlNode eaNodeAXMlValue = eaNode.getValue();
            //android:permission只会有一个
            Map<String, AXmlAttribute<?>> attributeMap = eaNodeAXMlValue.getAttributes();
            for (Map.Entry<String, AXmlAttribute<?>> entry : attributeMap.entrySet()) {
                String attrName = entry.getKey().trim();
                if (attrName.equals("permission") || attrName.equals("readPermission") || attrName.equals("writePermission")) {
                    String value = (String) entry.getValue().getValue();
                    System.out.println(value);
                    Map<AXmlNode, String> temp = new HashMap<AXmlNode, String>();
                    temp.put(eaNodeAXMlValue, value);
                    permissionProtectedEAs.put(componentName, temp);

                    List<String> permissionList = new ArrayList<>();
                    permissionList.add(value);
                    EAProtectedPermission.put(componentName, permissionList);//保护EA的权限
                }
            }


        }

    }

    public void calculatePermissionProtectedComponents() {//allComponents

        if (components == null)
            return;
        permissionProtectedComponents = new HashMap<String, Map<AXmlNode, String>>();
        for (Iterator<Map.Entry<String, AXmlNode>> iterator = components.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, AXmlNode> compoNode = iterator.next();
            String componentName = compoNode.getKey();

            AXmlNode componentNodeAXMlValue = compoNode.getValue();
            // System.out.println(eaNode.getAttribute("permission"));
            AXmlAttribute<?> permissionAttribute = componentNodeAXMlValue.getAttribute("permission");
            if (permissionAttribute != null) {
                String value = (String) permissionAttribute.getValue();
                System.out.println(value);
                Map<AXmlNode, String> temp = new HashMap<AXmlNode, String>();
                temp.put(componentNodeAXMlValue, value);
                permissionProtectedComponents.put(componentName, temp);
            }

        }

    }

}
