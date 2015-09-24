#!/bin/sh

# Set available memory for java in maven builds
export MAVEN_OPTS="-Xmx1024M -Xss128M -XX:+CMSClassUnloadingEnabled"

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`
if [ ! -f $PRGDIR/css/local_p2_repository/artifacts.jar ]
then
	echo "Could not locate Target Platform"
	echo "Did you run ./make-platform.sh already?"
	exit 1
fi

set -e
mvn -f $PRGDIR/yamcs-studio-osgi/pom.xml clean verify
mvn -f $PRGDIR/yamcs-studio-tycho/pom.xml clean verify
set +e

echo
echo 'All done. These are your generated Yamcs Studio products:'
find `cd $PRGDIR; pwd`/yamcs-studio-tycho/org.yamcs.studio.dist.default.repository/target/products -maxdepth 1 -type f -exec echo "{}" \;

