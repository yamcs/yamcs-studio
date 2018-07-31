package org.yamcs.studio.editor.base.views;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class ImportXTCEFileHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ImportXTCEFileHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);

        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterExtensions(new String[] { "*.xml" });
        String path = dialog.open();
        if (path == null) { // cancelled
            return null;
        }

        log.log(Level.FINE, "Importing XTCE from " + path);
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        ParametersView view = (ParametersView) part;
        try {
            view.importFile(new File(path));
        } catch (XMLStreamException e) {
            throw new ExecutionException("Failed to import XTCE file", e);
        } catch (IOException e) {
            throw new ExecutionException("Failed to import XTCE file", e);
        }

        return null;
    }
}
