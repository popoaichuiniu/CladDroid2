package com.popoaichuiniu.jacy;

import com.popoaichuiniu.util.Config;
import com.zhou.ApkSigner;

public class Main {

    public static void main(String[] args) {
        GenerateIntentIfUnitToGetInfo.main(null);
        DynamicSE.main(null);
        ApkSigner.isTest= Config.isDynamicSETest;
        ApkSigner.defaultAppDirPath=Config.dynamicAppDir;
        ApkSigner.main(null);

    }
}
