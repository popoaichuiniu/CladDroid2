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


def isADBWorkNormal():  # ok
    adb_status = "adb devices"
    status, output, proc = execuateCmd(adb_status)
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
        status, output, proc = execuateCmd(app_status)
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
        status, output, proc = execuateCmd(install_app)
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
    status, output, proc = execuateCmd(get_package_cmd)
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
        status, output, proc = execuateCmd(install_app)
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
        status, output, proc = execuateCmd(push_testFile)
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
    status, output, proc = execuateCmd(start_app_cmd)
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
    status, output, proc = execuateCmd(kill_app_cmd)
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
    status, output, proc = execuateCmd("killall guake")
    if (status == 0):
        print("killall guake ok!")
        cmd = "adb shell reboot -p"
        status, output, proc = execuateCmd(cmd)
        if (status == 0):
            print("emulator has closed")
            threadList = initialLogger(logDir)
            return True
        else:
            print("关闭手机失败," + output)
            return False
    else:
        print("killall guake fail!")
        return False


intent_test_count = 0


def getNewContent(test_feedback):
    feedback = open(test_feedback, 'r')


class Extra:
    def __init__(self, key, type, value):
        self.key = key
        self.type = type
        self.value = value

    def __init__(self, extra):
        self.key = extra.key
        self.value = extra.value
        self.type = extra.type

    def __eq__(self, o: object) -> bool:
        if (isinstance(o, Extra)):
            if (self.key == o.key and self.type == o.type and self.value == o.value):
                return True
        return False

    def __hash__(self) -> int:
        return 31 * (31 * hash(self.key) + hash(self.type)) + hash(self.value)


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
            # o=Intent(o)
            if (self.appPath == o.appPath and self.comPonentType
                    == o.comPonentType and self.comPonentName == o.comPonentName and self.action == o.action and self.categorySet ==
                    o.categorySet and self.comPonentData == o.comPonentData and self.extraSet == o.extraSet):
                return True
        return False

    def __hash__(self) -> int:
        return 31 * ((31 * (31 * (31 * (31 * hash(self.action) + hash(self.categorySet)) + hash(self.extraSet)) + hash(
            self.comPonentData)) + hash(self.appPath)) + hash(self.comPonentType)) + hash(self.comPonentName)

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
                    extraStr = extra.type + "&" + extra.key + "&" + extra.value;
                else:
                    extraStr = extraStr + ";" + extra.type + "&" + extra.key + "&" + extra.value;
        line = line + extraStr;
        return line


def getCombinationCategorySet(categoryList, index, selectCategorySet, oneCategorySet):
    if (index >= len(categoryList)):
        selectCategorySet.add(oneCategorySet)
    oneCategorySet = set()

    getCombinationCategorySet(categoryList, index + 1, selectCategorySet, oneCategorySet)

    category = categoryList[index]
    oneCategorySet.add(category)
    getCombinationCategorySet(categoryList, index + 1, selectCategorySet, oneCategorySet)


def getCombinationExtraSet(extraList, index, selectExtraSet, oneExtraSet):
    if (index >= len(extraList)):  #
        selectExtraSet.add(oneExtraSet)
    oneExtraSet = set()
    extra = extraList[index]
    if (extra.type == "int"):
        oneExtraSet.add(extra)
        getCombinationExtraSet(extraList, index + 1, selectExtraSet, oneExtraSet)

        extraG = Extra(extra)
        extraG.value = int(extraG.value) + 1
        oneExtraSet.add(extraG)
        getCombinationExtraSet(extraList, index + 1, selectExtraSet, oneExtraSet)

        extraL = Extra(extra)
        extraL.value = int(extra.value) - 1
        oneExtraSet.add(extraL)
        getCombinationExtraSet(extraList, index + 1, selectExtraSet, oneExtraSet)
    else:
        if extra.type == "float":
            oneExtraSet.add(extra)
            getCombinationExtraSet(extraList, index + 1, selectExtraSet, oneExtraSet)

            extraG = Extra(extra)
            extraG.value = float(extraG.value) + 0.1
            oneExtraSet.add(extraG)
            getCombinationExtraSet(extraList, index + 1, selectExtraSet, oneExtraSet)

            extraL = Extra(extra)
            extraL.value = float(extra.value) - 0.1
            oneExtraSet.add(extraL)
            getCombinationExtraSet(extraList, index + 1, selectExtraSet, oneExtraSet)
        else:
            if (extra.type != "java.lang.String"):
                fail_unHandle = open(logDir + "/" + "fail_unHandle.txt", 'a+')
                fail_unHandle.write(extra.type + "can't handle" + "\n")
                fail_unHandle.close()
            getCombinationExtraSet(extraList, index + 1, selectExtraSet, oneExtraSet)
            oneExtraSet.add(extra)

            getCombinationExtraSet(extraList, index + 1, selectExtraSet, oneExtraSet)


def monitorFeedBack():
    while (not isADBWorkNormal()):
        print("等待adb工作正常")
        time.sleep(1)
    log_getFeedBack = 'guake -e  "adb logcat | grep ZMSGetInfo | tee  ' + logDir + '/ZMSGetInfo.log "'
    thread_getFeedBack = MyThread(log_getFeedBack)
    thread_getFeedBack.start()
    time.sleep(3)
    return thread_getFeedBack


def test(apkPath, initial_intent_file_path):  # intent_file and instrumented app
    global intent_test_count

    app_test_status = open(logDir + "/app_test_status", 'a+')
    print(apkPath + "11111111111111111111111111111111111" + "\n")
    app_test_status.write(apkPath + "11111111111111111111111111111111111" + "\n")
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
        categorySet = set()
        extraSet = set()
        while (True):
            thread_getFeedBack = monitorFeedBack()
            if (isFirstIn):
                isFirstIn = False
                flag_test = start_one_intent_test(apkPath, initial_intent_file_path, app_test_status)

            else:
                selectExtraSet = set()
                extraList = list(extraSet)
                getCombinationExtraSet(extraList, 0, selectExtraSet, set())

                selectCategorySet = set()
                categoryList = list(categorySet)
                getCombinationCategorySet(categoryList, 0, selectCategorySet, set())

                for action in actionSet:
                    for oneExtraSet in selectExtraSet:
                        for oneCategorySet in selectCategorySet:
                            intent = Intent(appPath, appPackageName, comPonentType, comPonentName, action,
                                            oneCategorySet, None, oneExtraSet)
                            one_intent_file = open(logDir + "/temp_intent", 'w')
                            one_intent_file.write(intent.__str__() + "\n")
                            one_intent_file.close()
                            flag_test = start_one_intent_test(apkPath, logDir + "/temp_intent", app_test_status)

            killProcessTree(thread_getFeedBack.proc.pid)
            flag_add_new_content = analysisNewIntentFileToGetNewIntent(actionSet, categorySet, extraSet,app_test_status)
            if (not flag_add_new_content):
                break

    print(apkPath + "222222222222222222222222222222222222222" + "\n")
    app_test_status.write(apkPath + "222222222222222222222222222222222222222" + "\n")
    app_test_status.close()

    return flag_test


def start_one_intent_test(apkPath, testFile, app_test_status):
    global intent_test_count

    flag_test = False
    flag_install = installNewAPP(
        "/media/mobile/myExperiment/idea_ApkIntentAnalysis/android_project/Camera/TestPermissionleakge/app/build/outputs/apk/debug/app-debug.apk")
    if (not flag_install):
        print("install error")
        raise RuntimeError
    flag_install, install_info = installNewAPP(apkPath)
    if (flag_install):
        flag_pushTestFile, push_info = pushTestFile(testFile)
        if (flag_pushTestFile):
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

    intent_test_count = intent_test_count + 1
    if (intent_test_count % 20 == 0):
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


def analysisNewIntentFileToGetNewIntent(actionSet, categorySet, extraSet,app_test_status):
    # I / ZMSGetInfo_0_false_java.lang.String(2300): ggg
    # I / ZMSGetInfo_0_true_java.lang.String(2300): 2rrr
    flag_add_new=False
    feedBackFile = open(logDir + '/ZMSGetInfo.log', 'r')
    lines = feedBackFile.readlines()
    dict={}#{key=id value=list[isIf,type,value]}}
    for line in lines:
        start=line.index('ZMSGetInfo')
        end=line.index('(')

        if(start==-1 or end==-1):
            continue
        info=line[start,end]
        info_array=info.split('_')

        if(len(info_array)!=4):
            continue
        start_value=line.index(":")
        end_value=line.index("\n")

        if(start_value==-1 or end_value==-1):
            continue
        value=line[start_value+2,end_value]

        if(info_array[1]=="action"):
            actionSet.add(value)
            flag_add_new = True
            continue
        if(info_array[1]=="category"):
            categorySet.add(value)
            flag_add_new = True
            continue
        list=[]
        list[0]=info_array[2]
        list[1]=info_array[3]#这个是存储的type值，是这个日志后面变量的值的类型，for extra unit log ,the type is alsways string
        list[2]=value
        # dict[info_array[1]]=list
        testSet=dict.get(info_array[1])
        if(testSet==None):
            testSet=set()
        testSet.add(list)
        dict[info_array[1]]=testSet
    for id,listSet in dict.items():#相同id的信息 #{key=id value=list[isIf,type,value]}}
        extraValue=''
        extraType=''
        if_count=0
        for list in listSet:
            if list[0]=="true":
                extraValue=list[2]
                extraType=list[1]
                if_count=if_count+1
        if(if_count>1 or if_count==0):
            app_test_status.write("if_count is not correct! "+str(if_count)+" $$$"+str(listSet)+"$$$\n")

        for list in listSet:
            if list[0]=="false":
                extraKey=list[2]
                extraSet.add(Extra(extraKey,extraType,extraValue))
                flag_add_new = True
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


def initialLogger(logDir):
    threadList = []
    log1 = 'guake -e  "adb logcat | grep ZMSInstrument | tee -a ' + logDir + '/ZMSInstrument.log "'
    log2 = 'guake -e  "adb logcat | grep ZMSStart | tee -a ' + logDir + '/ZMSStart.log"'
    log3 = 'guake -e  "adb logcat *:E|tee -a ' + logDir + '/error.log"'

    # 创建3个线程

    threadStart = MyThread("/home/lab418/Android/Sdk/emulator/emulator -avd Nexus_5X_API_19 -wipe-data")
    threadStart.start()
    threadList.append(threadStart)
    while (
            not isADBWorkNormal()):  # adb devices work  normal and install app inmmediately and install successfully and app is not installed
        print("等待adb...")
        time.sleep(1)
    # flag_install = installNewAPP("/media/lab418/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/idea_ApkIntentAnalysis/android_project/Camera/TestPermissionleakge/app/build/outputs/apk/debug/app-debug.apk")
    # if (not flag_install):
    #     print("install error")
    thread1 = MyThread(log1)
    thread2 = MyThread(log2)
    thread3 = MyThread(log3)
    time.sleep(10)
    thread1.start()
    thread2.start()
    thread3.start()
    threadList.append(thread1)
    threadList.append(thread2)
    threadList.append(thread3)
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
    rootProc = psutil.Process(pid)
    print("childProcess:" + str(rootProc.pid) + "  " + str(rootProc.gids()))
    procs = rootProc.children()
    for proc in procs:
        if (len(proc.children()) == 0):
            cmd = "kill -9 " + str(proc.pid)
            status, output, p = execuateCmd(cmd)
            print("exe over  " + cmd)
        else:
            killProcessTree(proc.pid)

    cmd = "kill -9 " + str(rootProc.pid)
    status, output, p = execuateCmd(cmd)
    print("exe over  " + cmd)


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
        # apkDir='/home/lab418/Documents'
        logDir = '/home/zms/logger_file/DynamicSE/testLog'
    else:
        apkDir = sys.argv[1]
        logDir = sys.argv[2]
    if (not os.path.exists(logDir)):
        os.makedirs(logDir)
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
        success_apk_list = open(logDir + "/successTest_apk_list", "a+")
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
                timeUse.write(str(end_time - start_time) + "\n")
                timeUse.flush()
                success_apk_list.write(apkPath + "\n")
                success_apk_list.flush()
            else:
                print(apkPath + "测试失败！")
                fail_apk_list.write(apkPath + "\n")
                fail_apk_list.flush()

        timeUse.close()
        success_apk_list.close()
        fail_apk_list.close()
        has_process_app_list.close()
# execuateCmd("prctl(PR_SET_PDEATHSIG, SIGHUP)")
print("curProcess:" + str(os.getpid()) + " " + str(os.getgid()))
for oneThread in threadList:
    killProcessTree(oneThread.proc.pid)
print("over")
sys.exit(0)
