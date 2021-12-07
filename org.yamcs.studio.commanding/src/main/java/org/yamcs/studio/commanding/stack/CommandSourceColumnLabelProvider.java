/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.stack;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class CommandSourceColumnLabelProvider extends StyledCellLabelProvider {

    private CommandStackView styleProvider;
    private Image errorImage;

    public CommandSourceColumnLabelProvider(CommandStackView styleProvider) {
        super(NO_FOCUS);
        this.styleProvider = styleProvider;
        errorImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_ERROR);
    }

    @Override
    public void update(ViewerCell cell) {
        var cmd = (StackedCommand) cell.getElement();
        var str = cmd.toStyledString(styleProvider);
        cell.setText(str.toString());
        cell.setImage(cmd.isValid() ? null : errorImage);
        cell.setStyleRanges(str.getStyleRanges());
        super.update(cell);
    }
}
