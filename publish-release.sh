#!/bin/sh
set -e

VERSION=`sed -n '/<product/s/.*version="\([^"]*\)".*/\1/p' yamcs-studio-tycho/org.yamcs.studio.dist.default.repository/yamcs-studio.product`

if [ -z $VERSION ]; then
    echo "Could not determine version number"
    exit 1
fi

echo Checking release conditions....
TARGET_PRODUCT_DIR="/opt/studio/releases/v$VERSION"
TARGET_REPO_DIR="/opt/studio/updates/v$VERSION"

if [[ `ssh root@aces-ci test -d $TARGET_PRODUCT_DIR && echo exists` ]]; then
    echo "Found remote directory $TARGET_PRODUCT_DIR"
    echo "Was version '$VERSION' already released?"
    exit 1
fi

if [[ `ssh root@aces-ci test -d $TARGET_REPO_DIR && echo exists` ]]; then
    echo "Found remote directory $TARGET_REPO_DIR"
    echo "Was version '$VERSION' already released?"
    exit 1
fi

echo Uploading products....
PRODUCTS=yamcs-studio-tycho/org.yamcs.studio.dist.default.repository/target/products
ssh root@aces-ci "mkdir $TARGET_PRODUCT_DIR"
scp -r $PRODUCTS/*.gz root@aces-ci:$TARGET_PRODUCT_DIR
scp -r $PRODUCTS/*.zip root@aces-ci:$TARGET_PRODUCT_DIR

echo Uploading update-site....
REPO=yamcs-studio-tycho/org.yamcs.studio.dist.default.repository/target/repository
ssh root@aces-ci "mkdir $TARGET_REPO_DIR"
scp -r $REPO/* root@aces-ci:$TARGET_REPO_DIR

