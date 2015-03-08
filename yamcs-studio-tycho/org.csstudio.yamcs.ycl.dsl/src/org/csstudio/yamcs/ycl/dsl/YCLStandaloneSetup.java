package org.csstudio.yamcs.ycl.dsl;

/**
 * Initialization support for running Xtext languages 
 * without equinox extension registry
 */
public class YCLStandaloneSetup extends YCLStandaloneSetupGenerated{

	public static void doSetup() {
		new YCLStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}

