package org.yamcs.studio.ui.alphanum;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

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
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.ui.alphanum.AlphaNumericXml.Parameter;

public class ImportAlphaNumericHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ImportAlphaNumericHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
        dialog.setFilterExtensions(new String[] { "*.xml" });
        String importFile = dialog.open();
        if (importFile == null) {
            // cancelled
            return null;
        }
        log.log(Level.INFO, "Importing alpha numeric parameter list from file: " + importFile);

        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        IWorkbenchPart part = window.getActivePage().findView(AlphaNumericView.ID);
        AlphaNumericView alphanumericView = (AlphaNumericView) part;
        alphanumericView.clear();
        alphanumericView.addParameters(parseParameterList(importFile));

        return null;
    }

    public List<ParameterInfo> parseParameterList(String fileName) {
        try {
            final JAXBContext jc = JAXBContext.newInstance(AlphaNumericXml.class);
            final Unmarshaller unmarshaller = jc.createUnmarshaller();
            final AlphaNumericXml parameterList = (AlphaNumericXml) unmarshaller
                    .unmarshal(new FileReader(fileName));

            List<ParameterInfo> importedList = new LinkedList<ParameterInfo>();
            for (Parameter p : parameterList.getParameter()) {

                ParameterInfo pi = getParameterInfo(p.getQualifiedName());
                if (pi == null) {
                    MessageDialog.openError(Display.getCurrent().getActiveShell(), "Import Parameter List",
                            "Parameter " + p.getQualifiedName() + " does not exist in MDB.");
                    return null;
                }
                importedList.add(pi);
            }

            return importedList;

        } catch (Exception e) {
            log.log(Level.SEVERE, "Unable to load alphanumeric parameter list for importation. Check the XML file is correct. Details:\n"
                    + e.toString());
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Import Parameter List",
                    "Unable to load alphanumeric parameter list for importation. Check the XML file is correct. Details:\n"
                            + e.toString());

            return new ArrayList<>();
        }
    }
    
    private ParameterInfo getParameterInfo(String qualifiedName) {
    	for(ParameterInfo info : ParameterCatalogue.getInstance().getMetaParameters())
    		if(qualifiedName.equals(info.getQualifiedName()))
    			return info;
    	return null;
    }

}
