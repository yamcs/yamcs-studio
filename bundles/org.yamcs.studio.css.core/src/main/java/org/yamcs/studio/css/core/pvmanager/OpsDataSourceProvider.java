package org.yamcs.studio.css.core.pvmanager;

import org.diirt.datasource.DataSource;
import org.diirt.datasource.DataSourceProvider;

public class OpsDataSourceProvider extends DataSourceProvider {

    @Override
    public String getName() {
        return "ops";
    }

    @Override
    public DataSource createInstance() {
        return new OpsDataSource();
    }
}
