package org.yamcs.studio.css.core.pvmanager;

import static org.diirt.vtype.ValueFactory.alarmNone;
import static org.diirt.vtype.ValueFactory.newVString;
import static org.diirt.vtype.ValueFactory.timeNow;

import java.util.Objects;

import org.yamcs.client.YamcsClient;
import org.yamcs.studio.core.YamcsPlugin;

public class YamcsHostChannelHandler extends StateChannelHandler {

    private String previousValue = null;

    public YamcsHostChannelHandler(String channelName) {
        super(channelName);
    }

    @Override
    protected Object createValue() {
        String value = null;
        YamcsClient client = YamcsPlugin.getYamcsClient();
        if (client != null) {
            value = client.getHost();
        }

        if (value == null) {
            value = "";
        }

        if (!Objects.equals(value, previousValue)) {
            previousValue = value;
            return newVString(value, alarmNone(), timeNow());
        } else {
            return null;
        }
    }
}
