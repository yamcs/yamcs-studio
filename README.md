# Yamcs Studio

## Binary Releases

https://github.com/yamcs/yamcs-studio/releases/


## End-User Documentation

https://docs.yamcs.org/yamcs-studio/


## Building from Source

### Headless Build

```
mvn clean package -DskipTests
```


### Eclipse Development

Yamcs Studio is developed via 'Eclipse for RCP and RAP developers'. The advantage over the headless build is that Eclipse can launch snapshot copies of Yamcs Studio in seconds instead of minutes. Follow these steps to prepare your Eclipse development environment:

- Ensure you successfully ran the headless build first, because this will generate artefacts needed in the Eclipse build.

- Go to `Preferences > Maven > Errors/Warnings`, select `Warning` or `Ignore` (you choose) for the setting `Plugin execution not covered by lifecycle configuration`.

- Under `Preferences > Maven > Discovery`, click on the `Open Catalog` button and install the Tycho Configurator from there. This plugin will add support for Tycho artefacts to the integrated Maven build cycle.

- Import the Yamcs Studio maven projects into the workspace by right clicking in the navigator and choosing `Import > Maven > Existing Maven Projects`. Follow the on-screen instructions.

- Open the `*.platform` file found in the project `org.yamcs.studio.platform`. Click the upper right link `Set as target platform`. This operation may take a while, as it will attempt to resolve and download third-party dependencies.

- All error signs should now be resolved. If not, try right-clicking the yamcs-studio project and choose `Maven > Update Project Configuration`.

- Open one of the `*.product` files found under `org.yamcs.studio.releng`.

- Click `Synchronize` followed by `Launch an Eclipse Application`.


### Compile it from source(Tested on Ubuntu 18.04.5 LTS)

1. `maven` _must_ be set to __JAVA 11__:
    `export export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/`

__NOTE__: Configuring JAVA_HOME export will only make the setting active for the _current_ shell session. If you open
           a new shell, this setting _will_ be discarded. You may add this command to your  `.bashrc` script to  enforce
            it on every shell session.

2. To ensure maven is configured correctly:
    `mvn --version`:
   
    ```
        Java version: 11.0.9.1, vendor: Ubuntu, runtime: /usr/lib/jvm/java-11-openjdk-amd64
        Default locale: en_US, platform encoding: UTF-8
        OS name: "linux", version: "5.4.0-65-generic", arch: "amd64", family: "unix"
    ```
    
The important part here is __Java 11__; this means maven is configured to use JAVA11. If it is anything lower than that,
then yamcs-studio will _not_ build.

3. Build Yamcs Studio:

```
mvn clean package -DskipTests
```

This _will_ take a while.

4. Untar and install:
```
cd releng/org.yamcs.studio.editor.product/target/products/
mkdir  /opt/yamcs-studio
tar -xzf yamcs-studio-1.5.4-SNAPSHOT-linux.gtk.x86_64.tar.gz --strip-components=1 -C "/opt/yamcs-studio/"

```

That's it! YAMCS Studio is installed in `/opt/yamcs-studio`.


