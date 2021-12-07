package org.yamcs.studio.autocomplete.para;

import org.yamcs.studio.autocomplete.AutoCompleteConstants;
import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.ContentType;
import org.yamcs.studio.autocomplete.parser.IContentParser;

/**
 * Yamcs Parameter DataSource content parser.
 */
public class ParameterContentParser implements IContentParser {

    public static final String PARA_SOURCE = "para://";

    @Override
    public boolean accept(ContentDescriptor desc) {
        if (desc.getValue().startsWith(AutoCompleteConstants.FORMULA_PREFIX)) {
            return false;
        }
        if (desc.getValue().startsWith(PARA_SOURCE)
                || (desc.getValue().indexOf(AutoCompleteConstants.DATA_SOURCE_NAME_SEPARATOR) == -1
                        && PARA_SOURCE.equals(desc.getDefaultDataSource()))) {
            return true;
        }
        return false;
    }

    @Override
    public ContentDescriptor parse(ContentDescriptor desc) {
        var startIndex = 0;
        var contentToParse = desc.getValue();
        if (contentToParse.startsWith(PARA_SOURCE)) {
            contentToParse = contentToParse.substring(PARA_SOURCE.length());
        }
        var currentDesc = new ContentDescriptor();
        currentDesc.setContentType(ContentType.PVName);
        currentDesc.setStartIndex(startIndex);
        currentDesc.setValue(contentToParse);
        return currentDesc;
    }
}
