package org.csstudio.autocomplete.pvmanager.yamcs;

import org.csstudio.autocomplete.AutoCompleteConstants;
import org.csstudio.autocomplete.parser.ContentDescriptor;
import org.csstudio.autocomplete.parser.ContentType;
import org.csstudio.autocomplete.parser.IContentParser;

/**
 * Yamcs Data Source content parser.
 */
public class YamcsContentParser implements IContentParser {

	public static final String YAMCS_SOURCE = "yamcs://";

	@Override
	public boolean accept(ContentDescriptor desc) {
		if (desc.getValue().startsWith(AutoCompleteConstants.FORMULA_PREFIX))
			return false;
		if (desc.getValue().startsWith(YAMCS_SOURCE)
				|| (desc.getValue().indexOf(AutoCompleteConstants.DATA_SOURCE_NAME_SEPARATOR) == -1 
				&& YAMCS_SOURCE.equals(desc.getDefaultDataSource())))
			return true;
		return false;
	}

	@Override
	public ContentDescriptor parse(ContentDescriptor desc) {
		int startIndex = 0;
		String contentToParse = desc.getValue();
		if (contentToParse.startsWith(YAMCS_SOURCE)) {
			contentToParse = contentToParse.substring(YAMCS_SOURCE.length());
			// startIndex = YAMCS_SOURCE.length();
		}
		ContentDescriptor currentDesc = new ContentDescriptor();
		currentDesc.setContentType(ContentType.PVName);
		currentDesc.setStartIndex(startIndex);
		currentDesc.setValue(contentToParse);
		return currentDesc;
	}

}
