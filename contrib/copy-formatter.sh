#!/bin/bash

# Just copies the formatter-profile to new projects
# This leaves existing files unchanged because of possible inconsistencies
# (eclipse sometimes adds or remove content in these files)
# The good thing is that Eclipse detects changes in one prefs file automatically
# and applies it to the others (based on same formatter name)
DIR=$( cd "$( dirname "$0" )/.." && pwd )

# Find folders in upper dir that contain a .settings/
for path in $DIR/*; do
	[ -d "${path}" ] || continue
	if [ -d $path/.settings ]; then
		cp -n $DIR/misc/org.eclipse.jdt.core.prefs $path/.settings
		cp -n $DIR/misc/org.eclipse.jdt.ui.prefs $path/.settings
		
		# Also bypass .gitignore so that we don't forget this for new projects
		# Only in staging area, don't want to interfere too much..
		git add -f $path/.settings/org.eclipse.jdt.core.prefs
		git add -f $path/.settings/org.eclipse.jdt.ui.prefs
	fi
done

