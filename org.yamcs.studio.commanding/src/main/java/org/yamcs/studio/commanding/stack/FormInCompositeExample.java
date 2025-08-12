package org.yamcs.studio.commanding.stack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class FormInCompositeExample {
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());

        // Create the parent composite
        Composite parent = new Composite(shell, SWT.NONE);
        parent.setLayout(new FillLayout());

        // Initialize FormToolkit
        FormToolkit toolkit = new FormToolkit(parent.getDisplay());

        // Create ScrolledForm
        ScrolledForm form = toolkit.createScrolledForm(parent);
        form.setText("User Information");

        // Configure the form body
        Composite body = form.getBody();
        TableWrapLayout layout = new TableWrapLayout();
        layout.numColumns = 2;
        body.setLayout(layout);

        // Add form widgets
        toolkit.createLabel(body, "Name:");
        Text nameText = toolkit.createText(body, "");
        nameText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

        toolkit.createLabel(body, "Email:");
        Text emailText = toolkit.createText(body, "");
        emailText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

        // Add a section
        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        section.setText("Details");
        TableWrapData sectionData = new TableWrapData(TableWrapData.FILL_GRAB);
        sectionData.colspan = 2;
        section.setLayoutData(sectionData);

        Composite sectionClient = toolkit.createComposite(section);
        sectionClient.setLayout(new TableWrapLayout());
        toolkit.createLabel(sectionClient, "Additional Information");
        section.setClient(sectionClient);

        // Open the shell
        shell.setSize(400, 300);
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        toolkit.dispose();
        display.dispose();
    }
}
