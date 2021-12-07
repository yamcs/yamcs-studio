package org.csstudio.opibuilder.scriptUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.csstudio.opibuilder.util.ResourceUtil;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CssEntityResolver implements EntityResolver {

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        try {
            var uri = new URI(systemId);
            var file = new File(uri);
            if (!file.exists()) {
                var path = ResourceUtil.getPathFromString(file.getPath());
                var inputStream = ResourceUtil.pathToInputStream(path);
                if (inputStream != null) {
                    return new InputSource(inputStream);
                }
            }
        } catch (Exception e) {
            // Entity may not be found and this may throw exception. This is normal and FileUtil will revert to
            // xi:fallback
        }

        return null;
    }

}
