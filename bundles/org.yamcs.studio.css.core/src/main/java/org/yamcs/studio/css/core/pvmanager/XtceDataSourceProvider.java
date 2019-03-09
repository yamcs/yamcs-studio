package org.yamcs.studio.css.core.pvmanager;

import org.diirt.datasource.DataSource;
import org.diirt.datasource.DataSourceProvider;

public class XtceDataSourceProvider extends DataSourceProvider {

    @Override
    public String getName() {
        return "para";
    }

    @Override
    public DataSource createInstance() {
        return new XtceDataSource();
    }
}
