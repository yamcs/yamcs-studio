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
        System.out.println("Asked to create a new datasource");
        return new ParameterDataSource();
    }
}
