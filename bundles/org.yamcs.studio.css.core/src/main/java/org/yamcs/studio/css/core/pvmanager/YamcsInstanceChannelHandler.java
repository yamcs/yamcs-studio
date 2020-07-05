package org.yamcs.studio.css.core.pvmanager;

import static org.diirt.vtype.ValueFactory.alarmNone;
import static org.diirt.vtype.ValueFactory.newVString;
import static org.diirt.vtype.ValueFactory.timeNow;

import java.util.Objects;

import org.yamcs.studio.core.YamcsPlugin;

public class YamcsInstanceChannelHandler extends StateChannelHandler {

    private String previousValue = null;

    public YamcsInstanceChannelHandler(String channelName) {
        super(channelName);
    }

    @Override
    protected Object createValue() {
        String value = YamcsPlugin.getInstance();

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
