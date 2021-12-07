/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.editor.base;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ContributionItemFactory;

public class ShowViewMenu extends ContributionItem {

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public void fill(Menu menu, int index) {
        var window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        ContributionItemFactory.VIEWS_SHORTLIST.create(window).fill(menu, index);
    }
}
