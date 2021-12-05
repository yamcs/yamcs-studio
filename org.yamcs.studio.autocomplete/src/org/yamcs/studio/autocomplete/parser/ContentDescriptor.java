/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.parser;

import org.yamcs.studio.autocomplete.AutoCompleteType;
import org.yamcs.studio.autocomplete.IAutoCompleteProvider;

/**
 * Descriptor used in {@link IContentParser} and {@link IAutoCompleteProvider} to describe the current auto-completed
 * content.
 */
public class ContentDescriptor {

    /** Parsed value to complete */
    private String value = "";
    private AutoCompleteType autoCompleteType;
    private ContentType contentType = ContentType.Empty;
    /** Original content to complete */
    private String originalContent;
    /** Default data source defined in CSS */
    private String defaultDataSource;
    /** Parsed value start index in original content */
    private int startIndex = 0;
    /** Parsed value end index in original content */
    private int endIndex = 0;
    /** If <code>true</code> the descriptor will be submitted again to parsers */
    private boolean replay = false;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public AutoCompleteType getAutoCompleteType() {
        return autoCompleteType;
    }

    public void setAutoCompleteType(AutoCompleteType autoCompleteType) {
        this.autoCompleteType = autoCompleteType;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    public String getDefaultDataSource() {
        return defaultDataSource;
    }

    public void setDefaultDataSource(String defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public boolean isReplay() {
        return replay;
    }

    public void setReplay(boolean replay) {
        this.replay = replay;
    }

    @Override
    public String toString() {
        return "ContentDescriptor [value=" + value + ", autoCompleteType="
                + autoCompleteType + ", contentType=" + contentType
                + ", originalContent=" + originalContent
                + ", defaultDataSource=" + defaultDataSource + ", startIndex="
                + startIndex + ", endIndex=" + endIndex + ", replay=" + replay
                + "]";
    }

}
