# Yamcs Studio

This repository contains the source files for Yamcs Studio as well as the documentation sources for [docs.yamcs.org/yamcs-studio](https://docs.yamcs.org/yamcs-studio/).


## Binary Releases

https://github.com/yamcs/yamcs-studio/releases/

**Note to macOS users:** Binaries are not signed, nor notarized. Bypass Gatekeeper checks using:
```
sudo xattr -rds com.apple.quarantine ~/Downloads/Yamcs\ Studio.app
```


## Building from Source

See [development instructions](docs/development.md).


## Acknowledgments

The OPI display format originates from [Control System Studio](https://github.com/ControlSystemStudio/cs-studio). The source files in this repository include a modified fork of `org.csstudio.opibuilder` and related bundles. The main differences are:

* Custom datasource binding, adapted for integration with Yamcs.
* Trimming of unneeded views, menus and perspectives.
* Use of Runner/Builder window switch.
