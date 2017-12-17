package org.yamcs.studio.css.core;

import org.csstudio.utility.product.IWorkbenchWindowAdvisorExtPoint;
import org.diirt.datasource.CompositeDataSource;
import org.diirt.datasource.CompositeDataSourceConfiguration;
import org.diirt.datasource.PVManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.yamcs.studio.css.core.pvmanager.ParameterDataSourceProvider;
import org.yamcs.studio.css.core.pvmanager.SoftwareParameterDataSourceProvider;

public class DiirtStartup implements IWorkbenchWindowAdvisorExtPoint {

    @Override
    public void preWindowOpen() {
        CompositeDataSource defaultDs = (CompositeDataSource) PVManager.getDefaultDataSource();
        defaultDs.putDataSource(new ParameterDataSourceProvider());
        defaultDs.putDataSource(new SoftwareParameterDataSourceProvider());

        defaultDs.setConfiguration(new CompositeDataSourceConfiguration().defaultDataSource("para").delimiter("://"));
        PVManager.setDefaultDataSource(defaultDs);
    }

    @Override
    public boolean preWindowShellClose() {
        return true;
    }

    @Override
    public void postWindowRestore() throws WorkbenchException {
    }

    @Override
    public void postWindowCreate() {
    }

    @Override
    public void postWindowOpen() {
    }

    @Override
    public void postWindowClose() {
    }

    @Override
    public IStatus saveState(IMemento memento) {
        return null;
    }

    @Override
    public IStatus restoreState(IMemento memento) {
        return null;
    }
}
