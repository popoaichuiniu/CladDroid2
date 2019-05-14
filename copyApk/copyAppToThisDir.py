import  subprocess
def execuateCmd(cmd):
    status,output=subprocess.getstatusoutput(cmd)
    return status,output
appDir='/media/mobile/myExperiment/apps/apks_wandoujia/apks/all_app'
app_list='successTest_apk_list'
list_file=open(app_list,'r')
lines=list_file.readlines()
fail_list=open('fail_list','w')
for line in lines:
    apk=line.rstrip("\n")
    cpCmd="cp "+apk+" ."
    status, output=execuateCmd(cpCmd)
    if(status!=0):
        print(apk+" failure "+output)
        fail_list.write(apk+" failure "+output+"\n")
    else:
        print(apk+" success ")
list_file.close()
fail_list.close()

