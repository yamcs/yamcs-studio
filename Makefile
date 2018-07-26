.DEFAULT_GOAL := build

ifeq ($(PREFIX),)
	PREFIX := /opt/yamcs-studio
endif

.PHONY: build-deps
build-deps:
	mvn -f p2deps/pom.xml clean install

.PHONY: build
build: build-deps
	mvn clean install -DskipTests

.PHONY: install
install:
	install -d $(DESTDIR)$(PREFIX)
	tar -xzf releng/org.yamcs.studio.editor.product/target/products/yamcs-studio-*-linux.gtk.x86_64.tar.gz --strip-components=1 --directory $(DESTDIR)$(PREFIX)

.PHONY: set-version
set-version:
	test ${VERSION} \
	&& mvn versions:set -DnewVersion="${VERSION}" versions:commit \
	&& mvn tycho-versions:update-eclipse-metadata \
	&& sed -e "1,/version/s/<version>.*<\/version>/<version>${VERSION}<\/version>/" p2deps/pom.xml > p2deps/pom.xml.bak \
	&& mv p2deps/pom.xml.bak p2deps/pom.xml

