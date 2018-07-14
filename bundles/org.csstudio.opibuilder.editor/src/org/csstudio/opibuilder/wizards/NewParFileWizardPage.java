package org.csstudio.opibuilder.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.csstudio.opibuilder.model.DisplayModel;
import org.csstudio.opibuilder.persistence.XMLUtil;
import org.csstudio.opibuilder.util.SchemaService;
import org.csstudio.ui.util.wizards.WizardNewFileCreationPage;
import org.eclipse.jface.viewers.IStructuredSelection;

public class NewParFileWizardPage extends WizardNewFileCreationPage {

    public NewParFileWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName, selection);
        setTitle("Create a new Parameter Table");
        setDescription("Create a new Parameter Table in the selected project or folder.");
    }

    @Override
    protected InputStream getInitialContents() {
        DisplayModel displayModel = new DisplayModel();
        SchemaService.getInstance().applySchema(displayModel);
        String s = XMLUtil.widgetToXMLString(displayModel, true);
        InputStream result = new ByteArrayInputStream(s.getBytes());
        return result;
    }


    @Override
    protected String getNewFileLabel() {
        return "File Name:";
    }

    @Override
    public String getFileExtension() {
        return "par"; //TODO get from
    }
}
