package org.yamcs.studio.css.core.pvmanager;

import static org.diirt.vtype.ValueFactory.alarmNone;
import static org.diirt.vtype.ValueFactory.newVString;
import static org.diirt.vtype.ValueFactory.timeNow;

import java.util.Objects;

import org.yamcs.protobuf.UserInfo;
import org.yamcs.studio.core.YamcsPlugin;

public class YamcsUsernameChannelHandler extends StateChannelHandler {

    private String previousValue = null;

    public YamcsUsernameChannelHandler(String channelName) {
        super(channelName);
    }

    @Override
    protected Object createValue() {
        String value = null;
        UserInfo userInfo = YamcsPlugin.getUser();
        if (userInfo != null) {
            value = userInfo.getName();
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
