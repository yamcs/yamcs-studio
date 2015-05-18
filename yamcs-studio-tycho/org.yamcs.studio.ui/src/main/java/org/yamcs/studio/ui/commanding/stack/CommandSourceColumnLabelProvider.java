package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.yamcs.xtce.Argument;

public class CommandSourceColumnLabelProvider extends StyledCellLabelProvider {

    private ResourceManager resourceManager;

    private Color errorBackgroundColor;

    private final Styler bracketStyler;
    private final Styler argNameStyler;
    private final Styler numberStyler;
    private final Styler errorStyler;

    public CommandSourceColumnLabelProvider() {
        super(NO_FOCUS | COLORS_ON_SELECTION);
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
        errorBackgroundColor = resourceManager.createColor(new RGB(255, 221, 221));

        bracketStyler = new Styler() {
            @Override
            public void applyStyles(TextStyle textStyle) {
                textStyle.font = JFaceResources.getTextFont();
            }
        };
        argNameStyler = new Styler() {
            @Override
            public void applyStyles(TextStyle textStyle) {
                textStyle.font = JFaceResources.getTextFont();
                textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
            }
        };
        numberStyler = new Styler() {
            @Override
            public void applyStyles(TextStyle textStyle) {
                textStyle.font = JFaceResources.getTextFont();
                textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
            }
        };
        errorStyler = new Styler() {
            @Override
            public void applyStyles(TextStyle textStyle) {
                textStyle.font = JFaceResources.getTextFont();
                textStyle.background = errorBackgroundColor;
                textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_RED);
            }
        };
    }

    @Override
    public void update(ViewerCell cell) {
        Telecommand cmd = (Telecommand) cell.getElement();
        StyledString str = new StyledString();
        str.append(cmd.getMetaCommand().getName());
        str.append("(", bracketStyler);
        boolean first = true;
        for (Argument arg : cmd.getMetaCommand().getArgumentList()) {
            if (!first)
                str.append(", ", bracketStyler);
            first = false;
            str.append(arg.getName() + ": ", argNameStyler);
            String value = cmd.getAssignedStringValue(arg);
            if (value == null) {
                str.append("??", errorStyler);
            } else {
                str.append(value, cmd.isValid(arg) ? numberStyler : errorStyler);
            }
        }
        str.append(")", bracketStyler);

        cell.setText(str.toString());
        cell.setStyleRanges(str.getStyleRanges());
        super.update(cell);
    }

    @Override
    public void dispose() {
        resourceManager.dispose();
        super.dispose();
    }
}
