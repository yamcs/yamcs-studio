# Development

This document describes the process for building this application on your local computer.

Yamcs Studio is an Eclipse RCP application and uses [Eclipse Tycho](https://www.eclipse.org/tycho/) for its build workflow.


## Prerequisites

You must have 64-bit OpenJDK 17 or above, Apache Maven 3.5.0 or above, and Git installed on your machine.

Further your machine needs access to Internet, at least when building the first time. Your local Maven cache (`~/.m2`) will be primed with build and runtime dependencies.


## Headless Build

```
mvn clean package -Dtycho.localArtifacts=ignore
```

The `-Dtycho.localArtifacts=ignore` flag avoids unexpected caching issues when bundles have been installed to Maven Cache.

Alternativily, you can avoid manual installation of maven by using the `mvnw` wrapper script. This will automatically download an appropriate version of Maven:
```
./mvnw clean package -Dtycho.localArtifacts=ignore
```


## Eclipse RCP Concepts

The file [platform.target](../platform.target) describes the **platform** that is used by Yamcs Studio. The platform specifies the list of binary p2 bundles (features & plugins) that are *available* during build. Platform content may or may not be used. The platform is hosted on Yamcs infrastructure (https://dl.yamcs.org). Essentially this is a self-managed mirror of various upstream Eclipse p2 repositories (e.g. Orbit), as well as the Yamcs Client dependency that is used throughout this project.

The Yamcs Studio **product** is specified by its [product file](../org.yamcs.studio.editor.product/yamcs-studio.product). The product file is feature-based, therefore the referenced features are what eventually ends up in the application.

**Feature** projects have a file `feature.xml` that describe their plugin dependencies. These plugins must be either defined in this repository, or else located in the platform. Features may also include other features.

**Plugin** projects have a file `plugin.xml` that describes the capabilities of that plugin (for example: toolbars, menus, views).

When the product (Yamcs Studio) runs, all included plugins contribute additions to the RCP Workbench UI.


## Development inside Eclipse

Yamcs Studio is developed via 'Eclipse for RCP and RAP developers'. The advantage over the headless build is that Eclipse can launch snapshot copies of Yamcs Studio in seconds instead of minutes. Follow these steps to prepare your Eclipse development environment:

- Ensure you successfully ran the headless build first, because this will generate artefacts needed in the Eclipse build.

- Go to `Preferences > Maven > Errors/Warnings`, select `Warning` or `Ignore` (you choose) for the setting `Plugin execution not covered by lifecycle configuration`.

- Under `Preferences > Maven > Discovery`, click on the `Open Catalog` button and install the Tycho Configurator from there. This plugin will add support for Tycho artefacts to the integrated Maven build cycle.

- Import the Yamcs Studio maven projects into the workspace by right clicking in the navigator and choosing `Import > Maven > Existing Maven Projects`. Follow the on-screen instructions.

- Open the `platform.target` file found in repository root. Click the upper right link `Set as target platform`. This operation may take a while, as it will attempt to resolve and download third-party dependencies.

- All error signs should now be resolved. If not, try right-clicking the yamcs-studio project and choose `Maven > Update Project Configuration`.

- Open the `org.yamcs.studio.editor.product/yamcs-studio.product` file.

- Click `Synchronize` followed by `Launch an Eclipse Application`.



## Customizing Yamcs Studio

It is possible to make a custom variant of Yamcs Studio without needing to build this project, but you will need to have good understanding of Eclipse RCP. Some hints:

* Whenever a release is done, binary versions of the plugins and features of that release are published publicly to a p2 site. For example: https://dl.yamcs.org/p2/studio/1.6.1/ contains bundles of Yamcs Studio v1.6.1.
* You can include these bundles in your custom Eclipse product.
* The plugin `org.yamcs.studio.editor.base` provides the basic framework that sets up the application. You can use or extend those classes to craft your own product.


## Troubleshooting

### Let's Encrypt certificates

This should not normally occur, but if Maven is reporting certificate-related issues while fetching binary dependencies from the platform at https://dl.yamcs.org, it could be that the Let's Encrypt CA is missing from your environment's trusted roots.

A workaround is to use a custom cacerts:

1. Copy cacerts from the Java installation to a new file, such as (note that your path might have /jre in it as well, depending on your Java installation):
   ```
   cp $JAVA_HOME/lib/security/cacerts .
   ```
2. Fetch the Let's Encrypt certificate:
   ```
   wget https://letsencrypt.org/certs/isrgrootx1.der
   ```
3. Import the certificate into that copy (the default password is "changeit"):
   ```
   keytool -import -alias letsencrypt -keystore ./cacerts -file isrgrootx1.der
   ```
4. Run the Maven build, specifying the new certificate keystore:
   ```
   mvn -Djavax.net.ssl.trustStore=./cacerts \
       -Djavax.net.ssl.trustStorePassword="changeit" \
       package -Dtycho.localArtifacts=ignore
   ```
