/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.loc;

import org.diirt.datasource.DataSource;
import org.diirt.datasource.DataSourceProvider;

/**
 * DataSourceProvider for local variables.
 *
 * @author carcassi
 */
public class LocalDataSourceProvider extends DataSourceProvider {

    @Override
    public String getName() {
        return "loc";
    }

    @Override
    public DataSource createInstance() {
        return new LocalDataSource();
    }

}
