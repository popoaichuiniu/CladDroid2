import os
import commands


def findSmaliFile(path):
    for fileName in os.listdir(path):
        # print fileName
        if (os.path.isdir(path + "/" + fileName)):
            for item in findSmaliFile(path + "/" + fileName):
                yield item
        else:
            if (fileName.endswith(".smali")):
                yield path + "/" + fileName


def getApkSmaliLines():
    apk_smali_lines = open('apk_smali_result.csv', 'w')
    apk_smali_lines.write("apk,apk_size,android_count,allCount_without_android,allCount\n")
    path = "."
    for apk_apktool_dir in os.listdir(path):
        if (os.path.isdir(path + "/" + apk_apktool_dir) and os.path.exists(
                path + "/" + apk_apktool_dir + ".apk")):  # apk apktool dir
            allCount = 0
            allCount_without_android = 0
            android_count = 0
            for content in os.listdir(path + "/" + apk_apktool_dir):
                if (os.path.isdir(path + "/" + apk_apktool_dir + "/" + content) and content == "smali"):
                    for smaliFile in findSmaliFile(path + "/" + apk_apktool_dir + "/" + content):
                        print smaliFile
                        smaliFileRead = open(smaliFile, 'r')
                        lines = smaliFileRead.readlines()
                        smaliFileRead.close()
                        allCount = allCount + len(lines)
                        if (not smaliFile.startswith(path + "/" + apk_apktool_dir + "/" + content + "/" + "android")):
                            android_count = android_count + len(lines)
                        else:
                            allCount_without_android = allCount_without_android + len(lines)
            apk_size = os.path.getsize(path + "/" + apk_apktool_dir + ".apk")
            apk_size = float(apk_size) / 1024 / 1024
            apk_smali_lines.write(apk_apktool_dir + "," + str(apk_size) + "," + str(android_count) + "," + str(
                allCount_without_android) + "," + str(allCount) + "\n")
            apk_smali_lines.flush()
    apk_smali_lines.close()


def reverseAPK(position):
    apk_reverse_failed = open("apk_reverse_failed", "w")
    for item in findApk(position,False):
        print "11111111111111111111111111111111111111111"
        print item[0:-4]
        print item
        if os.path.exists(item[0:-4]):
            print "333333333333333333333333333333333333333333"
            print item + " have reversed!"
        else:
            print "22222222222222222222222222222222222222222222"
            cmd = "apktool d " + item + " -o " + item[0:-4]
            status, output = commands.getstatusoutput(cmd)
            if (status == 0):
                print item + " is reversed successful !"
            else:
                apk_reverse_failed.write(item + "\n")
    apk_reverse_failed.close()


def findApk(path,isRecursive):  # INPUT DIR
    if(not isRecursive):
        for fileName in os.listdir(path):
            if(fileName.endswith(".apk")):
                yield path + "/" + fileName
    else:
        for fileName in os.listdir(path):
            # print fileName
            if (os.path.isdir(path + "/" + fileName)):
                for item in findApk(path + "/" + fileName):
                    yield item
            else:
                if (fileName.endswith(".apk")):
                    # print apk_count
                    # print path+"/"+fileName
                    yield path + "/" + fileName

reverseAPK(".")
getApkSmaliLines()
