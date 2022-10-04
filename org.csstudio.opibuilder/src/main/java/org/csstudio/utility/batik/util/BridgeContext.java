/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.utility.batik.util;

import org.apache.batik.anim.timing.TimedDocumentRoot;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.script.InterpreterPool;

/**
 * Extension of standard {@link org.apache.batik.bridge.BridgeContext} which uses the extended
 * {@link SVGAnimationEngine} in order to access {@link TimedDocumentRoot}.
 */
public class BridgeContext extends org.apache.batik.bridge.BridgeContext {

    /**
     * By default we share a unique instance of InterpreterPool.
     */
    private static InterpreterPool sharedPool = new InterpreterPool();

    /**
     * Constructs a new bridge context.
     *
     * @param userAgent
     *            the user agent
     * @param loader
     *            document loader
     */
    public BridgeContext(UserAgent userAgent, DocumentLoader loader) {
        super(userAgent, sharedPool, loader);
    }

    /**
     * Returns the AnimationEngine for the document. Creates one if it doesn't exist.
     */
    @Override
    public org.apache.batik.bridge.SVGAnimationEngine getAnimationEngine() {
        if (animationEngine == null) {
            animationEngine = new SVGAnimationEngine(document, this);
            setAnimationLimitingMode();
        }
        return animationEngine;
    }
}
