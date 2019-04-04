package com.popoaichuiniu.intentGen;

import com.popoaichuiniu.util.WriteFile;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class IntentInfoFileGenerate {

    public static boolean generateIntentInfoFile(String appPath, List<IntentInfo> intentInfoList,String name, Logger exceptionLogger) {


        WriteFile writeFileIntentInfo = new WriteFile(appPath + "_" + name, false, exceptionLogger);
        for (IntentInfo intentInfo : intentInfoList) {
            //            String testAppPath;
//            String appPackageName;
//            String comPonentType;
//            String comPonentName;
//            String comPonentAction;
//            List<String> comPonentCategory;
//            String comPonentData;
//            Map<String,String> comPonentExtraData;

            String line = "";
            line = line + intentInfo.appPath + "#";
            line = line + intentInfo.appPackageName + "#";
            line = line + intentInfo.comPonentType + "#";
            line = line + intentInfo.comPonentName + "#";
            line = line + intentInfo.comPonentAction + "#";
            String categoryStr = "";
            if (intentInfo.comPonentCategory == null || intentInfo.comPonentCategory.size() == 0) {
                categoryStr = "null";
            } else {
                for (String category : intentInfo.comPonentCategory) {
                    if (categoryStr.equals("")) {
                        categoryStr = category;
                    } else {
                        categoryStr = categoryStr + "," + category;
                    }

                }

            }

            line = line + categoryStr + "#";
            line = line + intentInfo.comPonentData+ "#";

            String extraString = "";
            if (intentInfo.comPonentExtraData == null || intentInfo.comPonentExtraData.size() == 0) {
                extraString = "null";
            } else {
                for (IntentExtraKey extraData : intentInfo.comPonentExtraData) {
                    if (extraString.equals("")) {
                        extraString = extraData.type + "&" + extraData.key + "&" + extraData.value;
                    } else {
                        extraString = extraString + ";" + extraData.type + "&" + extraData.key + "&" + extraData.value;
                    }
                }
            }
            line = line + extraString;
            writeFileIntentInfo.writeStr(line + "\n");
        }


        writeFileIntentInfo.close();


        return false;


    }


}
