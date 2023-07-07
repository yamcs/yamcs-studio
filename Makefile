.PHONY: build
build:
	mvn clean install

.PHONY: clean
clean:
	mvn clean

.PHONY: set-version
set-version:
	mvn tycho-versions:set-version -DnewVersion="${VERSION}"
