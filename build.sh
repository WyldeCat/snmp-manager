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
rm -rf src/com/wyldecat/snmpclient/R.java
rm -rf classes.dex

if [ "$1" == "clean" ]; then
	exit 0;	
fi

echo "Generating R.java file..."
$AAPT package -f -m -J src -M AndroidManifest.xml -S res -I $PLATFORM

echo "Compiling..."
javac -d obj -classpath src -bootclasspath $PLATFORM -source 1.7 -target 1.7 src/com/wyldecat/snmpclient/MainActivity.java
javac -d obj -classpath src -bootclasspath $PLATFORM -source 1.7 -target 1.7 src/com/wyldecat/snmpclient/R.java

echo "Translating in Dalvik bytecode..."
$DX --dex --output=classes.dex obj

echo "Making APK..."
$AAPT package -f -m -F bin/snmpclient.unaligned.apk -M AndroidManifest.xml -S res -I $PLATFORM
$AAPT add bin/snmpclient.unaligned.apk classes.dex

echo "Aligning and signing APK..."
$ZIPALIGN -f 4 bin/snmpclient.unaligned.apk bin/snmpclient.apk
$APKSIGNER sign --ks key.keystore bin/snmpclient.apk

if [ "$1" == "test" ]; then
	echo "Launching..."
	adb install -r bin/snmpclient.apk
	adb shell am start -n com.wyldecat.snmpclient/.MainActivity
fi

