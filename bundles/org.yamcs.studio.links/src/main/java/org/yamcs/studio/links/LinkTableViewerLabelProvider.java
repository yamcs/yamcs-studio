package org.yamcs.studio.links;

import java.text.NumberFormat;
import java.util.Locale;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.yamcs.studio.core.utils.UiColors;

public class LinkTableViewerLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider {

    private final NumberFormat numberFormatter = NumberFormat.getInstance(Locale.US);

    @Override
    public Image getColumnImage(Object arg0, int arg1) {
        // no image to show
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        // each element comes from the ContentProvider.getElements(Object)
        LinkRecord rec = (LinkRecord) element;
        switch (columnIndex) {
        case 0:
            return rec.getLinkInfo().getName();
        case 1:
            return rec.getLinkInfo().getType();
        case 2:
            return rec.getLinkInfo().getStatus();
        case 3:
            return numberFormatter.format(rec.getLinkInfo().getDataInCount());
        case 4:
            return numberFormatter.format(rec.getLinkInfo().getDataOutCount());
        default:
            return "";
        }
    }

    @Override
    public Color getForeground(Object element) {
        if (index == 4) {
            LinkRecord rec = (LinkRecord) element;
            if (rec.getLinkInfo().getDisabled()) {
                return UiColors.DISABLED_FAINT_FG;
            } else if ("OK".equals(rec.getLinkInfo().getStatus())) {
                if (rec.isDataInCountIncreasing()) {
                    return UiColors.GOOD_BRIGHT_FG;
                } else {
                    return UiColors.GOOD_FAINT_FG;
                }
            } else {
                return UiColors.ERROR_FAINT_FG;
            }
        } else if (index == 5) {
            LinkRecord rec = (LinkRecord) element;
            if (rec.getLinkInfo().getDisabled()) {
                return UiColors.DISABLED_FAINT_FG;
            } else if ("OK".equals(rec.getLinkInfo().getStatus())) {
                if (rec.isDataOutCountIncreasing()) {
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
    final int nbColumn = 5;

    // This one is called for each column, with the same LinkInfo element
    @Override
    public Color getBackground(Object element) {
        if (index == nbColumn) {
            index = 0;
        }
        index++;

        if (index == 4) { // cell status
            LinkRecord rec = (LinkRecord) element;
            if (rec.getLinkInfo().getDisabled()) {
                return UiColors.DISABLED_FAINT_BG;
            } else if ("OK".equals(rec.getLinkInfo().getStatus())) {
                if (rec.isDataInCountIncreasing()) {
                    return UiColors.GOOD_BRIGHT_BG;
                } else {
                    return UiColors.GOOD_FAINT_BG;
                }
            } else {
                return UiColors.ERROR_FAINT_BG;
            }
        } else if (index == 5) {
            LinkRecord rec = (LinkRecord) element;
            if (rec.getLinkInfo().getDisabled()) {
                return UiColors.DISABLED_FAINT_BG;
            } else if ("OK".equals(rec.getLinkInfo().getStatus())) {
                if (rec.isDataInCountIncreasing()) {
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
