import subprocess
import time
import threading
import psutil
class Test:
    platform_tools_dir = '../android-sdk/platform-tools'
    build_tools_dir = '../android-sdk/build-tools/28.0.3'
    test_app = '../android_project/Camera/TestPermissionleakge/app/build/outputs/apk/debug/app-debug.apk'
    emulator_dir = '../android-sdk/tools'
    ANDROID_AVD_HOME = ".." + "/" + ".android/avd"
    ANDROID_SDK_ROOT = ".." + "/" + "android-sdk"
    ANDROID_SDK_HOME = ".." + "/" + ".android"

    @classmethod
    def execuateCmd(cls,cmd):
        # status,output=subprocess.getstatusoutput(cmd);
        proc = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        outs, errs = proc.communicate()
        return proc.returncode, str(outs, encoding="utf-8") + "##" + str(errs, encoding="utf-8"), proc

    @classmethod
    def execuateCmdPreventBlock(cls,cmd):
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

    @classmethod
    def isADBWorkNormal(cls):  # ok
        adb_status = Test.platform_tools_dir+"/"+"adb devices"
        status, output, proc = Test.execuateCmdPreventBlock(adb_status)
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

    @classmethod
    def isTestAPPAlive(cls):  # ok
        if (not Test.isADBWorkNormal()):
            print("adb work abnormal!")
            return False;
        else:
            app_status = Test.platform_tools_dir+"/"+"adb shell ps |grep jacy.popoaichuiniu.com.testpermissionleakge"
            # app_status = "adb shell ps |grep com.android.settings"
            status, output, proc = Test.execuateCmdPreventBlock(app_status)
            if (output.find("jacy.popoaichuiniu.com.testpermissionleakge") != -1):
                print(output)
                return True
            else:
                return False

    @classmethod
    def installNewAPP(cls,appPath):  # ok
        if (not Test.isADBWorkNormal()):
            print("adb work abnormal!")
            return False;
        else:
            install_app = Test.platform_tools_dir+"/"+"adb install -r" + " " + appPath
            status, output, proc = Test.execuateCmdPreventBlock(install_app)
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

    @classmethod
    def getPackageName(cls,appPath):  #
        get_package_cmd = Test.build_tools_dir+"/"+"aapt dump badging " + appPath
        status, output, proc = Test.execuateCmdPreventBlock(get_package_cmd)
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

    @classmethod
    def uninstall_app_by_packageName(cls,packageName):
        if (not Test.isADBWorkNormal()):
            print("adb work abnormal!")
            return False
        else:
            install_app = Test.platform_tools_dir+"/"+"adb uninstall " + " " + packageName
            status, output, proc = Test.execuateCmdPreventBlock(install_app)
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

    @classmethod
    def uninstall_app_by_path(cls,appPath):  # ok
        if (not Test.isADBWorkNormal()):
            print("adb work abnormal!")
            return False
        else:
            Test.killTestAPP()  # 虽然testapp已经进程没了但是还是要杀一下，不知道为啥
            packageName = Test.getPackageName(appPath)
            if (packageName == None):
                return False
            return Test.uninstall_app_by_packageName(packageName)

    @classmethod
    def pushTestFile(cls,appPath_testFile):  # ok
        if (not Test.isADBWorkNormal()):
            print("adb work abnormal!")
            return False
        else:
            push_testFile = Test.platform_tools_dir+"/"+"adb push " + appPath_testFile + " " + "/data/data/jacy.popoaichuiniu.com.testpermissionleakge/files/intentInfo.txt"
            status, output, proc = Test.execuateCmdPreventBlock(push_testFile)
            if (status == 0):
                info = appPath_testFile + "推送测试文件成功"
                print(info)
                return True, info
            else:
                info = "推送测试文件失败" + "error:" + status + "," + output + +"eeeeeeeeeeee"
                print(info)
                return False, info

    @classmethod
    def startTestAPP(cls):  # ok
        if (not Test.isADBWorkNormal()):
            print("adb work abnormal!")
            return False
        start_app_cmd = Test.platform_tools_dir+"/"+"adb shell am start -n jacy.popoaichuiniu.com.testpermissionleakge/jacy.popoaichuiniu.com.testpermissionleakge.MainActivity"
        status, output, proc = Test.execuateCmdPreventBlock(start_app_cmd)
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

    @classmethod
    def killTestAPP(cls):  # ok
        if (not Test.isADBWorkNormal()):
            print("adb work abnormal!")
            return False;
        kill_app_cmd = Test.platform_tools_dir+"/"+"adb shell am force-stop jacy.popoaichuiniu.com.testpermissionleakge"
        status, output, proc = Test.execuateCmdPreventBlock(kill_app_cmd)
        if (status == 0):
            return True
        else:
            print("杀死app失败," + output)
            return False
    @classmethod
    def waitForTestStop(cls):
        count = 0
        flagADB = Test.isADBWorkNormal()
        flagTestAPPLive = Test.isTestAPPAlive()
        while ((not flagADB) or flagTestAPPLive):
            if (not flagADB):
                print("adb工作不正常")
            else:
                print("等待当前APP测试结束！" + str(count))
                count = count + 1
                if (count > 10):
                    Test.killTestAPP()
            time.sleep(2)
            flagADB = Test.isADBWorkNormal()
            flagTestAPPLive = Test.isTestAPPAlive()

    @classmethod
    def killProcessTree(cls,pid):
        try:
            rootProc = psutil.Process(pid)
            print("childProcess:" + str(rootProc.pid) + "  " + str(rootProc.gids()))
            procs = rootProc.children()
            for proc in procs:
                try:
                    if proc.is_running():  # one process died may cause other processes died

                        if (len(proc.children()) == 0):
                            cmd = "kill -9 " + str(proc.pid)
                            status, output, p = Test.execuateCmdPreventBlock(cmd)
                            if (status == 0):
                                print("exe over  " + cmd)
                            else:
                                print("exe fail  " + cmd)

                        else:
                            Test.killProcessTree(proc.pid)
                except psutil.NoSuchProcess:  # one Proc
                    pass

            if (rootProc.is_running()):
                cmd = "kill -9 " + str(rootProc.pid)
                status, output, p = Test.execuateCmdPreventBlock(cmd)
                if (status == 0):
                    print("exe over  " + cmd)
                else:
                    print("exe fail  " + cmd)

        except psutil.NoSuchProcess:  # rootProc
            pass

    @classmethod
    def rebootPhone(cls,initialLogger,logDir):
        cmd = Test.platform_tools_dir+"/"+"adb shell reboot -p"
        status, output, proc = Test.execuateCmdPreventBlock(cmd)
        if (status == 0):
            print("emulator has closed")
            threadList = initialLogger(logDir)
            return threadList
        else:
            print("关闭手机失败," + output)
            return None

    @classmethod
    def setToolPosition(cls,platform_tools_dir,build_tools_dir,test_app,emulator_dir,ANDROID_AVD_HOME,ANDROID_SDK_ROOT,ANDROID_SDK_HOME):
        Test.platform_tools_dir = platform_tools_dir
        Test.build_tools_dir = build_tools_dir
        Test.test_app = test_app
        Test.emulator_dir = emulator_dir
        Test.ANDROID_AVD_HOME = ANDROID_AVD_HOME
        Test.ANDROID_SDK_ROOT = ANDROID_SDK_ROOT
        Test.ANDROID_SDK_HOME=ANDROID_SDK_HOME


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