#!/bin/sh
apkName=$1
echo $apkName
signedAPKName=${apkName%.*}"_signed.apk"
echo $signedAPKName
zlignAPKName=${signedAPKName%.*}"_zipalign.apk"
jdk1.8.0_161/bin/jarsigner -verbose -keystore .android/debug.keystore -signedjar $signedAPKName  $apkName androiddebugkey -digestalg SHA1 -sigalg MD5withRSA -keypass android -storepass android
android-sdk/build-tools/28.0.3/zipalign -f -v 4 $signedAPKName $zlignAPKName
android-sdk/build-tools/28.0.3/zipalign -c -v 4 $zlignAPKName
rm $signedAPKName
