#!/bin/bash

set -e

AAPT="$ANDROID_SDK/build-tools/27.0.3/aapt"
DX="$ANDROID_SDK/build-tools/27.0.3/dx"
ZIPALIGN="$ANDROID_SDK/build-tools/27.0.3/zipalign"
APKSIGNER="$ANDROID_SDK/build-tools/27.0.3/apksigner"
PLATFORM="$ANDROID_SDK/platforms/android-27/android.jar"

echo "Cleaning..."
rm -rf obj/*
rm -rf bin/*
rm -rf src/com/wyldecat/snmpmanager/R.java
rm -rf classes.dex

if [ "$1" == "clean" ]; then
	exit 0;	
fi

echo "Generating R.java file..."
$AAPT package -f -m -J src -M AndroidManifest.xml -S res -I $PLATFORM

echo "Compiling..."
javac -d obj -classpath src -bootclasspath $PLATFORM -source 1.7 -target 1.7 src/com/wyldecat/snmpmanager/MainActivity.java
javac -d obj -classpath src -bootclasspath $PLATFORM -source 1.7 -target 1.7 src/com/wyldecat/snmpmanager/R.java

echo "Translating in Dalvik bytecode..."
$DX --dex --output=classes.dex obj

echo "Making APK..."
$AAPT package -f -m -F bin/snmpmanager.unaligned.apk -M AndroidManifest.xml -S res -I $PLATFORM
$AAPT add bin/snmpmanager.unaligned.apk classes.dex

echo "Aligning and signing APK..."
$ZIPALIGN -f 4 bin/snmpmanager.unaligned.apk bin/snmpmanager.apk
$APKSIGNER sign --ks key.keystore bin/snmpmanager.apk

if [ "$1" == "test" ]; then
	echo "Launching..."
	adb install -r bin/snmpmanager.apk
	adb shell am start -n com.wyldecat.snmpmanager/.MainActivity
fi

