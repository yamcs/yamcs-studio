package org.yamcs.studio.core.web;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class URLBuilder {

    private String path;
    private Map<String, List<String>> params;

    public URLBuilder(String path) {
        this.path = path;
    }

    public void setParam(String param, String value) {
        setParam(param, Arrays.asList(value));
    }

    public void setParam(String param, int value) {
        setParam(param, String.valueOf(value));
    }

    public void setParam(String param, long value) {
        setParam(param, String.valueOf(value));
    }

    public void setParam(String param, boolean value) {
        setParam(param, String.valueOf(value));
    }

    public void setParam(String param, List<String> values) {
        if (params == null)
            params = new LinkedHashMap<>();
        params.put(param, values);
    }

    @Override
    public String toString() {
        if (params == null || params.isEmpty()) {
            return path;
        } else {
            StringBuilder buf = new StringBuilder(path);
            boolean first = true;
            for (Entry<String, List<String>> param : params.entrySet()) {
                if (first) {
                    buf.append("?");
                    first = false;
                } else {
                    buf.append("&");
                }
                if (param.getValue().size() > 1) {
                    boolean subfirst = true;
                    for (String value : param.getValue()) {
                        if (!subfirst)
                            buf.append("&");
                        subfirst = false;
                        buf.append(param.getKey()).append("[]=").append(value);
                    }
                } else if (param.getValue().size() == 1) {
                    buf.append(param.getKey()).append("=").append(param.getValue().get(0));
                } else {
                    buf.append(param.getKey()).append("=");
                }

            }
            return buf.toString();
        }
    }
}
