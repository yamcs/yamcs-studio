/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.script;

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
