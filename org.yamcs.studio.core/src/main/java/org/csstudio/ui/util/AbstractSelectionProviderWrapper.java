/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.ui.util;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Helper class to wrap a selection provider (e.g. a table, a tree, ...), so that events are fired at the same time, but
 * the selection is changed to a different type (e.g. String to ProcessVariable).
 * <p>
 * This class is useful when constructing a composite that should behave like a standard JFace widget in term of
 * selection but with CSS business objects. To use, you implement the transform and reverseTransform that convert the
 * selection back and forth.
 */
public abstract class AbstractSelectionProviderWrapper implements ISelectionProvider {

    private final ISelectionProvider wrappedProvider;
    private final ISelectionProvider eventSource;

    public AbstractSelectionProviderWrapper(ISelectionProvider wrappedProvider, ISelectionProvider eventSource) {
        this.wrappedProvider = wrappedProvider;
        this.eventSource = eventSource;
    }

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        wrappedProvider.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                listener.selectionChanged(new SelectionChangedEvent(eventSource, getSelection()));
            }

            @Override
            public int hashCode() {
                return listener.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                // TODO: this doesn't actually work!
                return listener.equals(obj);
            }
        });
    }

    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        // TODO: this doesn't actually work!
        wrappedProvider.removeSelectionChangedListener(listener);
    }

    @Override
    public ISelection getSelection() {
        var selection = wrappedProvider.getSelection();
        if (selection instanceof IStructuredSelection) {
            return transform((IStructuredSelection) wrappedProvider.getSelection());
        } else {
            return new StructuredSelection();
        }
    }

    @Override
    public void setSelection(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            wrappedProvider.setSelection(reverseTransform((IStructuredSelection) selection));
        } else {
            wrappedProvider.setSelection(new StructuredSelection());
        }
    }

    protected abstract ISelection transform(IStructuredSelection selection);

    protected ISelection reverseTransform(IStructuredSelection selection) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
