#!/bin/bash

mvn -f ./yamcs-studio-bundles/pom.xml clean verify
mvn -f ./yamcs-studio/pom.xml clean verify
