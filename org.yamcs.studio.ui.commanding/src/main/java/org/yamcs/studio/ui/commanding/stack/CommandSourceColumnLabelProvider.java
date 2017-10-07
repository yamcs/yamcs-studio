package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
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
        StackedCommand cmd = (StackedCommand) cell.getElement();
        StyledString str = cmd.toStyledString(styleProvider);
        cell.setText(str.toString());
        cell.setImage(cmd.isValid() ? null : errorImage);
        cell.setStyleRanges(str.getStyleRanges());
        super.update(cell);
    }
}
