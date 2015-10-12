Yamcs Studio is an Eclipse RCP project leveraging the Eclipse Workbench. The build process uses Maven with Tycho. This combination enables headless builds and improved dependency management.

Yamcs Studio includes many features and plugins from CS-Studio, these dependencies are managed using git submodules. We could have also set up some central repository with all the CS-Studio binaries, but it's a lot more useful if you have the source code on your developer machine so that you can navigate the code, and make changes to it within Eclipse.

In what follows we'll explain how to build Yamcs Studio from the command line first. When you get that to work, you can read the section on how to run products from within `Eclipse for RCP and RAP developers` instead. This is what you'll want for any serious development because with this setup there's no build step anymore. 

Note that you do need to go through the headless build first as this sets up your environment for the Eclipse build too.


### Prerequisites
* Eclipse for RCP and RAP developers (Mars recommended)
* Oracle JDK 8
* Maven

### Build Yamcs
Yamcs Studio depends on Yamcs API. Therefore, you must first build the Yamcs repository from source.
```
git clone https://github.com/yamcs/yamcs
cd yamcs
mvn clean install -DskipTests
cd ..
```

### Generate Yamcs Studio Products
Clone the Yamcs Studio repository, including the CS-Studio submodules. These are 'separate' nested git repositories. Read up on git-submodules, if this concept is new to you.
```
git clone --recursive https://github.com/yamcs/yamcs-studio
cd yamcs-studio
```

Run the `make-platform.sh` helper script. This will run some checks and guide you through the whole process of setting up the CS-Studio dependencies.
```
./make-platform.sh
```

With that out of the way, we're now ready to generate the actual Yamcs Studio product. There are two different maven reactors (one bundles up non-OSGI friendly dependencies), so use this simple wrapper script to build both:
```
./make-product.sh
```

The generated products can be found in `org.yamcs.studio.dist.default.repository/target/products/`. There are 64-bit versions for Mac, Linux and Windows.

### Next up, Eclipse

With the previous steps all successfully completed, we now continue with getting it working from within Eclipse. This requires a bit more manual set-up, but the end result is worth it, since you will not have to sit through a 15-minute build on every change.

- Go to `Preferences > Maven > Errors/Warnings`, select `Warning` or `Ignore` (you choose) for the setting `Plugin execution not covered by lifecycle configuration`.

- Under `Preferences > Maven > Discovery`, click on the `Open Catalog` button and install the Tycho Configurator from there. This plugin will add support for Tycho artefacts to the integrated Maven build cycle.

- For now, you'll need to install the Xtext SDK as well from this update site: http://download.eclipse.org/modeling/tmf/xtext/updates/composite/releases/ I'm hoping to make this optional in the future (presumably by packaging it up in a bundle ourselves).

- Configure a Target Platform by going to `Preferences > Plug-in Development > Target Platform`. We want to configure Eclipse so that it gives priority to the projects that are open in your workspace, and only then checks the local P2 repository which you built earlier. So click `Add...`. Choose to initialize with the `Current Target`. Click `Next`. Click `Add...` to add a directory. Choose the directory `css/local_p2_repository` that we generated earlier on the command line. Click `Finish`. And select that new entry as the active Target Platform. Close the preferences pane with `OK`.

- Import the Yamcs Studio maven projects under `yamcs-studio-tycho` into the workspace by right clicking in the navigator and choosing `Import > Maven > Existing Maven Projects`. Follow the on-screen instructions.

- Open `org.yamcs.studio.dist.default.repository/yamcs-studio.product`
![Product Testing](images/product-testing.png)

- Click `Launch an Eclipse Application`

This will give you a lot of error output the first time, but that's fine. We need to tweak the generated launch file first. Note that this launch file is specific to your Eclipse installation and should therefore not be version controlled.

So, to fix the errors, open the launch configuration dialog (`Run > Run Configurations...`), and in the tab Plugins of the `yamcs-studio.product` configuration, include the lower version for `org.antlr.runtime` in the setup.

You'll have to do this workaround every time you click that launch button. We have not yet found an elegant solution to this, so please be patient.

I would also recommend to tick the option `Validate plug-ins automatically prior to launching`. This will save you time.

### Keep your development copy up-to-date

#### Target Platform
When you run Yamcs Studio through Eclipse RCP, you always work with a Target Platform, which we configured in the above steps. Everytime you run `./make-product.sh` (which should hopefully not need to happen that often), you have to sync your Target Platform within Eclipse so that the newly generated jars are picked up.

1. Go to `Preferences > Plugin-in Development > Target Platform`, select your active platform and click `Reload...`.
2. Close the preferences dialog with OK.
3. Adapt your launch configuration to the updated platform, by opening `org.yamcs.studio.dist.default.repository/yamcs-studio.product` and clicking the `Synchronize` link.

#### Git Submodules
In general we try to stick to a stable version of our upstream CS-Studio dependencies so that you don't need to run `make-platform.sh` all the time. Assuming that none of the submodules were updated, you typically only run `make-product.sh` to generate the Yamcs Studio product.

If, however, `git status` starts outputting weird error messages, it may be time for you to update your entire CS-Studio Platform. You'll want to do something like this:
```
git submodule update
./make-platform.sh
./make-product.sh
```
This will give you the option to update your platform in-place, or to replace it entirely. Be aware that `git submodule update` will really update your submodules, thereby losing any local uncommited changes you may have made in there. This is because submodules by default are set to a detached HEAD. If you want to keep your changes when updating submodules, set them aside using `git stash`, or commit them on a branch which you can `git checkout` again after having updated your submodules.
