package org.yamcs.studio.core.pvmanager;

import org.diirt.datasource.DataSource;
import org.diirt.datasource.DataSourceProvider;

public class ParameterDataSourceProvider extends DataSourceProvider {

    @Override
    public String getName() {
        return "para";
    }

    @Override
    public DataSource createInstance() {
        return new ParameterDataSource();
    }
}
