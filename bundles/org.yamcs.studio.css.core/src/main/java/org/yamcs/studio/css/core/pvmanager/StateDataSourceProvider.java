package org.yamcs.studio.css.core.pvmanager;

import org.diirt.datasource.DataSource;
import org.diirt.datasource.DataSourceProvider;

public class StateDataSourceProvider extends DataSourceProvider {

    @Override
    public String getName() {
        return "state";
    }

    @Override
    public DataSource createInstance() {
        return new StateDataSource();
    }
}
