Yamcs Studio is an Eclipse RCP project leveraging the Eclipse Workbench. The build process uses Maven with Tycho. This combination enables headless builds and improved dependency management.

Yamcs Studio includes many features and plugins from CS-Studio, these dependencies are managed using git submodules. We could have also set up some central repository with all the CS-Studio binaries, but it's a lot more useful if you have the source code on your developer machine so that you can navigate the code, and make changes to it within Eclipse.

In what follows we'll explain how to build Yamcs Studio from the command line first. When you get that to work, you can read the section on how to run products from within `Eclipse for RCP and RAP developers` instead. This is what you'll want for any serious development because with this setup there's no build step anymore. 

Note that you do need to go through the headless build first as this sets up your environment for the Eclipse build too.


### Prerequisites
* Eclipse for RCP and RAP developers
* Oracle JDK 8
* Maven

### Headless build

There are two different maven reactors (one bundles up non-OSGI friendly dependencies), so use this simple wrapper script to build both:
```
./make-product.sh
```

### Next up, Eclipse

With the previous steps all successfully completed, we now continue with getting it working from within Eclipse. This requires a bit more manual set-up, but the end result is worth it, since you will not have to sit through a 15-minute build on every change.

- Go to `Preferences > Maven > Errors/Warnings`, select `Warning` or `Ignore` (you choose) for the setting `Plugin execution not covered by lifecycle configuration`.

- Under `Preferences > Maven > Discovery`, click on the `Open Catalog` button and install the Tycho Configurator from there. This plugin will add support for Tycho artefacts to the integrated Maven build cycle.

- Configure a Target Platform by going to `Preferences > Plug-in Development > Target Platform`. We want to configure Eclipse so that it gives priority to the projects that are open in your workspace, and only then checks the local P2 repository which you built earlier. So click `Add...`. Choose to initialize with the `Current Target`. Click `Next`. Click `Add...` to add a directory. Choose the directory `css/local_p2_repository` that we generated earlier on the command line. Click `Finish`. And select that new entry as the active Target Platform. Close the preferences pane with `OK`.

- Import the Yamcs Studio maven projects into the workspace by right clicking in the navigator and choosing `Import > Maven > Existing Maven Projects`. Follow the on-screen instructions.

- Open `org.yamcs.studio.dist.default.repository/yamcs-studio.product`

- Click `Launch an Eclipse Application`

### Keep your development copy up-to-date

#### Target Platform
When you run Yamcs Studio through Eclipse RCP, you always work with a Target Platform, which we configured in the above steps. Everytime you run `./make-product.sh` (which should hopefully not need to happen that often), you have to sync your Target Platform within Eclipse so that the newly generated jars are picked up.

1. Go to `Preferences > Plugin-in Development > Target Platform`, select your active platform and click `Reload...`.
2. Close the preferences dialog with OK.
3. Adapt your launch configuration to the updated platform, by opening `org.yamcs.studio.dist.default.repository/yamcs-studio.product` and clicking the `Synchronize` link.

