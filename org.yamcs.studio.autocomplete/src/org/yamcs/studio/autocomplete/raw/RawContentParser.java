package org.yamcs.studio.autocomplete.raw;

import org.yamcs.studio.autocomplete.parser.ContentDescriptor;
import org.yamcs.studio.autocomplete.parser.ContentType;
import org.yamcs.studio.autocomplete.parser.IContentParser;

/**
 * Yamcs raw DataSource content parser.
 */
public class RawContentParser implements IContentParser {

    public static final String RAW_SOURCE = "raw://";

    @Override
    public boolean accept(ContentDescriptor desc) {
        return desc.getValue().startsWith(RAW_SOURCE);
    }

    @Override
    public ContentDescriptor parse(ContentDescriptor desc) {
        int startIndex = 0;
        String contentToParse = desc.getValue();
        if (contentToParse.startsWith(RAW_SOURCE)) {
            contentToParse = contentToParse.substring(RAW_SOURCE.length());
        }
        ContentDescriptor currentDesc = new ContentDescriptor();
        currentDesc.setContentType(ContentType.PVName);
        currentDesc.setStartIndex(startIndex);
        currentDesc.setValue(contentToParse);
        return currentDesc;
    }
}
