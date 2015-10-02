#!/bin/sh
set -e

if [ -z "$1" ]; then
    echo "New version not specified"
    echo "Usage: ./prepare-release.sh version"
    exit 1
fi

echo "Updating poms and manifests"
mvn -o -f yamcs-studio-osgi/pom.xml org.eclipse.tycho:tycho-versions-plugin:0.23.0:set-version -DnewVersion=$1

mvn -o -f yamcs-studio-tycho/pom.xml org.eclipse.tycho:tycho-versions-plugin:0.23.0:set-version -DnewVersion=$1

echo "Updated version numbers to $1"
read -p 'Rebuild binaries? [Y/n]' yn
case $yn in
    [Nn]* )
        exit 0
    * )
        sh make-product.sh
        ;;
esac

read -p "Publish release $1? [Y/n]" yn
case $yn in
    [Nn]* )
        exit 0
    * )
        sh publish-release.sh
        ;;
esac
