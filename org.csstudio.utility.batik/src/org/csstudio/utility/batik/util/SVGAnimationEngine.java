/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.utility.batik.util;

import org.apache.batik.anim.timing.TimedDocumentRoot;
import org.apache.batik.bridge.BridgeContext;
import org.w3c.dom.Document;

/**
 * Extension of standard {@link org.apache.batik.bridge.SVGAnimationEngine} which allows to access the
 * {@link TimedDocumentRoot}.
 */
public class SVGAnimationEngine extends org.apache.batik.bridge.SVGAnimationEngine {

    public SVGAnimationEngine(Document doc, BridgeContext ctx) {
        super(doc, ctx);
    }

    public TimedDocumentRoot getTimedDocumentRoot() {
        return timedDocumentRoot;
    }
}
