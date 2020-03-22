.PHONY: build
build:
	mvn clean install

.PHONY: clean
clean:
	mvn clean

.PHONY: set-version
set-version:
	test ${VERSION} \
	&& mvn versions:set -DnewVersion="${VERSION}" versions:commit \
	&& mvn tycho-versions:update-eclipse-metadata
