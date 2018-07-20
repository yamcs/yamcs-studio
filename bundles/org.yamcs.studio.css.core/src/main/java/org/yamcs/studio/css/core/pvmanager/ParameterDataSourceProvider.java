package org.yamcs.studio.css.core.pvmanager;

import org.diirt.datasource.DataSource;
import org.diirt.datasource.DataSourceProvider;

public class ParameterDataSourceProvider extends DataSourceProvider {

    private String name;

    public ParameterDataSourceProvider(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DataSource createInstance() {
        return new ParameterDataSource();
    }
}
