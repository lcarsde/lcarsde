#!/bin/bash

export LC_ALL=en_US.UTF-8
TIME="$(date '+%a, %d %b %Y %T %z')"
YEAR="$(date '+%Y')"
NAME=$1
TAG=$2

echo "preparing debian package build for ${NAME}-${TAG}"

# create debian package construction directory
mkdir -p "build/deb/${NAME}-${TAG}"

# copy software resources
cp -r gradle "build/deb/${NAME}-${TAG}/gradle"
cp -r app-selector "build/deb/${NAME}-${TAG}/app-selector"
cp -r lcarsde-common "build/deb/${NAME}-${TAG}/lcarsde-common"
cp -r lcarsde-gtk "build/deb/${NAME}-${TAG}/lcarsde-gtk"
cp -r lcarswm "build/deb/${NAME}-${TAG}/lcarswm"
cp -r logout "build/deb/${NAME}-${TAG}/logout"
cp -r menu "build/deb/${NAME}-${TAG}/menu"
cp -r status-bar "build/deb/${NAME}-${TAG}/status-bar"

cp build.gradle.kts "build/deb/${NAME}-${TAG}/"
cp CHANGELOG "build/deb/${NAME}-${TAG}/"
cp gradle.properties "build/deb/${NAME}-${TAG}/"
cp gradlew "build/deb/${NAME}-${TAG}/"
cp LICENSE "build/deb/${NAME}-${TAG}/"
cp README.md "build/deb/${NAME}-${TAG}/"
cp settings.gradle.kts "build/deb/${NAME}-${TAG}/"

cd "build/deb"

echo "create tarball"
tar -czf "${NAME}_${TAG}.orig.tar.gz" "${NAME}-${TAG}"
tar -ztf "${NAME}_${TAG}.orig.tar.gz"

cd ../..

# copy debian packaging files
cp -r "debian" "build/deb/${NAME}-${TAG}/debian"

echo "building debian package of ${NAME}-${TAG}"

cd "build/deb/${NAME}-${TAG}"

sed -i "s/%version%/${TAG}/" "debian/changelog"
sed -i "s/%time%/${TIME}/" "debian/changelog"
sed -i "s/%year%/${YEAR}/g" "debian/copyright"

debuild -us -uc

cd ..
ls -l
mkdir "deploy"
mv "${NAME}_${TAG}_amd64.deb" deploy/

cd ../..

echo "deb and tar.gz files are in build/deb/deploy"