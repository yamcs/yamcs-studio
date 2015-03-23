#!/bin/bash

# Builds CSS from the sources using a local p2 repository as described
# in their documentation.

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

# Generate settings.xml (to be reused in Eclipse)
LOCAL_P2_REPO=$PRGDIR/css/local_p2_repository
mkdir -p $LOCAL_P2_REPO
LOCAL_P2_REPO_FULL=`cd "$LOCAL_P2_REPO"; pwd`
sed "s#REPLACE_WITH_LOCAL_P2_REPO#"$LOCAL_P2_REPO_FULL"#" $PRGDIR/css/settings_template.xml >$PRGDIR/css/settings.xml

# Verify that the css-for-yamcs maven profile actually exists
# We unfortunately need this external file. To make it even
# worse, this needs to have an absolute path in it. Blame maven.

# This command is sometimes a bit slow. So give some indication
echo "Searching for css-for-yamcs maven profile...."
if ! mvn help:all-profiles | grep css-for-yamcs >/dev/null
then
	# The user could have some other profiles in there. Don't just overwrite it
	if [ -f ~/.m2/settings.xml ]
	then
		echo 'Could not find css-for-yamcs maven profile. But ~/.m2/settings.xml'
		echo 'already exists. Merge its content with this snippet, then try again:'
		echo
		cat $PRGDIR/css/settings.xml
		exit 1
	else
		echo 'Could not find css-for-yamcs maven profile.'
		read -p 'Generate one and copy it to ~/.m2/settings.xml now? [y/N]' yn
		case $yn in
			[Yy]* )
				cp $PRGDIR/css/settings.xml ~/.m2/settings.xml
				;;
			* )
				echo "Cannot continue without the css-for-yamcs maven profile"
				exit 1
		esac
	fi
fi

if [ -f $LOCAL_P2_REPO/artifacts.jar ]
then
	echo "Existing Target Platform detected."
	read -p "Do you want to delete it, before continuing? [y/N]" yn
	case $yn in
		[Yy]* )
			 rm -Rf $LOCAL_P2_REPO
			 ;;
		* )
			echo "Will update $LOCAL_P2_REPO in-place"
	esac
fi

set -e
mvn -f $PRGDIR/css/maven-osgi-bundles -s $PRGDIR/css/settings.xml -Pcss-for-yamcs clean verify
mvn -f $PRGDIR/css/cs-studio-thirdparty -s $PRGDIR/css/settings.xml -Pcss-for-yamcs clean verify
mvn -f $PRGDIR/css/cs-studio/core -s $PRGDIR/css/settings.xml -Pcss-for-yamcs clean verify
mvn -f $PRGDIR/css/cs-studio/applications -s $PRGDIR/css/settings.xml -Pcss-for-yamcs clean verify
set +e

echo 
echo '----------------------------------------------------------------'
echo 'CS-Studio dependencies successfully built.'
echo '----------------------------------------------------------------'

