package org.yamcs.studio.autocomplete.para;

import org.yamcs.studio.autocomplete.AutoCompleteConstants;
import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.ContentType;
import org.yamcs.studio.autocomplete.parser.IContentParser;

/**
 * Yamcs Parameter DataSource content parser.
 */
public class XtceContentParser implements IContentParser {

    public static final String XTCE_SOURCE = "para://";

    @Override
    public boolean accept(ContentDescriptor desc) {
        if (desc.getValue().startsWith(AutoCompleteConstants.FORMULA_PREFIX)) {
            return false;
        }
        if (desc.getValue().startsWith(XTCE_SOURCE)
                || (desc.getValue().indexOf(AutoCompleteConstants.DATA_SOURCE_NAME_SEPARATOR) == -1
                        && XTCE_SOURCE.equals(desc.getDefaultDataSource()))) {
            return true;
        }
        return false;
    }

    @Override
    public ContentDescriptor parse(ContentDescriptor desc) {
        int startIndex = 0;
        String contentToParse = desc.getValue();
        if (contentToParse.startsWith(XTCE_SOURCE)) {
            contentToParse = contentToParse.substring(XTCE_SOURCE.length());
            // startIndex = XTCE_SOURCE.length();
        }
        ContentDescriptor currentDesc = new ContentDescriptor();
        currentDesc.setContentType(ContentType.PVName);
        currentDesc.setStartIndex(startIndex);
        currentDesc.setValue(contentToParse);
        return currentDesc;
    }
}
