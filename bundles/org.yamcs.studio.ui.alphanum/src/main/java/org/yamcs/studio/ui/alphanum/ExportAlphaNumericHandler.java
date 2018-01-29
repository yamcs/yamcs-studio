package org.yamcs.studio.ui.alphanum;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.studio.ui.alphanum.AlphaNumericXml.Parameter;

public class ExportAlphaNumericHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

    	// Get parameter list
    	IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        IWorkbenchPart part = window.getActivePage().findView(AlphaNumericView.ID);
        AlphaNumericView alphaNumView = (AlphaNumericView) part;
        List<ParameterInfo> pis = alphaNumView.getParameterList();
        if (pis == null || pis.isEmpty()) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Export Command Stack",
                    "Current command stack is empty. No command to export.");
            return null;
        }

        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.xml" });
        String exportFile = dialog.open();
        System.out.println("export file choosen: " + exportFile);

        if (exportFile == null) {
            // cancelled
            return null;
        }

        // Build model
        AlphaNumericXml exportAlphaNumeric = new AlphaNumericXml();
        List<Parameter> exportedParameters = exportAlphaNumeric.getParameter();
        for (ParameterInfo pi : pis) {
            Parameter p = new Parameter();
            p.setQualifiedName(pi.getQualifiedName());
            exportedParameters.add(p);
        }

        // Write model to file
        try {
            File file = new File(exportFile);
            JAXBContext jaxbContext = JAXBContext.newInstance(AlphaNumericXml.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(exportAlphaNumeric, file);
            jaxbMarshaller.marshal(exportAlphaNumeric, System.out);
        } catch (Exception e) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Export Alpha Numeric List",
                    "Unable to perform alphanumeric list export.\nDetails:" + e.getMessage());
            return null;
        }

        MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Export Alpha Numeric List", "Alpha Numeric List exported successfully.");
        return null;
    }

}
