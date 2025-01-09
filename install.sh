#!/bin/bash

echo ""
echo "=========================================="
echo "lcarsde installation"
echo "=========================================="
echo ""
echo "This program requires:"
echo "* JDK >= 8"
echo "* libx11-dev"
echo "* libxpm-dev"
echo "* libgtk-3-dev"
echo "* libxrandr-dev"
echo "* libpango1.0-dev"
echo "* libxml2-dev"
echo "* libtinfo6"
echo "* libx11-6"
echo "* libxpm4"
echo "* libxrandr2"
echo "* libgtk-3-0,"
echo "* libpango1.0-0"
echo "* libpangoxft-1.0-0"
echo "* libxml2"
echo "* dex"
echo "* libxcrypt-compat (Arch)"
echo ""
echo "Recommended:"
echo "* fonts-ubuntu"
echo "* alsa-utils"
echo "* pulseaudio-utils"
echo ""

./gradlew clean
./gradlew build
./gradlew installDist
./gradlew combineRelease

cp -r "./build/release/usr/*" "/usr/"
cp -r "./build/release/etc/*" "/etc/"

mkdir -p "/usr/share/doc/lcarsde"
cp "./LICENSE" "/usr/share/doc/lcarsde/copyright"
