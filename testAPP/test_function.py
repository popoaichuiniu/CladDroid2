import subprocess
import time
import threading
import psutil
import os
import GlobalData
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
        time.sleep(2)
        flagADB = isADBWorkNormal()
        flagTestAPPLive = isTestAPPAlive()


def rebootPhone():
    cmd = "adb shell reboot -p"
    status, output, proc = execuateCmdPreventBlock(cmd)
    if (status == 0):
        print("emulator has closed")
        threadList = initialLogger(GlobalData.logDir)
        return True
    else:
        print("关闭手机失败," + output)
        return False
def monitorFeedBack():
    while (not isADBWorkNormal()):
        print("等待adb工作正常")
        time.sleep(1)
    log_getFeedBack = 'guake -e  "adb logcat | grep ZMSGetInfo | tee   ' + GlobalData.logDir + '/ZMSGetInfo.log "'
    thread_getFeedBack = MyThread(log_getFeedBack)
    thread_getFeedBack.start()
    time.sleep(5)
    return thread_getFeedBack
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
                GlobalData.runLogFile.write("NoSuchProcess" + "\n")

        if (rootProc.is_running()):
            cmd = "kill -9 " + str(rootProc.pid)
            status, output, p = execuateCmdPreventBlock(cmd)
            if (status == 0):
                print("exe over  " + cmd)
            else:
                print("exe fail  " + cmd)

    except psutil.NoSuchProcess:  # rootProc
        GlobalData.runLogFile.write("NoSuchProcess" + "\n")