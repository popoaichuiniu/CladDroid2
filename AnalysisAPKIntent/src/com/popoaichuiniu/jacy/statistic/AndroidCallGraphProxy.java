package com.popoaichuiniu.jacy.statistic;

import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.ExceptionStackMessageUtil;
import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;
import soot.jimple.toolkits.callgraph.CallGraph;

import java.io.IOException;

public class AndroidCallGraphProxy implements CGOperation {

    public AndroidCallGraph androidCallGraph = null;

    public AndroidCallGraphProxy(String appPath, String androidPlatformPath, Logger logger) {
        androidCallGraph = new AndroidCallGraph(appPath, androidPlatformPath);


        try {
            androidCallGraph.calculateAndroidCallGraph();

        } catch (IOException e) {


            logger.error(appPath + "&&" + "IOException" + "###" + e.getMessage() + "###" + ExceptionStackMessageUtil.getStackTrace(e));


        } catch (XmlPullParserException e) {


            logger.error(appPath + "&&" + "XmlPullParserException" + "###" + e.getMessage() + "###" + ExceptionStackMessageUtil.getStackTrace(e));
        }


    }

    @Override
    public CallGraph getCG() {
        return androidCallGraph.getCg();
    }


}
