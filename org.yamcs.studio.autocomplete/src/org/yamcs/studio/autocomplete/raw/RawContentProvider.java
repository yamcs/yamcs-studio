package org.yamcs.studio.autocomplete.raw;

import org.yamcs.studio.autocomplete.para.ParameterContentProvider;

public class RawContentProvider extends ParameterContentProvider {

    @Override
    public String getPrefix() {
        return RawContentParser.RAW_SOURCE;
    }

    @Override
    public boolean requirePrefix() {
        return true;
    }
}
