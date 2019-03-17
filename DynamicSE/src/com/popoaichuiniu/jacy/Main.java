package com.popoaichuiniu.jacy;

import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.ExceptionStackMessageUtil;
import com.popoaichuiniu.util.MyLogger;
import com.popoaichuiniu.util.Util;
import com.zhou.ApkSigner;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class Main {
    private static Logger exceptionLogger = new MyLogger(Config.DynamicSE_logDir, "exception").getLogger();

    private static Logger infoLogger = new MyLogger(Config.DynamicSE_logDir, "info").getLogger();

    public static void main(String[] args) {
        GenerateIntentIfUnitToGetInfo.main(null);
        DynamicSE.main(null);
        ApkSigner.isTest = Config.isDynamicSETest;
        ApkSigner.defaultAppDirPath = Config.dynamicSEAppDir;
        ApkSigner.main(null);


//        try {
//            int status = Util.exeCmd(new File("testAPP"), infoLogger, exceptionLogger, "/home/lab418/anaconda3/bin/python", "testAPP.py", Config.dynamicSEAppDir, Config.DynamicSE_logDir);
//            if (status != 0) {
//                exceptionLogger.error(status + " exeCmd error");
//            }
//        } catch (IOException e) {
//            exceptionLogger.error(e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e));
//        } catch (InterruptedException e) {
//            exceptionLogger.error(e.getMessage() + "##" + ExceptionStackMessageUtil.getStackTrace(e));
//        }

    }
}
