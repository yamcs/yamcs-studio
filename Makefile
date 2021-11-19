.DEFAULT_GOAL := build

.PHONY: build-deps
build-deps:
	mvn -f p2deps/pom.xml clean install -Dtycho.localArtifacts=ignore

.PHONY: build
build: build-deps
	mvn clean install -DskipTests -Dtycho.localArtifacts=ignore

.PHONY: set-version
set-version:
	test ${VERSION} \
	&& mvn versions:set -DnewVersion="${VERSION}" versions:commit \
	&& mvn tycho-versions:update-eclipse-metadata \
	&& sed -e "1,/version/s/<version>.*<\/version>/<version>${VERSION}<\/version>/" p2deps/pom.xml > p2deps/pom.xml.bak \
	&& mv p2deps/pom.xml.bak p2deps/pom.xml

