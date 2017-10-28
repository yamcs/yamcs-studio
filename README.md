Below instructions are targeted at Yamcs Studio core contributors.

* **End-User documentation** is available at: [http://www.yamcs.org/docs/studio/](http://www.yamcs.org/docs/studio/)
* Extension development is not currently documented.

---

### Prerequisites
* Oracle JDK 8
* Maven


### Headless Build

There are two different maven reactors (the first bundles up non-OSGI dependencies):

```
mvn -f p2deps/pom.xml clean install
mvn clean install
```

### Eclipse Development

Yamcs Studio is developed via 'Eclipse for RCP and RAP developers'. The advantage over the headless build is that Eclipse can launch snapshot copies of Yamcs Studio in seconds instead of minutes. Follow these steps to prepare your Eclipse development environment:

- Ensure you successfully ran the headless build first, because this will generate artefacts needed in the Eclipse build.

- Go to `Preferences > Maven > Errors/Warnings`, select `Warning` or `Ignore` (you choose) for the setting `Plugin execution not covered by lifecycle configuration`.

- Under `Preferences > Maven > Discovery`, click on the `Open Catalog` button and install the Tycho Configurator from there. This plugin will add support for Tycho artefacts to the integrated Maven build cycle.

- Import the Yamcs Studio maven projects into the workspace by right clicking in the navigator and choosing `Import > Maven > Existing Maven Projects`. Follow the on-screen instructions.

- Repeat the previous step by importing the projects found directly under the `p2deps` subfolder. These are detached from the main maven build, and need to be imported separately.

- Open the `*.platform` file found in the project `org.yamcs.studio.platform`. Click the upper right link `Set as target platform`. This operation may take a while, as it will attempt to resolve and download third-party dependencies.

- All error signs should now be resolved. If not, try right-clicking the yamcs-studio project and choose `Maven > Update Project Configuration`.

- Open `yamcs-studio.product` found in the project `org.yamcs.studio.dist.default.repository`.

- Click `Launch an Eclipse Application`.


### CI Status

[![Build Status](https://travis-ci.org/yamcs/yamcs-studio.svg?branch=master)](https://travis-ci.org/yamcs/yamcs-studio)
