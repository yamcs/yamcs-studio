/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete;

/**
 * Used by {@link AutoCompleteService} to notify that a {@link IAutoCompleteProvider} has returned a
 * {@link AutoCompleteResult}.
 */
public interface IAutoCompleteResultListener {

    void handleResult(Long uniqueId, Integer index, AutoCompleteResult result);
}
