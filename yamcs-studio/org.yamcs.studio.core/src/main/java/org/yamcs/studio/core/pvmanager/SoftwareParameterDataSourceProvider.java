package org.yamcs.studio.core.pvmanager;

import org.diirt.datasource.DataSource;
import org.diirt.datasource.DataSourceProvider;

public class SoftwareParameterDataSourceProvider extends DataSourceProvider {

    @Override
    public String getName() {
        return "sw";
    }

    @Override
    public DataSource createInstance() {
        System.out.println("Asked to create a new sw datasource");
        return new SoftwareParameterDataSource();
    }
}
