/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.persistence;

import org.jdom.DefaultJDOMFactory;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.input.SAXHandler;

/**
 *
 * <code>LineAwareXMLParser</code> provides a set of classes that parse an XML file and set the line number on each
 * Element that was parsed.
 */
public class LineAwareXMLParser {

    /**
     *
     * <code>LineAwareElement</code> is an element that also holds the line number at which it is located within the
     * file.
     */
    public static class LineAwareElement extends Element implements Comparable<LineAwareElement> {

        private static final long serialVersionUID = 1L;
        private final int lineNumber;

        public LineAwareElement(String name, Namespace namespace, int lineNumber) {
            super(name, namespace);
            this.lineNumber = lineNumber;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        @Override
        public int compareTo(LineAwareElement o) {
            return lineNumber - o.lineNumber;
        }
    }

    private static class XMLLineAwareBuilder extends SAXBuilder {

        private LineAwareFactory factory = new LineAwareFactory();

        public XMLLineAwareBuilder() {
            setFactory(factory);
        }

        @Override
        protected void configureContentHandler(SAXHandler contentHandler) {
            super.configureContentHandler(contentHandler);
            factory.setSAXHandler(contentHandler);
        }
    }

    private static class LineAwareFactory extends DefaultJDOMFactory {
        private SAXHandler saxHandler;

        public void setSAXHandler(SAXHandler sh) {
            saxHandler = sh;
        }

        @Override
        public Element element(String name) {
            return this.element(name, (Namespace) null);
        }

        @Override
        public Element element(String name, Namespace namespace) {
            return new LineAwareElement(name, namespace, saxHandler.getDocumentLocator().getLineNumber());
        }
    }

    /**
     * @return a new builder that parses an XML and sets the line numbers on the elements
     */
    public static SAXBuilder createBuilder() {
        return new XMLLineAwareBuilder();
    }
}
