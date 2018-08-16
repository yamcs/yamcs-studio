package org.yamcs.studio.customscript;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class MyScriptUtil {

    public static void helloWorld() {
        MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Hello", "Hello World!");
    }
}
