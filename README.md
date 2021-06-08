# Yamcs Studio

## Binary Releases

https://github.com/yamcs/yamcs-studio/releases/

**Note to macOS users:** Binaries are not signed, nor notarized. Bypass Gatekeeper checks using:
```
sudo xattr -rds com.apple.quarantine ~/Downloads/Yamcs\ Studio.app
```

## End-User Documentation

https://docs.yamcs.org/yamcs-studio/


## Building from Source

### Headless Build

```
mvn clean package -Dtycho.localArtifacts=ignore
```


### Eclipse Development

Yamcs Studio is developed via 'Eclipse for RCP and RAP developers'. The advantage over the headless build is that Eclipse can launch snapshot copies of Yamcs Studio in seconds instead of minutes. Follow these steps to prepare your Eclipse development environment:

- Ensure you successfully ran the headless build first, because this will generate artefacts needed in the Eclipse build.

- Go to `Preferences > Maven > Errors/Warnings`, select `Warning` or `Ignore` (you choose) for the setting `Plugin execution not covered by lifecycle configuration`.

- Under `Preferences > Maven > Discovery`, click on the `Open Catalog` button and install the Tycho Configurator from there. This plugin will add support for Tycho artefacts to the integrated Maven build cycle.

- Import the Yamcs Studio maven projects into the workspace by right clicking in the navigator and choosing `Import > Maven > Existing Maven Projects`. Follow the on-screen instructions.

- Open the `*.platform` file found in the project `org.yamcs.studio.platform`. Click the upper right link `Set as target platform`. This operation may take a while, as it will attempt to resolve and download third-party dependencies.

- All error signs should now be resolved. If not, try right-clicking the yamcs-studio project and choose `Maven > Update Project Configuration`.

- Open the `org.yamcs.studio.editor.product/yamcs-studio.product` file.

- Click `Synchronize` followed by `Launch an Eclipse Application`.
