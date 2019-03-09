package org.yamcs.studio.css.core.pvmanager.autocomplete;

import org.csstudio.autocomplete.AutoCompleteConstants;
import org.csstudio.autocomplete.parser.ContentDescriptor;
import org.csstudio.autocomplete.parser.ContentType;
import org.csstudio.autocomplete.parser.IContentParser;

/**
 * Yamcs Opsname DataSource content parser.
 */
public class OpsContentParser implements IContentParser {

    public static final String OPS_SOURCE = "ops://";

    @Override
    public boolean accept(ContentDescriptor desc) {
        if (desc.getValue().startsWith(AutoCompleteConstants.FORMULA_PREFIX)) {
            return false;
        } else if (desc.getValue().startsWith("/")) {
            return false;
        }
        return true;
    }

    @Override
    public ContentDescriptor parse(ContentDescriptor desc) {
        int startIndex = 0;
        String contentToParse = desc.getValue();
        if (contentToParse.startsWith(OPS_SOURCE)) {
            contentToParse = contentToParse.substring(OPS_SOURCE.length());
            // startIndex = OPS_SOURCE.length();
        }
        ContentDescriptor currentDesc = new ContentDescriptor();
        currentDesc.setContentType(ContentType.PVName);
        currentDesc.setStartIndex(startIndex);
        currentDesc.setValue(contentToParse);
        return currentDesc;
    }
}
