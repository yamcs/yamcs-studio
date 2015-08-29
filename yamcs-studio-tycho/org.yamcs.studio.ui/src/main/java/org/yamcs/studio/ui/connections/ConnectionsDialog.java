package org.yamcs.studio.ui.connections;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.studio.ui.YamcsUIPlugin;

/**
 * A modal dialog for managing connection to Yamcs servers. Extracted out of preferences, because
 * these kind of settings are a lot more variable and depending on the user configuration.
 * <p>
 * The idea is that when you start Yamcs Studio for the very first time, it does not attempt any
 * connection. Auto-connect is an option.
 * <p>
 * Another thing we want to address with this dialog is to make it easy to manage different yamcs
 * servers. Especially as yamcs developers, we often have to switch from one server to another.
 * Through this dialog these settings can be managed and stored. The settings are stored using java
 * (so inside the home directory). This makes it easier to migrate from one Yamcs Studio to another.
 */
public class ConnectionsDialog extends Dialog {

    public ConnectionsDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        parent.getShell().setText("Connections");
        Composite contentArea = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.verticalSpacing = 0;
        contentArea.setLayout(gl);

        ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources(), contentArea);

        ToolBar editBar = new ToolBar(contentArea, SWT.NO_FOCUS);
        ToolItem item = new ToolItem(editBar, SWT.NONE);
        item.setImage(resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/obj16/server_add.png")));
        item.setToolTipText("Add Connection");
        item = new ToolItem(editBar, SWT.NONE);
        item.setImage(resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/obj16/server_remove.png")));
        item.setToolTipText("Delete Connection");
        editBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        SashForm sash = new SashForm(contentArea, SWT.HORIZONTAL);
        sash.setLayoutData(new GridData(GridData.FILL_BOTH));
        sash.setLayout(new FillLayout());
        createServerPanel(sash, resourceManager);
        createDetailPanel(sash, resourceManager);
        sash.setWeights(new int[] { 60, 40 });

        return contentArea;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // Change parent layout data to fill the whole bar
        parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button sampleButton = createButton(parent, IDialogConstants.NO_ID, "Sample", true);

        Label spacer = new Label(parent, SWT.NONE);
        spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Update layout of the parent composite to count the spacer
        GridLayout layout = (GridLayout) parent.getLayout();
        layout.numColumns++;
        layout.makeColumnsEqualWidth = false;

        createButton(parent, IDialogConstants.OK_ID, "Connect", false);
        createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
    }

    private Composite createServerPanel(Composite parent, ResourceManager resourceManager) {
        Composite serverPanel = new Composite(parent, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        serverPanel.setLayoutData(gd);
        TableColumnLayout tcl = new TableColumnLayout();
        serverPanel.setLayout(tcl);

        Image serverImage = resourceManager.createImage(YamcsUIPlugin.getImageDescriptor("icons/obj16/server.gif"));

        TableViewer tableViewer = new TableViewer(serverPanel, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        tableViewer.getTable().setHeaderVisible(true);
        tableViewer.getTable().setLinesVisible(false);

        TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        nameColumn.getColumn().setText("Name");
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public Image getImage(Object element) {
                return serverImage;
            }

            @Override
            public String getText(Object element) {
                YamcsConnectionProperties props = (YamcsConnectionProperties) element;
                return props.webResourceURI("").toString();
            }
        });
        tcl.setColumnData(nameColumn.getColumn(), new ColumnWeightData(400));

        tableViewer.setContentProvider(new ArrayContentProvider());
        tableViewer.setInput(new YamcsConnectionConfiguration().getConnectionPropertiesList());

        return serverPanel;
    }

    private Composite createDetailPanel(Composite parent, ResourceManager resourceManager) {
        Composite detailPanel = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.marginWidth = 0;
        gl.horizontalSpacing = 0;
        detailPanel.setLayout(gl);

        Label lbl = new Label(detailPanel, SWT.NONE);
        lbl.setText("Yamcs Instance:");
        Text yamcsInstanceText = new Text(detailPanel, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        yamcsInstanceText.setLayoutData(gd);

        lbl = new Label(detailPanel, SWT.NONE);
        lbl.setText("User:");
        Text yamcsUserText = new Text(detailPanel, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        yamcsUserText.setLayoutData(gd);

        lbl = new Label(detailPanel, SWT.PASSWORD);
        lbl.setText("Password:");
        Text yamcsPasswordText = new Text(detailPanel, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        yamcsPasswordText.setLayoutData(gd);

        // Spacer
        lbl = new Label(detailPanel, SWT.NONE);
        gd = new GridData();
        gd.horizontalSpan = 2;
        gd.widthHint = 200; // ! this influences the width of the whole dialog
        lbl.setLayoutData(gd);

        Group detailsGroup = new Group(detailPanel, SWT.NONE);
        detailsGroup.setText("Connection Details");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        detailsGroup.setLayoutData(gd);
        gl = new GridLayout(2, false);
        detailsGroup.setLayout(gl);

        lbl = new Label(detailsGroup, SWT.NONE);
        lbl.setText("Primary Server");
        gd = new GridData();
        gd.horizontalSpan = 2;
        lbl.setLayoutData(gd);

        lbl = new Label(detailsGroup, SWT.NONE);
        lbl.setText("Host:");
        Text yamcsHostText = new Text(detailsGroup, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        yamcsHostText.setLayoutData(gd);

        lbl = new Label(detailsGroup, SWT.NONE);
        lbl.setText("Port:");
        Text yamcsPortText = new Text(detailsGroup, SWT.BORDER);

        lbl = new Label(detailsGroup, SWT.HORIZONTAL | SWT.SEPARATOR);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        lbl.setLayoutData(gd);

        lbl = new Label(detailsGroup, SWT.NONE);
        lbl.setText("Failover (optional)");
        gd = new GridData();
        gd.horizontalSpan = 2;
        lbl.setLayoutData(gd);

        lbl = new Label(detailsGroup, SWT.NONE);
        lbl.setText("Host:");
        Text yamcsFailoverHostText = new Text(detailsGroup, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        yamcsFailoverHostText.setLayoutData(gd);

        lbl = new Label(detailsGroup, SWT.NONE);
        lbl.setText("Port:");
        Text yamcsFailoverPortText = new Text(detailsGroup, SWT.BORDER);

        // Spacer
        lbl = new Label(detailPanel, SWT.NONE);
        gd = new GridData();
        gd.horizontalSpan = 2;
        lbl.setLayoutData(gd);

        lbl = new Label(detailPanel, SWT.NONE);
        lbl.setText("Name:");
        gd = new GridData();
        gd.horizontalSpan = 2;
        lbl.setLayoutData(gd);

        Text nameText = new Text(detailPanel, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        nameText.setLayoutData(gd);

        Button savePasswordButton = new Button(detailPanel, SWT.CHECK);
        savePasswordButton.setText("Save Password");
        gd = new GridData();
        gd.horizontalSpan = 2;
        savePasswordButton.setLayoutData(gd);

        return detailPanel;
    }
}
