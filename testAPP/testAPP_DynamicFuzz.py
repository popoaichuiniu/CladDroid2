import subprocess
import time
import os
import threading
import sys
import psutil


def execuateCmd(cmd):
    # status,output=subprocess.getstatusoutput(cmd);
    proc = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    outs, errs = proc.communicate()
    return proc.returncode, str(outs, encoding="utf-8") + "##" + str(errs, encoding="utf-8"), proc


def execuateCmdPreventBlock(cmd):
    proc = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    try:
        outs, errs = proc.communicate(timeout=100)
        return proc.returncode, str(outs, encoding="utf-8") + "##" + str(errs, encoding="utf-8"), proc
    except subprocess.TimeoutExpired:
        proc.kill()
        try:
            outs, errs = proc.communicate(timeout=300)
            return proc.returncode, str(outs, encoding="utf-8") + "##" + str(errs, encoding="utf-8"), proc
        except subprocess.TimeoutExpired:
            proc.kill()
            return -1, '', None


def isADBWorkNormal():  # ok
    adb_status = "adb devices"
    status, output, proc = execuateCmdPreventBlock(adb_status)
    print("*" + output + "*")
    if (status == 0):
        index1 = output.find("emulator")
        index2 = output.find("offline")
        if (index1 != -1 and index2 == -1):
            return True
        else:
            return False
    else:
        return False


def isTestAPPAlive():  # ok
    if (not isADBWorkNormal()):
        print("adb work abnormal!")
        return False;
    else:
        app_status = "adb shell ps |grep jacy.popoaichuiniu.com.testpermissionleakge"
        # app_status = "adb shell ps |grep com.android.settings"
        status, output, proc = execuateCmdPreventBlock(app_status)
        if (output.find("jacy.popoaichuiniu.com.testpermissionleakge") != -1):
            print(output)
            return True
        else:
            return False


# def isGuakeAlive():#guake has not started completely ,but this method return true
#         app_status = "ps -a |grep guake"
#         status, output,proc = execuateCmd(app_status)
#         index=-1
#         count=0
#         isFirstIn=True
#
#         while (isFirstIn or index!=-1):
#             isFirstIn=False
#             index=output.find("guake",index+1)
#             if(index!=-1):
#                 count=count+1
#
#         if count ==3:
#             print("guake ok")
#             return True
#         else:
#             return False

def installNewAPP(appPath):  # ok
    if (not isADBWorkNormal()):
        print("adb work abnormal!")
        return False;
    else:
        install_app = "adb install -r" + " " + appPath
        status, output, proc = execuateCmdPreventBlock(install_app)
        if (status == 0):
            index = output.find("Failure")
            if (index != -1):
                info = appPath + "安装失败" + "eeeeeeeeeeee"
                print(info)
                return False, info
            else:
                info = appPath + "安装成功"
                print(info)
                return True, info
        else:
            info = "安装失败" + "error:" + str(status) + "," + output + "eeeeeeeeeeee"
            print(info)
            return False, info


def getPackageName(appPath):  #
    get_package_cmd = "aapt dump badging " + appPath
    status, output, proc = execuateCmdPreventBlock(get_package_cmd)
    if (status == 0):
        # print("*"+output+"*")
        tempStr = output.split("\n")[0]
        # print(tempStr)
        index1 = tempStr.find("'")
        index2 = tempStr.find("'", index1 + 1)
        packageName = tempStr[index1 + 1:index2]

        return packageName
    else:
        print("error:" + str(status) + "," + output)
        return None


def uninstall_app_by_packageName(packageName):
    if (not isADBWorkNormal()):
        print("adb work abnormal!")
        return False
    else:
        install_app = "adb uninstall " + " " + packageName
        status, output, proc = execuateCmdPreventBlock(install_app)
        if (status == 0):  # Success
            index = output.find("Success")
            if (index != -1):
                print(packageName + "卸载成功\n")
                return True
            else:
                print(packageName + "卸载失败\n")
                return False

        else:
            print("error:" + str(status) + "," + output)
            return False


def uninstall_app_by_path(appPath):  # ok
    if (not isADBWorkNormal()):
        print("adb work abnormal!")
        return False
    else:
        killTestAPP()  # 虽然testapp已经进程没了但是还是要杀一下，不知道为啥
        packageName = getPackageName(appPath)
        if (packageName == None):
            return False
        return uninstall_app_by_packageName(packageName)


def pushTestFile(appPath_testFile):  # ok
    if (not isADBWorkNormal()):
        print("adb work abnormal!")
        return False
    else:
        push_testFile = "adb push " + appPath_testFile + " " + "/data/data/jacy.popoaichuiniu.com.testpermissionleakge/files/intentInfo.txt"
        status, output, proc = execuateCmdPreventBlock(push_testFile)
        if (status == 0):
            info = appPath_testFile + "推送测试文件成功"
            print(info)
            return True, info
        else:
            info = "推送测试文件失败" + "error:" + status + "," + output + +"eeeeeeeeeeee"
            print(info)
            return False, info


def startTestAPP():  # ok
    if (not isADBWorkNormal()):
        print("adb work abnormal!")
        return False
    start_app_cmd = "adb shell am start -n jacy.popoaichuiniu.com.testpermissionleakge/jacy.popoaichuiniu.com.testpermissionleakge.MainActivity"
    status, output, proc = execuateCmdPreventBlock(start_app_cmd)
    if (status == 0):
        index = output.find("Error")
        if (index != -1):
            print(output)
            return False, "启动APP失败" + output + "eeeeeeeeeeee"
        else:
            return True, "启动APP成功" + output

    else:
        print("启动测试APP失败")
        return False, "启动APP失败" + output + "eeeeeeeeeeee"


def killTestAPP():  # ok
    if (not isADBWorkNormal()):
        print("adb work abnormal!")
        return False;
    kill_app_cmd = "adb shell am force-stop jacy.popoaichuiniu.com.testpermissionleakge"
    status, output, proc = execuateCmdPreventBlock(kill_app_cmd)
    if (status == 0):
        return True
    else:
        print("杀死app失败," + output)
        return False


def analysisAPKDir(apkDir):  #
    if (os.path.isdir(apkDir)):
        failure_log = open(logDir + "/apk_file_no_test_info.txt", "a+")
        for file in os.listdir(apkDir):
            apk_path = apkDir + "/" + file
            if (str(apk_path).endswith("_signed_zipalign.apk")):
                intent_file = apkDir + "/../" + file.replace("_signed_zipalign", "") + "_" + "intentInfo.txt"
                if (os.path.exists(intent_file) and os.path.exists(apk_path)):
                    yield apk_path, intent_file
                else:
                    print(apk_path + "没有找到指定的intent测试文件")
                    failure_log.write(apk_path + "\n")
                    failure_log.flush()

        failure_log.close()


def waitForTestStop():
    count = 0
    flagADB = isADBWorkNormal()
    flagTestAPPLive = isTestAPPAlive()
    while ((not flagADB) or flagTestAPPLive):
        if (not flagADB):
            print("adb工作不正常")
        else:
            print("等待当前APP测试结束！" + str(count))
            count = count + 1
            if (count > 10):
                killTestAPP()
        time.sleep(5)
        flagADB = isADBWorkNormal()
        flagTestAPPLive = isTestAPPAlive()


def rebootPhone():
    cmd = "adb shell reboot -p"
    status, output, proc = execuateCmdPreventBlock(cmd)
    if (status == 0):
        print("emulator has closed")
        threadList = initialLogger(logDir)
        return True
    else:
        print("关闭手机失败," + output)
        return False


class Extra:
    def __init__(self, key, type, value):
        self.key = key
        self.type = type
        self.value = value

    def __eq__(self, o: object) -> bool:
        if (isinstance(o, Extra)):
            if (self.key == o.key and self.type == o.type and self.value == o.value):
                return True
        return False

    def __hash__(self) -> int:
        return 31 * (31 * hash(self.key) + hash(self.type)) + hash(self.value)

    def __str__(self) -> str:
        return self.type + "&" + self.key + "&" + str(self.value)


class Extra_Key:
    def __init__(self, key, type, value):
        self.key = key
        self.type = type
        self.value = value

    def __eq__(self, o: object) -> bool:
        if (isinstance(o, Extra)):
            if (self.key == o.key and self.type == o.type):
                return True
        return False

    def __hash__(self) -> int:
        return 31 * (31 * hash(self.key) + hash(self.type))

    def __str__(self) -> str:
        return self.type + "&" + self.key


class Intent:
    def __init__(self, appPath, appPackageName, comPonentType, comPonentName, action, categorySet, comPonentData,
                 extraSet) -> None:
        self.appPath = appPath
        self.appPackageName = appPackageName
        self.comPonentType = comPonentType
        self.comPonentName = comPonentName
        self.action = action
        self.categorySet = categorySet
        self.comPonentData = comPonentData
        self.extraSet = extraSet

    def __eq__(self, o: object) -> bool:
        if (isinstance(o, Intent)):
            if (self.getBaseStr(self) == self.getBaseStr(o) and self.getActionStr(
                    self) == self.getActionStr(
                o)):
                if (self.getCategorySet(self) == self.getCategorySet(o) and self.getDataStr(
                        self) == self.getDataStr(
                    o)):
                    if (self.getExtraSet(self) == self.getExtraSet(o)):
                        return True

        return False

    def getBaseStr(self, intent):
        line = ''
        if (intent.appPath != None):
            line = line + intent.appPath + "#"
        else:
            line = line + "null" + "#"
        if (intent.appPackageName != None):
            line = line + intent.appPackageName + "#"
        else:
            line = line + "null" + "#"
        if (intent.comPonentType != None):
            line = line + intent.comPonentType + "#"
        else:
            line = line + "null" + "#"
        if (intent.comPonentName != None):
            line = line + intent.comPonentName + "#"
        else:
            line = line + "null" + "#"

        return line

    def getActionStr(self, intent):
        line = ''
        if (intent.action != None):
            line = line + intent.action + "#"
        else:
            line = line + "null" + "#"
        return line

    def getCategorySet(self, intent):
        categorySet = set()
        if (intent.categorySet == None or len(intent.categorySet) == 0):
            categorySet.add("null")
        else:
            for category in intent.categorySet:
                categorySet.add(category)
        return categorySet

    def getDataStr(self, intent):
        line = ''
        if (intent.comPonentData != None):
            line = line + intent.comPonentData + "#"
        else:
            line = line + "null" + "#"
        return line

    def getExtraSet(self, intent):
        extraSet = set()
        if (intent.extraSet == None or len(intent.extraSet) == 0):
            extraSet.add("null")
        else:
            for extra in intent.extraSet:
                extraStr = extra.type + "&" + extra.key + "&" + str(extra.value)
                extraSet.add(extraStr)
        return extraSet

    def __hash__(self) -> int:
        return hash(self.__str__())

    def __str__(self) -> str:
        line = ''
        if (self.appPath != None):
            line = line + self.appPath + "#"
        else:
            line = line + "null" + "#"
        if (self.appPackageName != None):
            line = line + self.appPackageName + "#"
        else:
            line = line + "null" + "#"
        if (self.comPonentType != None):
            line = line + self.comPonentType + "#"
        else:
            line = line + "null" + "#"
        if (self.comPonentName != None):
            line = line + self.comPonentName + "#"
        else:
            line = line + "null" + "#"
        if (self.action != None):
            line = line + self.action + "#"
        else:
            line = line + "null" + "#"
        categoryStr = ''
        if (self.categorySet == None or len(self.categorySet) == 0):
            categoryStr = "null"
        else:
            for category in self.categorySet:
                if (categoryStr == ''):
                    categoryStr = category
                else:
                    categoryStr = categoryStr + "," + category
        line = line + categoryStr + "#"

        if (self.comPonentData != None):
            line = line + self.comPonentData + "#"
        else:
            line = line + "null" + "#"

        extraStr = ''
        if (self.extraSet == None or len(self.extraSet) == 0):
            extraStr = "null"
        else:
            for extra in self.extraSet:
                if (extraStr == ""):
                    extraStr = extra.type + "&" + extra.key + "&" + str(extra.value);
                else:
                    extraStr = extraStr + ";" + extra.type + "&" + extra.key + "&" + str(extra.value);
        line = line + extraStr;
        return line


def getCombinationCategorySet(categoryList, index, selectCategorySet, oneCategorySet):
    if (index >= len(categoryList)):
        selectCategorySet.add(frozenset(oneCategorySet))
        return

    oneCategorySetCopy = set(oneCategorySet)
    getCombinationCategorySet(categoryList, index + 1, selectCategorySet, oneCategorySetCopy)

    category = categoryList[index]
    oneCategorySetCopy = set(oneCategorySet)
    oneCategorySetCopy.add(category)
    getCombinationCategorySet(categoryList, index + 1, selectCategorySet, oneCategorySetCopy)


def addMutatedIntentToExtra(extraSet):
    extraNew = set()
    for extra in extraSet:
        if (extra.type == "int"):
            extraG = Extra(extra.key, extra.type, extra.value)  # +1
            extraG.value = int(extraG.value) + 1
            extraNew.add(extraG)

            extraL = Extra(extra.key, extra.type, extra.value)
            extraL.value = int(extra.value) - 1
            extraNew.add(extraL)
        if (extra.type == "float"):
            extraG = Extra(extra.key, extra.type, extra.value)
            extraG.value = float(extraG.value) + 0.1
            extraNew.add(extraG)

            extraL = Extra(extra.key, extra.type, extra.value)
            extraL.value = float(extra.value) - 0.1
            extraNew.add(extraL)

        # if (extra.type == "java.lang.String"):
        #     if (extra.value != None):
        #         extraG = Extra(extra.key, extra.type, extra.value)
        #         extraG.value = None
        #         extraNew.add(extraG)
        #
        #         if (extra.value != ""):
        #             extraG = Extra(extra.key, extra.type, extra.value)
        #             extraG.value = ""
        #             extraNew.add(extraG)
        #
        #
        #     else:
        #         extraG = Extra(extra.key, extra.type, extra.value)
        #         extraG.value = "zms"
        #         extraNew.add(extraG)
    for extra in extraNew:
        extraSet.add(extra)


def getCombinationExtraSet(extraMap, extraMapKeyList, index, selectExtraSet, oneExtraSet):
    if (index >= len(extraMapKeyList)):
        selectExtraSet.add(frozenset(oneExtraSet))
        return
    extraKey = extraMapKeyList[index]
    extraSet = extraMap.get(extraKey)
    for extra in extraSet:
        oneExtraSetCopy = set(oneExtraSet)
        oneExtraSetCopy.add(extra)
        getCombinationExtraSet(extraMap, extraMapKeyList, index + 1, selectExtraSet, oneExtraSetCopy)


def monitorFeedBack():
    while (not isADBWorkNormal()):
        print("等待adb工作正常")
        time.sleep(1)
    log_getFeedBack = 'guake -e  "adb logcat | grep ZMSGetInfo | tee   ' + logDir + '/ZMSGetInfo.log "'
    thread_getFeedBack = MyThread(log_getFeedBack)
    thread_getFeedBack.start()
    time.sleep(5)
    return thread_getFeedBack


def generateIntent(appPath, appPackageName, comPonentType, comPonentName, actionSet, categorySet, extraSet,
                   selectExtraSet, selectCategorySet, has_tested_intent):
    all_intent_count = 0
    for action in actionSet:
        for oneExtraSet in selectExtraSet:
            for oneCategorySet in selectCategorySet:
                intent = Intent(appPath, appPackageName, comPonentType, comPonentName, action,
                                oneCategorySet, None, oneExtraSet)
                if (intent not in has_tested_intent):
                    all_intent_count = all_intent_count + 1
                    if (all_intent_count > 50):
                        intent_count_exceed = open(logDir + "/" + "intent_count_exceed.txt", "a+")
                        intent_count_exceed.write(
                            appPath + "\n" + getStr(actionSet) + "\n" + getStr(categorySet) + "\n" + getStr(
                                extraSet) + "\n")
                        intent_count_exceed.close()
                        return
                    else:
                        yield intent


app_test_count = 0


def test(test_apkPath, initial_intent_file_path):  # intent_file and instrumented app
    flag_test = False
    global app_test_count
    app_test_count = 0
    app_test_status = open(logDir + "/app_test_status", 'a+')
    print(test_apkPath + "11111111111111111111111111111111111" + "\n")
    app_test_status.write(test_apkPath + "11111111111111111111111111111111111" + "\n")
    waitForTestStop()
    print("开始测试新app")

    initial_intent = open(initial_intent_file_path, 'r')
    content = initial_intent.readlines()
    initial_intent.close()

    for one_line in content:
        infoList = one_line.split("#")
        appPath = infoList[0]
        appPackageName = infoList[1]
        comPonentType = infoList[2]
        comPonentName = infoList[3]

        isFirstIn = True
        actionSet = set()
        actionSet.add(None)  # default
        categorySet = set()
        extraSet = set()
        has_tested_intent = set()

        while (True):

            # thread_getFeedBack = monitorFeedBack()
            if (isFirstIn):
                newInfoFile.write("---------intent------------\n")
                newInfoFile.write(appPath + "\n")
                newInfoFile.write(comPonentType + "\n")
                newInfoFile.write(comPonentName + "\n")
                newInfoFile.write("actionSet:" + getStr(actionSet) + "\n")
                newInfoFile.write("categorySet:" + getStr(categorySet) + "\n")
                newInfoFile.write("extraSet:" + getStr(extraSet) + "\n")
                newInfoFile.write("---------------------\n\n\n")
                newInfoFile.flush()
                isFirstIn = False
                flag = start_one_intent_test(test_apkPath, initial_intent_file_path, app_test_status)
                if (flag):
                    flag_test = True
                intent = Intent(appPath, appPackageName, comPonentType, comPonentName, None,
                                None, None, None)
                has_tested_intent.add(intent)
                newInfoFile.write(intent.__str__() + "\n\n")
                newInfoFile.flush()

            else:

                newInfoFile.write("---------intent------------\n")
                newInfoFile.write(appPath + "\n")
                newInfoFile.write(comPonentType + "\n")
                newInfoFile.write(comPonentName + "\n")
                newInfoFile.write("actionSet:" + getStr(actionSet) + "\n")
                newInfoFile.write("categorySet:" + getStr(categorySet) + "\n")
                newInfoFile.write("extraSet:" + getStr(extraSet) + "\n")
                newInfoFile.write("---------------------\n\n\n")
                newInfoFile.flush()

                extraMap = {}
                for extra in extraSet:
                    if (extra != None):
                        extra_key = Extra_Key(extra.key, extra.type, extra.value)
                        extraSetHasOneKeyType = extraMap.get(extra_key.__str__())
                        if (extraSetHasOneKeyType == None):
                            extraSetHasOneKeyType = set()
                        extraSetHasOneKeyType.add(extra)
                        extraMap[extra_key.__str__()] = extraSetHasOneKeyType

                selectExtraSet = set()
                extraMapKeyList = list(extraMap.keys())
                getCombinationExtraSet(extraMap, extraMapKeyList, 0, selectExtraSet, set())

                selectCategorySet = set()
                categoryList = list(categorySet)
                getCombinationCategorySet(categoryList, 0, selectCategorySet, set())

                newInfoFile.write("----------intent test select-----------\n")
                newInfoFile.write("actionSetUUU:" + str(len(actionSet)) + "\n")
                newInfoFile.write("categorySetUUU:" + str(len(selectCategorySet)) + "\n")
                newInfoFile.write("extraSetUUU:" + str(len(selectExtraSet)) + "\n")
                newInfoFile.write("---------------------\n\n\n")
                newInfoFile.flush()

                for intent in generateIntent(appPath, appPackageName, comPonentType, comPonentName, actionSet,
                                             categorySet, extraSet,
                                             selectExtraSet, selectCategorySet, has_tested_intent):
                    one_intent_file = open(logDir + "/temp_intent", 'w')
                    one_intent_file.write(intent.__str__() + "\n")
                    one_intent_file.close()
                    flag = start_one_intent_test(test_apkPath, logDir + "/temp_intent",
                                                 app_test_status)
                    if (flag):
                        flag_test = True
                    has_tested_intent.add(intent)
                    newInfoFile.write(intent.__str__() + "\n\n")
                    newInfoFile.flush()

            newInfoFile.write("1111111111111111111111111\n")

            flag_add_new_content = analysisNewIntentFileToGetNewIntent(actionSet, categorySet, extraSet)
            newInfoFile.write("2222222222222222222222222\n\n\n")
            newInfoFile.flush()
            if (not flag_add_new_content):
                break

    print(test_apkPath + "222222222222222222222222222222222222222" + "\n")
    app_test_status.write(test_apkPath + "222222222222222222222222222222222222222" + "\n")
    app_test_status.close()

    return flag_test


def getStr(oneSet):
    line = "{"
    for ele in oneSet:
        if (line == "{"):
            line = "{" + str(ele) + "\n"
        else:
            line = line + "#" + str(ele) + "\n"
    line = line + "}"
    return line


def clearLog():
    while (not isADBWorkNormal()):
        print("等待adb工作正常")
        time.sleep(1)
    log_clear = 'adb logcat -c'
    status, output, proc = execuateCmdPreventBlock(log_clear)
    if (status == 0):
        return True
    else:
        return False


def dumpLog():
    while (not isADBWorkNormal()):
        print("等待adb工作正常")
        time.sleep(1)
    log_dump = 'adb logcat -d >>' + logDir + '/ZMSGetInfo.log'
    status, output, proc = execuateCmdPreventBlock(log_dump)
    if (status == 0):
        return True
    else:
        return False


def start_one_intent_test(apkPath, testFile, app_test_status):
    global intent_test_count
    global app_test_count
    flag_test = False
    if app_test_count > 100:
        test_app_intent_count = open(logDir + "/" + 'test_app_intent_count.txt', 'a+')
        test_app_intent_count.write(apkPath + '\n')
        test_app_intent_count.close()
        return flag_test
    start_time = time.time()
    flag_install = installNewAPP(
        "/media/mobile/myExperiment/idea_ApkIntentAnalysis/android_project/Camera/TestPermissionleakge/app/build/outputs/apk/debug/app-debug.apk")
    if (not flag_install):
        print("install error")
        raise RuntimeError
    flag_install, install_info = installNewAPP(apkPath)
    if (flag_install):
        flag_pushTestFile, push_info = pushTestFile(testFile)
        if (flag_pushTestFile):

            flag_clear = clearLog()
            if (not flag_clear):
                print("clearLog failure")
                runLogFile.write("clearLog failure\n")
                return False

            flag_test_APP, test_info = startTestAPP()
            if (flag_test_APP):
                flag_test = True
                print("oneIntent启动成功!" + "\n")
                app_test_status.write("oneIntent启动成功!" + "\n")

            else:
                print("启动TestPermissionAPP失败")
                app_test_status.write(test_info + "\n")

        else:
            print("推送测试文件失败")
            app_test_status.write(push_info + "\n")


    else:
        print("待测apk安装失败")
        app_test_status.write(install_info + "\n")

    waitForTestStop()
    uninstall_app_by_path(apkPath)
    uninstall_app_by_packageName("jacy.popoaichuiniu.com.testpermissionleakge")
    flag_dump = dumpLog()
    if (not flag_dump):
        flag_test = False
        print("dumpLog failure")
        runLogFile.write("dumpLog failure\n")
        return flag_test

    end_time = time.time()
    newInfoFile.write("time:" + str(end_time - start_time) + "\n")
    intent_test_count = intent_test_count + 1
    app_test_count = app_test_count + 1
    if (intent_test_count % 15 == 0):
        if (rebootPhone()):
            while (not isADBWorkNormal()):
                print("等待手机启动！")
                time.sleep(1)
        else:
            app_test_status.write("reboot phone error" + "\n")
            raise RuntimeError
    if (flag_test):
        return True
    else:
        return False


def filterLog(file, message, returnFile):
    filter = 'cat ' + file + " | grep " + message + " > " + returnFile
    status, output, proc = execuateCmdPreventBlock(filter)
    if (status == 0):
        return returnFile
    else:
        return None


def filterLogAppend(file, message, returnFile):
    filter = 'cat ' + file + " | grep " + message + " >>" + returnFile
    status, output, proc = execuateCmdPreventBlock(filter)
    if (status == 0):
        return returnFile
    else:
        return None


def analysisNewIntentFileToGetNewIntent(actionSet, categorySet, extraSet):
    # I / ZMSGetInfo_0_false_java.lang.String(2300): ggg
    # I / ZMSGetInfo_0_true_java.lang.String(2300): 2rrr
    flag_add_new = False
    # waitForGetInfoFile(logDir + '/ZMSGetInfo.log')
    actionNew = set()
    categoryNew = set()
    extraNew = set()
    if (os.path.exists(logDir + '/ZMSGetInfo.log')):

        try:
            once_getInfoFile = open(logDir + '/ZMSGetInfo.log', 'r', errors='ignore')
            info_lines = once_getInfoFile.readlines()
            for info_line in info_lines:
                allInfoFile.write(info_line)
            once_getInfoFile.close()

        except Exception as err:
            runLogFile.write(str(err) + "\n")
            once_getInfoFile.close()
            os.remove(logDir + '/ZMSGetInfo.log')
            if (rebootPhone()):
                while (not isADBWorkNormal()):
                    print("等待手机启动！")
                    time.sleep(1)
            else:
                raise RuntimeError
            return False

        permissionLeak = filterLogAppend(logDir + '/ZMSGetInfo.log', 'ZMSInstrument',
                                         logDir + '/permissionLeakResults.log')

        returnFile = filterLog(logDir + '/ZMSGetInfo.log', 'ZMSGetInfo', logDir + '/once_feedback.log')
        os.remove(logDir + '/ZMSGetInfo.log')
        if (returnFile == None):
            return False
        once_feedBackFile = open(returnFile, 'r')
        lines = once_feedBackFile.readlines()
        once_feedBackFile.close()

        dict = {}  # {key=id value=list[isIf,type,value]}}
        for line in lines:

            start = line.find('ZMSGetInfo')
            end = line.find('(')

            if (start == -1 or end == -1):
                continue
            info = line[start:end]
            info_array = info.split('_')

            if (len(info_array) != 4):
                continue
            start_value = line.find(":")
            end_value = line.find("\n")

            if (start_value == -1 or end_value == -1):
                continue
            value = line[start_value + 2:end_value]

            if (info_array[1] == "action"):
                if value not in actionSet:
                    actionNew.add(value)
                    flag_add_new = True
                continue
            if (info_array[1] == "category"):
                if (value not in categorySet):
                    categoryNew.add(value)
                    flag_add_new = True
                continue
            list = []
            list.append(info_array[2])  # isIf
            list.append(info_array[3])  # 这个是存储的type值，是这个日志后面变量的值的类型，for extra unit log ,the type is alsways string
            list.append(value)
            # dict[info_array[1]]=list
            testSet = dict.get(info_array[1])
            if (testSet == None):
                testSet = set()
            testSet.add(tuple(list))
            dict[info_array[1]] = testSet
        for id, tupleSet in dict.items():  # 相同id的信息 #{key=id value=list[isIf,type,value]}}
            extraValue = []
            extraType = []
            for info_tuple in tupleSet:
                if info_tuple[0] == "true":
                    extraValue.append(info_tuple[2])
                    extraType.append(info_tuple[1])

            extraKey = []
            for info_tuple in tupleSet:
                if info_tuple[0] == "false":
                    extraKey.append(info_tuple[2])

            for indexValue in range(len(extraValue)):
                for indexKey in range(len(extraKey)):
                    newExtra = Extra(extraKey[indexKey], extraType[indexValue], extraValue[indexValue])
                    if newExtra not in extraSet:
                        extraNew.add(newExtra)
                        flag_add_new = True

    newInfoFile.write("action:" + getStr(actionNew) + "\n")
    newInfoFile.write("category:" + getStr(categoryNew) + "\n")
    newInfoFile.write("extra:" + getStr(extraNew) + "\n")

    addMutatedIntentToExtra(extraNew)

    for action in actionNew:
        actionSet.add(action)
    for category in categoryNew:
        categorySet.add(category)
    for extra in extraNew:
        extraSet.add(extra)
    return flag_add_new


class MyThread(threading.Thread):
    def __init__(self, cmd):
        threading.Thread.__init__(self)

        self.cmd = cmd

    def run(self):
        print(self.cmd + "start...")
        proc = subprocess.Popen(self.cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        self.proc = proc
        outs, errs = proc.communicate()
        print(self.cmd + " cmd over@@@@@@@@@@@@@@@@@@@@")
        print("output info: " + str(outs, encoding="utf-8") + "##" + str(errs, encoding="utf-8"))


def setLogSize():
    cmd = 'adb logcat -G 16M'
    status, output, p = execuateCmdPreventBlock(cmd)
    if (status == 0):
        return True
    else:
        return False


def initialLogger(logDir):
    threadList = []
    log1 = 'guake -e  "adb logcat | grep ZMSInstrument | tee -a ' + logDir + '/ZMSInstrument.log "'
    log2 = 'guake -e  "adb logcat | grep ZMSStart | tee -a ' + logDir + '/ZMSStart.log"'
    log3 = 'guake -e  "adb logcat *:E|tee -a ' + logDir + '/error.log"'

    # 创建3个线程

    threadStart = MyThread("/home/lab418/Android/Sdk/emulator/emulator -avd Nexus_5X_API_19 -wipe-data")  # 5.0 can‘t
    threadStart.start()
    threadList.append(threadStart)
    while (
            not isADBWorkNormal()):  # adb devices work  normal and install app inmmediately and install successfully and app is not installed
        print("等待adb...")
        time.sleep(1)

    # setLogSize() it can’t be used under android5.0

    time.sleep(15)
    # thread1 = MyThread(log1)
    # thread2 = MyThread(log2)
    # thread3 = MyThread(log3)
    # thread1.start()
    # thread2.start()
    # thread3.start()
    # threadList.append(thread1)
    # threadList.append(thread2)
    # threadList.append(thread3)
    return threadList


def getFileContent(path):
    content = []
    if (os.path.exists(path)):
        str_file = open(path, 'r')
        content = []
        for line in str_file.readlines():
            content.append(line.rstrip('\n'))
    return content


def killProcessTree(pid):
    try:
        rootProc = psutil.Process(pid)
        print("childProcess:" + str(rootProc.pid) + "  " + str(rootProc.gids()))
        procs = rootProc.children()
        for proc in procs:
            try:
                if proc.is_running():  # one process died may cause other processes died

                    if (len(proc.children()) == 0):
                        cmd = "kill -9 " + str(proc.pid)
                        status, output, p = execuateCmdPreventBlock(cmd)
                        if (status == 0):
                            print("exe over  " + cmd)
                        else:
                            print("exe fail  " + cmd)

                    else:
                        killProcessTree(proc.pid)
            except psutil.NoSuchProcess:  # one Proc
                runLogFile.write("NoSuchProcess" + "\n")

        if (rootProc.is_running()):
            cmd = "kill -9 " + str(rootProc.pid)
            status, output, p = execuateCmdPreventBlock(cmd)
            if (status == 0):
                print("exe over  " + cmd)
            else:
                print("exe fail  " + cmd)

    except psutil.NoSuchProcess:  # rootProc
        runLogFile.write("NoSuchProcess" + "\n")


intent_test_count = 0
if __name__ == '__main__':

    # print(isADBWorkNormal())
    # print(isTestAPPAlive())
    # print(installNewAPP("/home/lab418/PycharmProjects/testAPP/sms2.apk"))
    # print(getPackageName("/home/lab418/PycharmProjects/testAPP/sms2.apk"))
    # print(uninstall_app("/home/lab418/PycharmProjects/testAPP/sms2.apk"))
    # print(pushTestFile("/home/lab418/PycharmProjects/testAPP/intentInfo1.txt"))
    # print(startTestAPP())
    # print(killTestAPP())

    # for apk,intent_file in analysisAPKDir("."):
    #     print(apk+","+intent_file)

    # test("sms2.apk","intentInfo1.txt")

    # initialLogger()
    # rebootPhone()
    # initialLogger()
    # startTestAPP()

    # killTestAPP()
    # print(pushTestFile("/media/lab418/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/idea_ApkIntentAnalysis/android_project/Camera/TestWebView2/app/build/outputs/apk/debug/instrumented/app-debug_signed_zipalign.ap"))
    # print(uninstall_app("/media/lab418/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/idea_ApkIntentAnalysis/android_project/Camera/TestWebView2/app/build/outputs/apk/debug/instrumented/app-debug_signed_zipalign.apk"))

    # test************************************************************before

    apkDir = ''
    logDir = ''
    print(sys.argv)
    if (len(sys.argv) <= 2):  # 给定参数不对时，使用默认参数
        # apkDir = '/media/mobile/myExperiment/apps/apks_wandoujia/apks/all_app/instrumented'
        apkDir = '/media/mobile/myExperiment/idea_ApkIntentAnalysis/android_project/Camera/TestWebView2/app/build/outputs/apk/debug/app-debug.apk'
        apkDir = '/media/mobile/myExperiment/apps/apks_wandoujia/apks/all_app'
        logDir = '/home/zms/logger_file/DynamicSE/testLog'
    else:
        apkDir = sys.argv[1]
        logDir = sys.argv[2]
    if (not os.path.exists(logDir)):
        os.makedirs(logDir)
    baseName = os.path.basename(apkDir)
    runLogFile = open(logDir + "/" + baseName + "_runException.log", 'a+')
    newInfoFile = open(logDir + "/" + baseName + "_feedbackInfo.log", 'a+')
    allInfoFile = open(logDir + "/" + baseName + "_allInfo.log", 'a+')
    threadList = []
    threadList = initialLogger(logDir)
    while (not isADBWorkNormal()):
        print("等待adb工作正常")
        time.sleep(1)
    if (not os.path.isdir(apkDir)):  # is apk
        parent_path = os.path.dirname(apkDir)
        apk_name = os.path.basename(apkDir)
        intent_file = parent_path + "/" + apk_name + "_" + "intentInfo.txt"
        apkDir = parent_path + "/" + "instrumented" + "/" + apk_name.replace(".apk", "_signed_zipalign.apk")
        if (os.path.exists(intent_file) and os.path.exists(apkDir)):
            start_time = time.time()
            flag_test = test(apkDir, intent_file)
            if (flag_test):
                end_time = time.time()
            else:
                print(apkDir + "测试失败！")

        else:
            print("没有找到指定的intent测试文件或者instrumented app")

    else:
        fail_apk_list = open(logDir + "/failTest_apk_list", "a+")
        has_process = getFileContent(logDir + "/has_process_app_list")
        has_process_app_list = open(logDir + "/has_process_app_list", "a+")
        timeUse = open(logDir + "/timeUse.txt", "a+")

        apkDir = apkDir + "/" + 'instrumented'
        for apkPath, intent_file in analysisAPKDir(apkDir):
            if apkPath in has_process:
                continue
            has_process_app_list.write(apkPath + "\n")
            has_process_app_list.flush()
            start_time = time.time()
            flag_test = test(apkPath, intent_file)
            if (flag_test):
                end_time = time.time()
                timeUse.write(str(end_time - start_time) + "," + apkPath + "\n")
                timeUse.flush()
            else:
                print(apkPath + "测试失败！")
                fail_apk_list.write(apkPath + "\n")
                fail_apk_list.flush()

        timeUse.close()
        fail_apk_list.close()
        has_process_app_list.close()
    print("curProcess:" + str(os.getpid()) + " " + str(os.getgid()))
    for oneThread in threadList:
        killProcessTree(oneThread.proc.pid)
    print("over")
    runLogFile.close()
    newInfoFile.close()
    sys.exit(0)
