package org.yamcs.studio.core.ui.connections;

import static org.yamcs.studio.core.ui.utils.TextUtils.forceString;
import static org.yamcs.studio.core.ui.utils.TextUtils.isBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
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
import org.yamcs.studio.core.security.YamcsCredentials;
import org.yamcs.studio.core.ui.utils.RCPUtils;

/**
 * A modal dialog for managing connection to Yamcs servers. Extracted out of
 * preferences, because these kind of settings are a lot more variable and
 * depending on the user configuration.
 * <p>
 * The idea is that when you start Yamcs Studio for the very first time, it does
 * not attempt any connection. Auto-connect is an option.
 * <p>
 * Another thing we want to address with this dialog is to make it easy to
 * manage different yamcs servers. Especially as yamcs developers, we often have
 * to switch from one server to another. Through this dialog these settings can
 * be managed and stored. The settings are stored using java (so inside the home
 * directory). This makes it easier to migrate from one Yamcs Studio to another.
 */
public class ConnectionsDialog extends Dialog {

    private static final Logger log = Logger.getLogger(ConnectionsDialog.class.getName());

    private TableViewer connViewer;
    private Composite detailPanel;

    private ToolItem addServerButton;
    private ToolItem removeServerButton;

    private YamcsConfiguration selectedConfiguration;

    private Button autoConnect;
    private Text yamcsInstanceText;
    private Text yamcsUserText;
    private Text yamcsPasswordText;
    private Text yamcsPrimaryHostText;
    private Text yamcsPrimaryPortText;
    private Text yamcsFailoverHostText;
    private Text yamcsFailoverPortText;
    private Text nameText;
    private Button savePasswordButton;

    private YamcsConfiguration chosenConfiguration;
    private YamcsCredentials chosenCredentials;

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
        addServerButton = new ToolItem(editBar, SWT.NONE);
        addServerButton.setImage(resourceManager
                .createImage(RCPUtils.getImageDescriptor(ConnectionsDialog.class, "icons/obj16/server_add.png")));
        addServerButton.setToolTipText("Add Connection");
        addServerButton.addListener(SWT.Selection, evt -> {
            addServer();
            updateState();
        });

        removeServerButton = new ToolItem(editBar, SWT.NONE);
        removeServerButton.setImage(resourceManager
                .createImage(RCPUtils.getImageDescriptor(ConnectionsDialog.class, "icons/obj16/server_remove.png")));
        removeServerButton.setToolTipText("Delete Connection");
        removeServerButton.addListener(SWT.Selection, evt -> {
            removeSelectedServer();
            updateState();
        });
        editBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        SashForm sash = new SashForm(contentArea, SWT.HORIZONTAL);
        sash.setLayoutData(new GridData(GridData.FILL_BOTH));
        sash.setLayout(new FillLayout());
        createServerPanel(sash, resourceManager);

        // Create right side, but wrap it in another composite to force
        // dimensions
        // even when invisible
        Composite detailPanelWrapper = new Composite(sash, SWT.NONE);
        gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        detailPanelWrapper.setLayout(gl);
        createDetailPanel(detailPanelWrapper, resourceManager);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 250;
        detailPanel.setLayoutData(gd);

        sash.setWeights(new int[] { 55, 45 });

        ConnectionPreferences.getConfigurations().forEach(conf -> {
            connViewer.add(conf);
        });

        YamcsConfiguration lastConf = ConnectionPreferences.getLastUsedConfiguration();
        if (lastConf != null)
            selectServer(lastConf);
        else
            selectFirstServer();
        updateState();

        return contentArea;
    }

    private void updateState() {
        IStructuredSelection sel = (IStructuredSelection) connViewer.getSelection();
        Button ok = getButton(IDialogConstants.OK_ID);
        if (sel.isEmpty()) {
            selectedConfiguration = null;
            detailPanel.setVisible(false);
            removeServerButton.setEnabled(false);
            if (ok != null) // It's null during initial creation
                ok.setText("Save"); // Give opportunity to user to quit dialog
                                    // without connecting and saving changes
        } else {
            selectedConfiguration = (YamcsConfiguration) sel.getFirstElement();
            detailPanel.setVisible(true);
            removeServerButton.setEnabled(true);
            if (ok != null) // It's null during initial creation
                ok.setText("Connect");
        }
    }

    @Override
    protected void okPressed() {
        if (selectedConfiguration != null) {
            chosenConfiguration = selectedConfiguration;
            chosenCredentials = selectedConfiguration.toYamcsCredentials();
        }
        ConnectionPreferences.setAutoConnect(autoConnect.getSelection());
        List<YamcsConfiguration> confs = new ArrayList<>();
        Object el;
        int i = 0;
        while ((el = connViewer.getElementAt(i++)) != null) {
            YamcsConfiguration conf = (YamcsConfiguration) el;
            if (!conf.isSavePassword())
                conf.setPassword(null);
            confs.add(conf);
        }
        ConnectionPreferences.setConfigurations(confs);
        super.okPressed();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        autoConnect = new Button(parent, SWT.CHECK);
        autoConnect.setText("Connect on startup");
        autoConnect.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        autoConnect.setSelection(ConnectionPreferences.isAutoConnect());

        GridLayout layout = (GridLayout) parent.getLayout();
        layout.numColumns++;
        layout.makeColumnsEqualWidth = false;

        super.createButtonsForButtonBar(parent);

        Button ok = getButton(IDialogConstants.OK_ID);
        ok.setText("Connect");
        setButtonLayoutData(ok);
    }

    private void selectServer(YamcsConfiguration conf) {
        connViewer.setSelection(new StructuredSelection(conf), true);
    }

    private void selectFirstServer() {
        if (connViewer.getTable().getItemCount() > 0) {
            connViewer.setSelection(new StructuredSelection(connViewer.getElementAt(0)), true);
        }
    }

    private void addServer() {
        YamcsConfiguration conf = new YamcsConfiguration();
        conf.setName("Untitled");
        conf.setPrimaryPort(8090);
        connViewer.add(conf);
        connViewer.setSelection(new StructuredSelection(conf), true);
        yamcsInstanceText.setFocus();
    }

    private void removeSelectedServer() {
        IStructuredSelection sel = (IStructuredSelection) connViewer.getSelection();
        YamcsConfiguration conf = (YamcsConfiguration) sel.getFirstElement();
        boolean confirmed = MessageDialog.openConfirm(connViewer.getTable().getShell(), "",
                "Do you really want to remove the server configuration '" + conf.getName() + "'?");
        if (confirmed) {
            connViewer.remove(conf);
            selectFirstServer();
        }
    }

    private Composite createServerPanel(Composite parent, ResourceManager resourceManager) {
        Composite serverPanel = new Composite(parent, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        serverPanel.setLayoutData(gd);
        TableColumnLayout tcl = new TableColumnLayout();
        serverPanel.setLayout(tcl);

        Image serverImage = resourceManager
                .createImage(RCPUtils.getImageDescriptor(ConnectionsDialog.class, "icons/obj16/server.gif"));

        connViewer = new TableViewer(serverPanel, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        connViewer.getTable().setHeaderVisible(true);
        connViewer.getTable().setLinesVisible(false);

        TableViewerColumn nameColumn = new TableViewerColumn(connViewer, SWT.NONE);
        nameColumn.getColumn().setText("Name");
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public Image getImage(Object element) {
                return serverImage;
            }

            @Override
            public String getText(Object element) {
                YamcsConfiguration conf = (YamcsConfiguration) element;
                return conf.getName();
            }
        });
        tcl.setColumnData(nameColumn.getColumn(), new ColumnWeightData(400));

        connViewer.setContentProvider(new ArrayContentProvider());
        connViewer.addSelectionChangedListener(evt -> {
            IStructuredSelection sel = (IStructuredSelection) evt.getSelection();
            if (sel.getFirstElement() != null) {
                YamcsConfiguration conf = (YamcsConfiguration) sel.getFirstElement();
                yamcsInstanceText.setText(forceString(conf.getInstance()));
                yamcsUserText.setText(forceString(conf.getUser()));
                yamcsPasswordText.setText(forceString(conf.getPassword()));
                savePasswordButton.setSelection(conf.isSavePassword());
                yamcsPrimaryHostText.setText(forceString(conf.getPrimaryHost()));
                yamcsPrimaryPortText.setText(forceString(conf.getPrimaryPort()));
                yamcsFailoverHostText.setText(forceString(conf.getFailoverHost()));
                yamcsFailoverPortText.setText(forceString(conf.getFailoverPort()));
                nameText.setText(forceString(conf.getName()));

                updateState();
            }
        });

        connViewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object o1, Object o2) {
                YamcsConfiguration c1 = (YamcsConfiguration) o1;
                YamcsConfiguration c2 = (YamcsConfiguration) o2;
                if (c1.getName() != null && c2.getName() != null)
                    return c1.getName().compareTo(c2.getName());
                else
                    return 0;
            }
        });

        return serverPanel;
    }

    private Composite createDetailPanel(Composite parent, ResourceManager resourceManager) {
        detailPanel = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.marginWidth = 0;
        gl.horizontalSpacing = 0;
        detailPanel.setLayout(gl);

        Label lbl = new Label(detailPanel, SWT.NONE);
        lbl.setText("Yamcs Instance:");
        yamcsInstanceText = new Text(detailPanel, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        yamcsInstanceText.setLayoutData(gd);
        yamcsInstanceText.addListener(SWT.KeyUp, evt -> {
            if (!isBlank(yamcsInstanceText.getText()) && selectedConfiguration != null) {
                selectedConfiguration.setInstance(yamcsInstanceText.getText());
            } else if (selectedConfiguration != null) {
                selectedConfiguration.setInstance(null);
            }
        });

        lbl = new Label(detailPanel, SWT.NONE);
        lbl.setText("User:");
        yamcsUserText = new Text(detailPanel, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        yamcsUserText.setLayoutData(gd);
        yamcsUserText.addListener(SWT.KeyUp, evt -> {
            if (!isBlank(yamcsUserText.getText()) && selectedConfiguration != null) {
                selectedConfiguration.setUser(yamcsUserText.getText());
            } else if (selectedConfiguration != null) {
                selectedConfiguration.setUser(null);
            }
        });

        lbl = new Label(detailPanel, SWT.NONE);
        lbl.setText("Password:");
        yamcsPasswordText = new Text(detailPanel, SWT.BORDER | SWT.PASSWORD);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        yamcsPasswordText.setLayoutData(gd);
        yamcsPasswordText.addListener(SWT.KeyUp, evt -> {
            if (!isBlank(yamcsPasswordText.getText()) && selectedConfiguration != null) {
                selectedConfiguration.setPassword(yamcsPasswordText.getText());
            } else if (selectedConfiguration != null) {
                selectedConfiguration.setPassword(null);
            }
        });

        // Spacer
        lbl = new Label(detailPanel, SWT.NONE);
        gd = new GridData();
        gd.horizontalSpan = 2;
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
        yamcsPrimaryHostText = new Text(detailsGroup, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        yamcsPrimaryHostText.setLayoutData(gd);
        yamcsPrimaryHostText.addListener(SWT.KeyUp, evt -> {
            if (!isBlank(yamcsPrimaryHostText.getText()) && selectedConfiguration != null) {
                selectedConfiguration.setPrimaryHost(yamcsPrimaryHostText.getText());
            } else if (selectedConfiguration != null) {
                selectedConfiguration.setPrimaryHost(null);
            }
        });

        lbl = new Label(detailsGroup, SWT.NONE);
        lbl.setText("Port:");
        yamcsPrimaryPortText = new Text(detailsGroup, SWT.BORDER);
        gd = new GridData();
        gd.widthHint = 50;
        yamcsPrimaryPortText.setLayoutData(gd);
        yamcsPrimaryPortText.addListener(SWT.KeyUp, evt -> {
            if (!isBlank(yamcsPrimaryPortText.getText()) && selectedConfiguration != null) {
                try {
                    int d = Integer.parseInt(yamcsPrimaryPortText.getText());
                    selectedConfiguration.setPrimaryPort(d);
                } catch (NumberFormatException e) {
                    log.warning("Ignoring invalid number for primary port " + yamcsPrimaryPortText.getText());
                    selectedConfiguration.setPrimaryPort(null);
                }
            } else if (selectedConfiguration != null) {
                selectedConfiguration.setPrimaryPort(null);
            }
        });

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
        yamcsFailoverHostText = new Text(detailsGroup, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        yamcsFailoverHostText.setLayoutData(gd);
        yamcsFailoverHostText.addListener(SWT.KeyUp, evt -> {
            if (!isBlank(yamcsFailoverHostText.getText()) && selectedConfiguration != null) {
                selectedConfiguration.setFailoverHost(yamcsFailoverHostText.getText());
            } else if (selectedConfiguration != null) {
                selectedConfiguration.setFailoverHost(null);
            }
        });

        lbl = new Label(detailsGroup, SWT.NONE);
        lbl.setText("Port:");
        yamcsFailoverPortText = new Text(detailsGroup, SWT.BORDER);
        gd = new GridData();
        gd.widthHint = 50;
        yamcsFailoverPortText.setLayoutData(gd);
        yamcsFailoverPortText.addListener(SWT.KeyUp, evt -> {
            if (!isBlank(yamcsFailoverPortText.getText()) && selectedConfiguration != null) {
                try {
                    int d = Integer.parseInt(yamcsFailoverPortText.getText());
                    selectedConfiguration.setFailoverPort(d);
                } catch (NumberFormatException e) {
                    log.warning("Ignoring invalid number for failover port " + yamcsFailoverPortText.getText());
                    selectedConfiguration.setFailoverPort(null);
                }
            } else if (selectedConfiguration != null) {
                selectedConfiguration.setFailoverPort(null);
            }
        });

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

        nameText = new Text(detailPanel, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        nameText.setLayoutData(gd);
        // Update the label in the left panel too
        nameText.addListener(SWT.KeyUp, evt -> {
            IStructuredSelection sel = (IStructuredSelection) connViewer.getSelection();
            YamcsConfiguration conf = (YamcsConfiguration) sel.getFirstElement();
            conf.setName(nameText.getText());
            connViewer.update(conf, null);

            if (!isBlank(nameText.getText()) && selectedConfiguration != null) {
                System.out.println("Storing name " + nameText.getText());
                selectedConfiguration.setName(nameText.getText());
            } else if (selectedConfiguration != null) {
                selectedConfiguration.setName(null);
            }
        });

        savePasswordButton = new Button(detailPanel, SWT.CHECK);
        savePasswordButton.setText("Save Password");
        gd = new GridData();
        gd.horizontalSpan = 2;
        savePasswordButton.setLayoutData(gd);
        savePasswordButton.addListener(SWT.Selection, evt -> {
            selectedConfiguration.setSavePassword(savePasswordButton.getSelection());
        });

        return detailPanel;
    }

    public YamcsConfiguration getChosenConfiguration() {
        // Add our credentials back in (they could have been removed during
        // serialization)
        if (chosenConfiguration != null && chosenCredentials != null) {
            chosenConfiguration.setUser(chosenCredentials.getUsername());
            chosenConfiguration.setPassword(chosenCredentials.getPasswordS());
        }
        return chosenConfiguration;
    }
}
