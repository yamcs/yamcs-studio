package org.yamcs.studio.ycl.dsl;

import org.yamcs.studio.ycl.dsl.YCLStandaloneSetupGenerated;

/**
 * Initialization support for running Xtext languages without equinox extension registry
 */
public class YCLStandaloneSetup extends YCLStandaloneSetupGenerated {

    public static void doSetup() {
        new YCLStandaloneSetup().createInjectorAndDoEMFRegistration();
    }
}
