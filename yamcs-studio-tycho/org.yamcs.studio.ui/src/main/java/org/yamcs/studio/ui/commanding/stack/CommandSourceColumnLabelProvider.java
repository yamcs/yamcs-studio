package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;

public class CommandSourceColumnLabelProvider extends StyledCellLabelProvider {

    private CommandStackView styleProvider;

    public CommandSourceColumnLabelProvider(CommandStackView styleProvider) {
        super(NO_FOCUS);
        this.styleProvider = styleProvider;
    }

    @Override
    public void update(ViewerCell cell) {
        StackedCommand cmd = (StackedCommand) cell.getElement();
        StyledString str = cmd.toStyledString(styleProvider);
        cell.setText(str.toString());
        cell.setStyleRanges(str.getStyleRanges());
        super.update(cell);
    }
}
