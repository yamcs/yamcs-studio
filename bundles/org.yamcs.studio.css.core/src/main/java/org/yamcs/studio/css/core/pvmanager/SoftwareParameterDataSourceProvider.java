package org.yamcs.studio.css.core.pvmanager;

import org.diirt.datasource.DataSource;
import org.diirt.datasource.DataSourceProvider;

public class SoftwareParameterDataSourceProvider extends DataSourceProvider {

    @Override
    public String getName() {
        return "sw";
    }

    @Override
    public DataSource createInstance() {
        return new SoftwareParameterDataSource();
    }
}
