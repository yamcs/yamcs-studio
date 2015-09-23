package org.yamcs.studio.ui.css.pvmanager.autocomplete;

import org.csstudio.autocomplete.AutoCompleteConstants;
import org.csstudio.autocomplete.parser.ContentDescriptor;
import org.csstudio.autocomplete.parser.ContentType;
import org.csstudio.autocomplete.parser.IContentParser;

/**
 * Yamcs Parameter DataSource content parser.
 */
public class ParameterContentParser implements IContentParser {

    public static final String PARA_SOURCE = "para://";

    @Override
    public boolean accept(ContentDescriptor desc) {
        if (desc.getValue().startsWith(AutoCompleteConstants.FORMULA_PREFIX))
            return false;
        if (desc.getValue().startsWith(PARA_SOURCE)
                || (desc.getValue().indexOf(AutoCompleteConstants.DATA_SOURCE_NAME_SEPARATOR) == -1
                && PARA_SOURCE.equals(desc.getDefaultDataSource())))
            return true;
        return false;
    }

    @Override
    public ContentDescriptor parse(ContentDescriptor desc) {
        int startIndex = 0;
        String contentToParse = desc.getValue();
        if (contentToParse.startsWith(PARA_SOURCE)) {
            contentToParse = contentToParse.substring(PARA_SOURCE.length());
            // startIndex = PARA_SOURCE.length();
        }
        ContentDescriptor currentDesc = new ContentDescriptor();
        currentDesc.setContentType(ContentType.PVName);
        currentDesc.setStartIndex(startIndex);
        currentDesc.setValue(contentToParse);
        return currentDesc;
    }
}
