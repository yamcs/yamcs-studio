Yamcs Studio
============

A version of CS-Studio packaged and configured to suit [Yamcs -- the open source Mission Control System](https://github.com/yamcs/yamcs).

The build is split into two steps due to the use of some POM-first artifacts. To build everything do:

  ```shell
  ./buildall.sh
  ```

This will first build the yamcs-studio-bundles reactor (deploying artifacts to the configured target platform), and will then (in a separate maven reactor) build the manifest-first projects under yamcs-studio-tycho.
