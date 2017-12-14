package org.yamcs.studio.css.utility;

import org.csstudio.diirt.util.DiirtStartup;
import org.diirt.datasource.CompositeDataSource;
import org.diirt.datasource.CompositeDataSourceConfiguration;
import org.diirt.datasource.PVManager;

public class PatchedDiirtStartup extends DiirtStartup {

    @Override
    public void preWindowOpen() {
        CompositeDataSource defaultDs = (CompositeDataSource) PVManager.getDefaultDataSource();
        defaultDs.setConfiguration(new CompositeDataSourceConfiguration().defaultDataSource("para").delimiter("://"));
        PVManager.setDefaultDataSource(defaultDs);
    }
}
