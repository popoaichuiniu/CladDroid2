# -*- coding: utf-8 -*-
import os
import commands

try:
    import xml.etree.cElementTree as ET
except ImportError:
    import xml.etree.ElementTree as ET

import sys
reload(sys)  
sys.setdefaultencoding('utf8')

class code_analysis(object):
    def __init__(self):
        self.apk_count=0
        self.smali_count=0


    def findApk(self,path):#INPUT DIR   
        for fileName in os.listdir(path):
            #print fileName
            if(os.path.isdir(path+"/"+fileName)):
                for item in self.findApk(path+ "/"+fileName):
                    yield item
            else:
                if (fileName.endswith(".apk")):
                    self.apk_count=self.apk_count+1
                    #print apk_count
                    #print path+"/"+fileName
                    yield path+"/"+fileName

    def findSmali(self,path):
        for fileName in os.listdir(path):
            #print fileName
            if(os.path.isdir(path+"/"+fileName)):
                for item in self.findSmali(path+"/"+fileName):
                    yield item
            else:
                if (fileName.endswith(".smali")): 
                    self.smali_count=self.smali_count+1               
                    #print path+"/"+fileName
                    yield path+"/"+fileName


    
    def findCodeInSmali(self,position,code_find,result_file_name):
        result_file=open(result_file_name,"w")
        apk_reverse_failed=open("apk_reverse_failed","w")
        for item in self.findApk(position):
            print "11111111111111111111111111111111111111111"
            print item[0:-4]
            print item
            if os.path.exists(item[0:-4]):
                print "333333333333333333333333333333333333333333"
                for  smali in self.findSmali(item[0:-4]):
                        print smali
                        code_smali=open(smali,"r")
                        lines=code_smali.readlines()
                        tempstr=""
                        for line in lines:
                            tempstr=tempstr+line
                        #print tempstr
                        index=tempstr.find(code_find)
                        if index!=-1:
                            result_file.write(item+"\n")
                            break
            else:
                print "22222222222222222222222222222222222222222222"
                cmd="apktool d "+item+" -o "+item[0:-4]
                status,output=commands.getstatusoutput(cmd)
                if(status==0):
                    for  smali in self.findSmali(item[0:-4]):
                        print smali
                        code_smali=open(smali,"r")
                        lines=code_smali.readlines()
                        tempstr=""
                        for line in lines:
                            tempstr=tempstr+line
                        #print tempstr
                        index=tempstr.find(code_find)
                        if index!=-1:
                            result_file.write(item+"\n")
                            break
                else:
                    apk_reverse_failed.write(item+"\n")           
                
        result_file.close()
        apk_reverse_failed.close()
    def parseAndroidMainifestActivities(self,filePath):
        apk_reverse_failed = open("apk_reverse_failed", "w")
        if(os.path.exists(filePath)):
            try:
                tree=ET.parse(filePath)
                if (tree !=None):###########################解析错误的xml会怎么样？
                    root=tree.getroot()

                    android_tag="{http://schemas.android.com/apk/res/android}"

                    for item in root.findall('.//activity'):
                        #print item.attrib
                        print item.get(android_tag+"name")
                        yield item.get(android_tag+"name")
                else:
                    print "AndroidManifest parse fail!!!"

            except ET.ParseError:
                apk_android_manifest_error = open("apk_android_manifest_error", "a")
                apk_android_manifest_error.write(filePath+ "\n")
                apk_android_manifest_error.close()

        else :
            apk_reverse_failed = open("apk_reverse_failed", "a")
            apk_reverse_failed.write(filePath+"******************androidmanifest文件不存在"+"\n")
            apk_reverse_failed.close()






    def findFile(self,location_to_find,fileName):
               
        for child_file in os.listdir(location_to_find):
            if(os.path.isdir(location_to_find+"/"+child_file)):
                flag=self.findFile(location_to_find+"/"+child_file,fileName)
                if(flag==True):
                    return True
            else:
                print child_file
                if(fileName==child_file):
                    return True
        return False
    def reverseAPK(self,position):
        apk_reverse_failed=open("apk_reverse_failed","w")
        for item in self.findApk(position):
            print "11111111111111111111111111111111111111111"
            print item[0:-4]
            print item
            if os.path.exists(item[0:-4]):
                print "333333333333333333333333333333333333333333"
                print item+" have reversed!"
            else:
                print "22222222222222222222222222222222222222222222"
                cmd="apktool d "+item+" -o "+item[0:-4]
                status,output=commands.getstatusoutput(cmd)
                if(status==0):
                    print item+" is reversed successful !"
                else:
                    apk_reverse_failed.write(item+"\n")
        apk_reverse_failed.close()
    def findAPK_Harden(self,path):
        apk_harden=open("apk_harden","w")
        activity_fail=open("activity_fail","w")
        self.reverseAPK(path)        
        for item in self.findApk(path):
            print item
            activity_count=0
            lose_activity=0
            print "******************************************************************************"
            for activity in self.parseAndroidMainifestActivities(item[0:-4]+"/"+"AndroidManifest.xml"):
                print activity
                if(activity!=None):
                    activity_count=activity_count+1
                    acti_str=activity.split(".")
                    find_ac_str=acti_str[len(acti_str)-1]
                    print find_ac_str
                    if(self.findFile(item[0:-4],find_ac_str+".smali")==False):
                        lose_activity=lose_activity+1
                else:
                    activity_no_name = open("activity_no_name", "a")
                    activity_no_name.write(item+"\n")
                    activity_no_name.close()
            if(activity_count==0):
                activity_fail.write(item+"\n")
            else:
                if(float(lose_activity)/float(activity_count)>0.7):
                    apk_harden.write(item+"\n")
            print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
            

        apk_harden.close()

            

            







def main():
    ca=code_analysis()   
    
    #ca.reverseAPK("/media/jacy/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/丽人母婴")
    ca.findAPK_Harden(".")

    

                


    

if __name__ == '__main__':
    main()





    #test
    # for item in findApk("."):
    # 	print item
    # print apk_count

    #test

    # for item in findSmali("."):
    #     print item
    # print smali_count
