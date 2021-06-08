package org.csstudio.opibuilder.editor;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.csstudio.opibuilder.editor";

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);

        OPIBuilderPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(event -> {
            if (event.getProperty().equals(PreferencesHelper.SCHEMA_OPI)) {
                IDecoratorManager decoratorManager = PlatformUI.getWorkbench().getDecoratorManager();
                decoratorManager.update(SchemaDecorator.ID);
            }
        });
    }
}
