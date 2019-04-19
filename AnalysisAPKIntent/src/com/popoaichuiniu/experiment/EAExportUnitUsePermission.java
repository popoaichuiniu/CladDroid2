package com.popoaichuiniu.experiment;

import com.popoaichuiniu.jacy.statistic.AndroidInfo;
import com.popoaichuiniu.util.*;
import org.apache.log4j.Logger;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import soot.SootMethod;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EAExportUnitUsePermission {

    private static String appDir = Config.wandoijiaAPP;

    private static Logger logger = new MyLogger(Config.unitNeedAnalysisGenerate + "/EAExportUnitUsePermission", "exceptionLogger").getLogger();

    public static Map<String, Map<String, Set<String>>> export = new HashMap<>();
    public static void main(String[] args) {

        Map<String, Set<String>> sootMethodPermissionMap = AndroidInfo.getPermissionAndroguardMethods();

        Map<String, Set<String>> permissionAPIMap = Util.getPermissionAPIMap(sootMethodPermissionMap);
        Set<String> allPermissionsSet = new HashSet<>();

        File xmlFile = new File(Config.unitNeedAnalysisGenerate + "/" + new File(appDir).getName() + "_DIR_permissionUse.xml");

        Document document = null;
        Element rootElement = null;
        if (xmlFile.exists()) {
            try {
                document = new SAXReader().read(xmlFile).getDocument();
                rootElement = document.getRootElement();
            } catch (DocumentException e) {
                e.printStackTrace();
            }

        } else {
            document = DocumentHelper.createDocument();

            rootElement = document.addElement("APKs");

        }


        ReadFileOrInputStream readFileOrInputStreamDangerous = new ReadFileOrInputStream("dangerousPermission", logger);
        Set<String> dangerousPermissionSet = readFileOrInputStreamDangerous.getAllContentLinSet();

        WriteFile writeFileDangerousApp = new WriteFile(Config.unitNeedAnalysisGenerate + "/" + "dangerousApp", false, logger);


        for (File file : new File(appDir).listFiles()) {
            if (file.getName().endsWith("apk_UnitsNeedAnalysis.txt"))//"1元乐购.apk_UnitsNeedAnalysis.txt"
            {
                ReadFileOrInputStream readFileOrInputStream = new ReadFileOrInputStream(file.getAbsolutePath(), logger);
                String apkName = appDir + "/" + file.getName().substring(0, file.getName().length() - 22);
                System.out.println(apkName);

                Map<String, Set<String>> permissionUnitStrSetMap = export.get(apkName);
                if (permissionUnitStrSetMap == null) {
                    permissionUnitStrSetMap = new HashMap<>();
                }
                Set<String> appPermissionSet = new HashSet<>();
                for (String str : readFileOrInputStream.getAllContentLinSet()) {
                    String[] contentArray = str.split("#");
                    String sootMethodSignature = contentArray[3];
                    String method = contentArray[0];
                    String unit = contentArray[2];


                    Set<String> permissionSet = sootMethodPermissionMap.get(sootMethodSignature);

                    if (permissionSet != null) {
                        for (String permission : permissionSet) {

                            appPermissionSet.add(permission);
                            allPermissionsSet.add(permission);

                            Set<String> unitStrSet = permissionUnitStrSetMap.get(permission);

                            if (unitStrSet == null) {
                                unitStrSet = new HashSet<>();
                            }

                            //unitStrSet.add(method+"#"+unit);
                            unitStrSet.add(unit);
                            permissionUnitStrSetMap.put(permission, unitStrSet);


                        }
                    }


                }

                export.put(apkName,permissionUnitStrSetMap);



                for (String dangerousPermission : dangerousPermissionSet) {
                    if (appPermissionSet.contains(dangerousPermission)) {
                        writeFileDangerousApp.writeStr(apkName + "\n");
                    }
                }

                addElementNode(document, rootElement, apkName, appPermissionSet);


            }

        }

        writeFileDangerousApp.close();

        try {

            XMLWriter xmlWriter = new XMLWriter(new FileWriter(Config.unitNeedAnalysisGenerate + "/" + new File(appDir).getName() + "_DIR_permissionUse.xml"));
            xmlWriter.write(document);
            xmlWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        WriteFile writeFileAllPermission = new WriteFile(Config.unitNeedAnalysisGenerate + "/" + new File(appDir).getName() + "_DIR_AllPermission.txt", false, logger);
        for (String permissionString : allPermissionsSet) {

            writeFileAllPermission.writeStr(permissionString + "\n");//all permission


        }
        writeFileAllPermission.writeStr("\n\n\n");


        for (String permissionString : allPermissionsSet) {

            writeFileAllPermission.writeStr(permissionString + ":" + "\n");//permission
            for (String api : permissionAPIMap.get(permissionString)) {
                writeFileAllPermission.writeStr(api + "\n");//api
            }
            writeFileAllPermission.writeStr("\n");
        }
        writeFileAllPermission.close();
    }

    private static void addElementNode(Document document, Element rootElement, String apkName, Set<String> appPermissionSet) {


        Element apkElement = rootElement.addElement("APK");
        apkElement.addAttribute("name", apkName);

        for (String permission : appPermissionSet) {
            Element permissionElement = apkElement.addElement("permission");

            permissionElement.addText(permission);
        }


    }
}
