#!/bin/bash

mvn -f ./yamcs-studio-bundles/pom.xml clean verify
mvn -f ./yamcs-studio-tycho/pom.xml clean verify
