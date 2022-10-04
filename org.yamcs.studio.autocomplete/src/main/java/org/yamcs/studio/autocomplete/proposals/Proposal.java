/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.proposals;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.Assert;
import org.yamcs.studio.autocomplete.tooltips.TooltipData;
import org.yamcs.studio.autocomplete.tooltips.TooltipDataHandler;

/**
 * Defines a proposal as it will be displayed.
 */
public class Proposal implements Comparable<Proposal> {

    /**
     * Value that completes the field content (originalValue) and will be displayed in the main pop-up.
     */
    private final String value;
    /**
     * Description that will be displayed in a secondary pop-up if not <code>null</code>.
     */
    private String description;
    /**
     * SWT StyleRange that will be applied to value.
     */
    private List<ProposalStyle> styles;
    /**
     * <code>true</code> if the proposal is not a final one (example: CWS- as part of a PV name CWS-C4CO-...) => display
     * a magnifying glass icon.
     */
    private final boolean isPartial;
    /**
     * <code>true</code> if the proposal is a formula function => display a function icon.
     */
    private boolean isFunction = false;
    /**
     * <code>true</code> if the proposal start with the field content => append instead of replace. Used by top proposal
     * manager.
     */
    private boolean startWithContent = false;
    /**
     * Used by top proposals manager to calculate a display priority if one proposal is provided more than once.
     */
    private int occurrence;
    /**
     * Insertion position of value in the field content (original content).
     */
    private int insertionPos = 0;
    /**
     * Value submitted for auto-completion.
     */
    private String originalValue = "";
    /**
     * Data that will be processed by {@link TooltipDataHandler} when the proposal is selected in the UI.
     */
    private List<TooltipData> tooltips;

    public Proposal(String value, boolean isPartial) {
        Assert.isNotNull(value);
        Assert.isTrue(!value.isEmpty());
        Assert.isNotNull(isPartial);
        this.value = value;
        this.isPartial = isPartial;
        occurrence = 1;
        styles = new ArrayList<>();
        tooltips = new ArrayList<>();
    }

    public int getInsertionPos() {
        return insertionPos < 0 ? 0 : insertionPos;
    }

    public void increment() {
        occurrence++;
    }

    public void decrement() {
        occurrence--;
    }

    public int getOccurrence() {
        return occurrence;
    }

    public void addStyle(ProposalStyle style) {
        styles.add(style);
    }

    public void addTooltipData(TooltipData td) {
        tooltips.add(td);
    }

    public String getValue() {
        return value;
    }

    public boolean isPartial() {
        return isPartial;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFunction() {
        return isFunction;
    }

    public void setFunction(boolean isFunction) {
        this.isFunction = isFunction;
    }

    public boolean getStartWithContent() {
        return startWithContent;
    }

    public void setStartWithContent(boolean startWithContent) {
        this.startWithContent = startWithContent;
    }

    public List<ProposalStyle> getStyles() {
        return styles;
    }

    public List<TooltipData> getTooltips() {
        return tooltips;
    }

    public void setInsertionPos(int insertionPos) {
        this.insertionPos = insertionPos;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        var other = (Proposal) obj;
        if (!Objects.equals(value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Proposal arg0) {
        if (startWithContent && !arg0.getStartWithContent()) {
            return -1;
        } else if (!startWithContent && arg0.getStartWithContent()) {
            return 1;
        } else {
            if (occurrence > arg0.getOccurrence()) {
                return -1;
            } else if (occurrence < arg0.getOccurrence()) {
                return 1;
            } else {
                return value.compareTo(arg0.value);
            }
        }
    }

    @Override
    public String toString() {
        return "Proposal [value=" + value + ", description=" + description + ", styles=" + styles + ", isPartial="
                + isPartial + ", isFunction=" + isFunction + ", startWithContent=" + startWithContent + ", occurrence="
                + occurrence + ", insertionPos=" + insertionPos + ", originalValue=" + originalValue + ", tooltips="
                + tooltips + "]";
    }
}
