.DEFAULT_GOAL := build

.PHONY: build-deps
build-deps:
	mvn -f p2deps/pom.xml clean install

.PHONY: build
build: build-deps
	mvn clean install -DskipTests

.PHONY: set-version
set-version:
	@read -p "Enter the new version to set: " VERSION \
	&& mvn versions:set -DnewVersion="$$VERSION" versions:commit \
	&& mvn tycho-versions:update-eclipse-metadata \
	&& sed -i '' "1,/version/s/<version>.*<\/version>/<version>$$VERSION<\/version>/" p2deps/pom.xml

