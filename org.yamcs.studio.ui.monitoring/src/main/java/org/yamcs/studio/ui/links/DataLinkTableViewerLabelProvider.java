package org.yamcs.studio.ui.links;

import java.text.NumberFormat;
import java.util.Locale;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.yamcs.studio.core.ui.utils.UiColors;

public class DataLinkTableViewerLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider {

    private final NumberFormat numberFormatter = NumberFormat.getInstance(Locale.US);

    @Override
    public Image getColumnImage(Object arg0, int arg1) {
        // no image to show
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        // each element comes from the ContentProvider.getElements(Object)
        DataLinkRecord rec = (DataLinkRecord) element;
        switch (columnIndex) {
        case 0:
            return rec.getLinkInfo().getName();
        case 1:
            return rec.getLinkInfo().getType();
        case 2:
            return rec.getLinkInfo().getSpec();
        case 3:
            return rec.getLinkInfo().getStream();
        case 4:
            return rec.getLinkInfo().getStatus();
        case 5:
            return numberFormatter.format(rec.getLinkInfo().getDataCount());
        default:
            return "";
        }
    }

    @Override
    public Color getForeground(Object element) {
        if (index == 5) // cell status
        {
            DataLinkRecord rec = (DataLinkRecord) element;
            if (rec.getLinkInfo().getDisabled()) {
                return UiColors.DISABLED_FAINT_FG;
            } else if ("OK".equals(rec.getLinkInfo().getStatus())) {
                if (rec.isDataCountIncreasing()) {
                    return UiColors.GOOD_BRIGHT_FG;
                } else {
                    return UiColors.GOOD_FAINT_FG;
                }
            } else {
                return UiColors.ERROR_FAINT_FG;
            }
        } else {
            return null;
        }
    }

    int index = 0;
    final int nbColumn = 6;

    // This one is called for each column, with the same LinkInfo element
    @Override
    public Color getBackground(Object element) {
        if (index == nbColumn)
            index = 0;
        index++;

        if (index == 5) // cell status
        {
            DataLinkRecord rec = (DataLinkRecord) element;
            if (rec.getLinkInfo().getDisabled()) {
                return UiColors.DISABLED_FAINT_BG;
            } else if ("OK".equals(rec.getLinkInfo().getStatus())) {
                if (rec.isDataCountIncreasing()) {
                    return UiColors.GOOD_BRIGHT_BG;
                } else {
                    return UiColors.GOOD_FAINT_BG;
                }
            } else {
                return UiColors.ERROR_FAINT_BG;
            }
        } else {
            return null;
        }
    }
}
