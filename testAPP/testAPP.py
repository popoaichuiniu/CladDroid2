import os
import sys
from APPTestFunction import *


def analysisAPKDir(apkDir):  #
    if (os.path.isdir(apkDir)):
        failure_log = open(logDir + "/apk_file_no_test_info.txt", "a+")
        for file in os.listdir(apkDir):
            apk_path = apkDir + "/" + file
            if (str(apk_path).endswith("_signed_zipalign.apk")):
                intent_file = apkDir + "/../" + file.replace("_signed_zipalign", "") + "_" + "intentInfoSE.txt"
                if (os.path.exists(intent_file) and os.path.exists(apk_path)):
                    yield apk_path, intent_file
                else:
                    print(apk_path + "没有找到指定的intent测试文件")
                    failure_log.write(apk_path + "\n")
                    failure_log.flush()

        failure_log.close()


def flushLog():
    while (not Test.isADBWorkNormal()):
        print("等待adb工作正常")
        time.sleep(1)
    log_dump = platform_tools_dir+"/"+'adb logcat -d '
    status, output, proc = Test.execuateCmdPreventBlock(log_dump)
    if (status == 0):
        return True
    else:
        return False
def rebootPhoneAndKillGuake(threadList):
    flushLog()
    for oneThread in threadList:
        if('emulator'not in oneThread.cmd):
            Test.killProcessTree(oneThread.proc.pid)
    if(Test.rebootPhone(initialLogger,logDir) !=None):
        return True
    else:
        return False







def test(apkPath, intent_file):  # intent_file and instrumented app
    global intent_test_count
    flag_test = False;
    app_test_status = open(logDir + "/app_test_status", 'a+')
    print(apkPath + "11111111111111111111111111111111111" + "\n")
    app_test_status.write(apkPath + "11111111111111111111111111111111111" + "\n")
    Test.waitForTestStop()
    print("开始测试新app")
    intent = open(intent_file, "r")
    lines_intent = intent.readlines()
    intent.close()
    statistic_intent_count = open(logDir + "/intent_count", 'a+')
    statistic_intent_count.write(apkPath + "\n")
    statistic_intent_count.write(str(len(lines_intent)) + "\n")
    statistic_intent_count.close()
    if (len(lines_intent) > 400):
        too_many_test_app = open(logDir + "/too_many_test_app", 'a+')
        too_many_test_app.write(apkPath + "\n")
        too_many_test_app.close()
    index = 0
    for line in lines_intent:
        if (index > 100):
            continue
        print("ssssssssssssssssss" + line)
        oneIntentFile = open(logDir + "/temp_intent", "w")
        oneIntentFile.write(line)
        oneIntentFile.close()
        flag_install = Test.installNewAPP(test_app)
        if (not flag_install):
            print("install error")
            raise RuntimeError
        flag_install, install_info = Test.installNewAPP(apkPath)
        if (flag_install):
            flag_pushTestFile, push_info = Test.pushTestFile(logDir+"/temp_intent")
            if (flag_pushTestFile):
                flag_test_APP, test_info = Test.startTestAPP()
                if (flag_test_APP):
                    flag_test = True
                    print(str(index) + ":oneIntent启动成功!" + "\n")
                    app_test_status.write(str(index) + ":oneIntent启动成功!" + "\n")

                else:
                    print(str(index) + ":启动TestPermissionAPP失败")
                    app_test_status.write(str(index) + test_info + "\n")

            else:
                print(str(index) + ":推送测试文件失败")
                app_test_status.write(str(index) + push_info + "\n")


        else:
            print(str(index) + ":待测apk安装失败")
            app_test_status.write(str(index) + install_info + "\n")

        Test.waitForTestStop()
        Test.uninstall_app_by_path(apkPath)
        Test.uninstall_app_by_packageName("jacy.popoaichuiniu.com.testpermissionleakge")
        index = index + 1
        intent_test_count = intent_test_count + 1
        if (intent_test_count % 15 == 0):
            if (rebootPhoneAndKillGuake(threadList)):
                while (not Test.isADBWorkNormal()):
                    print("等待手机启动！")
                    time.sleep(1)
            else:
                app_test_status.write("reboot phone error" + "\n")
                raise RuntimeError

    print(apkPath + "222222222222222222222222222222222222222" + "\n")
    app_test_status.write(apkPath + "222222222222222222222222222222222222222" + "\n")
    app_test_status.close()

    return flag_test





def initialLogger(logDir):
    threadList = []
    log1 = 'guake -e  "'+"$PWD"+'/'+platform_tools_dir+"/"+'adb logcat | grep ZMSInstrument | tee -a ' + "$PWD"+'/'+logDir + '/ZMSInstrument.log"'
    log2 = 'guake -e  "'+"$PWD"+'/'+platform_tools_dir+"/"+'adb logcat | grep ZMSStart | tee -a ' +"$PWD"+'/'+logDir + '/ZMSStart.log"'
    log3 = 'guake -e  "'+"$PWD"+'/'+platform_tools_dir+"/"+'adb logcat *:E|tee -a ' + "$PWD"+'/'+logDir + '/error.log"'

    # 创建3个线程

    exportANDROID_AVD_HOME='export ANDROID_AVD_HOME='+ANDROID_AVD_HOME
    exportANDROID_SDK_ROOT='export ANDROID_SDK_ROOT='+ANDROID_SDK_ROOT
    exportANDROID_SDK_HOME='export ANDROID_SDK_HOME='+ANDROID_SDK_HOME

    printANDROID_AVD_HOME='echo $ANDROID_AVD_HOME'
    #emulatorCMD=emulator_dir+"/"+"emulator -avd "+avd_name+" -sysdir "+ANDROID_AVD_HOME+" -wipe-data"
    emulatorCMD = emulator_dir + "/" + "emulator -avd " + avd_name  + " -wipe-data"
    #threadStart = MyThread(exportANDROID_SDK_ROOT+" && "+exportANDROID_SDK_HOME+" && "+exportANDROID_AVD_HOME+" && "+printANDROID_AVD_HOME+" && "+emulatorCMD)
    threadStart = MyThread(emulatorCMD)
    threadStart.start()
    threadList.append(threadStart)
    while (
            not Test.isADBWorkNormal()):  # adb devices work  normal and install app inmmediately and install successfully and app is not installed
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







if __name__ == '__main__':

    intent_test_count = 0
    apkDir = ''
    logDir = ''
    instrumented_dir_name=''
    print(sys.argv)
    platform_tools_dir='../android-sdk/platform-tools'
    build_tools_dir='../android-sdk/build-tools/28.0.3'
    test_app='../android_project/Camera/TestPermissionleakge/app/build/outputs/apk/debug/app-debug.apk'
    emulator_dir='../android-sdk/tools'
    ANDROID_AVD_HOME=".."+"/"+".android/avd"
    ANDROID_SDK_ROOT=".."+"/"+"android-sdk"
    ANDROID_SDK_HOME=".."+"/"+".android"
    avd_name='Nexus_5X_API_19'

    Test.setToolPosition(platform_tools_dir,build_tools_dir,test_app,emulator_dir,ANDROID_AVD_HOME,ANDROID_SDK_ROOT,ANDROID_SDK_HOME)

    if (len(sys.argv) <= 3):#给定参数不对时，使用默认参数
        apkDir='../../apps/apks_wandoujia/apks/all_app'
        apkDir='../../apps/91锁屏.apk'
        logDir = '../logger_file/testLog'
        instrumented_dir_name='instrumented_SE'

    else:
        apkDir = sys.argv[1]
        logDir = sys.argv[2]
        instrumented_dir_name=sys.argv[3]

    if (not os.path.exists(logDir)):
        os.makedirs(logDir)


    threadList = []
    threadList = initialLogger(logDir)

    while (not Test.isADBWorkNormal()):
        print("等待adb工作正常")
        time.sleep(1)
    if(os.path.exists(apkDir)):
        if (not os.path.isdir(apkDir)):  # is apk
            print("apk:" + apkDir)
            parent_path = os.path.dirname(apkDir)
            print("parent_dir:"+parent_path)
            intent_file=''
            apk_name = os.path.basename(apkDir)
            if(parent_path==""):
                intent_file = apk_name + "_" + "intentInfoSE.txt"
                apkDir = instrumented_dir_name + "/" + apk_name.replace(".apk","_signed_zipalign.apk")
            else:
                intent_file = parent_path + "/" + apk_name + "_" + "intentInfoSE.txt"
                apkDir = parent_path + "/" + instrumented_dir_name + "/" + apk_name.replace(".apk","_signed_zipalign.apk")
            print("intent_file:"+intent_file)
            print("signed_zipalign:" + apkDir)
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

            apkDir = apkDir + "/" + instrumented_dir_name
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
flushLog()
print("curProcess:" + str(os.getpid()) + " " + str(os.getgid()))
for oneThread in threadList:
    Test.killProcessTree(oneThread.proc.pid)
print("over")
sys.exit(0)
