/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.sim;

import java.math.BigDecimal;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.Instant;

/**
 *
 * @author carcassi
 */
class XmlTimeStampAdapter extends XmlAdapter<BigDecimal, Instant> {

    @Override
    public Instant unmarshal(BigDecimal v) throws Exception {
        return Instant.ofEpochSecond(v.longValue(), v.remainder(new BigDecimal(1)).scaleByPowerOfTen(9).intValue());
    }

    @Override
    public BigDecimal marshal(Instant v) throws Exception {
        return new BigDecimal(v.getNano()).scaleByPowerOfTen(-9).add(new BigDecimal(v.getEpochSecond()));
    }

}
