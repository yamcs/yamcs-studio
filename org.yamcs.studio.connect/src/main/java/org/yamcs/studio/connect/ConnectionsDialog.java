package org.yamcs.studio.connect;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.equinox.security.storage.provider.IProviderHints;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.yamcs.client.ClientException;
import org.yamcs.client.YamcsClient;
import org.yamcs.studio.connect.YamcsConfiguration.AuthType;

/**
 * A modal dialog for managing connection to Yamcs servers. Extracted out of preferences, because these kind of settings
 * are a lot more variable and depending on the user configuration.
 */
public class ConnectionsDialog extends Dialog {

    private static final Logger log = Logger.getLogger(ConnectionsDialog.class.getName());
    private static final String ITEM_STANDARD = "Standard";
    private static final String ITEM_KERBEROS = "Kerberos";

    private TableViewer connViewer;
    private Composite detailPanel;

    private ToolItem addServerButton;
    private ToolItem removeServerButton;
    private Button testButton;

    private YamcsConfiguration selectedConfiguration;

    private Text yamcsInstanceText;
    private Text yamcsURLText;
    private Text commentText;

    private Label yamcsUserLabel;
    private Text yamcsUserText;
    private Label yamcsPasswordLabel;

    private Composite passwordButtons;
    private Button storePasswordButton;
    private Button clearPasswordButton;

    private Text caCertFileText;
    private Combo authTypeCombo;

    private Set<YamcsConfiguration> connections = new HashSet<>();

    public ConnectionsDialog(Shell parentShell) {
        super(parentShell);

        // Allow resize
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    public void create() {
        super.create();
        getShell().setSize(800, 600);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        parent.getShell().setText("Yamcs Server Connections");

        Composite contentArea = (Composite) super.createDialogArea(parent);
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.verticalSpacing = 0;
        contentArea.setLayout(gl);

        ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources(), contentArea);

        ToolBar editBar = new ToolBar(contentArea, SWT.NO_FOCUS);
        addServerButton = new ToolItem(editBar, SWT.NONE);
        addServerButton.setImage(resourceManager
                .createImage(getImageDescriptor(ConnectionsDialog.class, "icons/obj16/server_add.png")));
        addServerButton.setToolTipText("Add Connection");
        addServerButton.addListener(SWT.Selection, evt -> {
            addServer();
            updateState();
            saveChanges();
        });

        removeServerButton = new ToolItem(editBar, SWT.NONE);
        removeServerButton.setImage(resourceManager
                .createImage(getImageDescriptor(ConnectionsDialog.class, "icons/obj16/server_remove.png")));
        removeServerButton.setToolTipText("Delete Connection");
        removeServerButton.addListener(SWT.Selection, evt -> {
            removeSelectedServer();
            updateState();
            saveChanges();
        });
        editBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        SashForm sash = new SashForm(contentArea, SWT.HORIZONTAL);
        sash.setLayoutData(new GridData(GridData.FILL_BOTH));
        sash.setLayout(new FillLayout());
        createServerPanel(sash, resourceManager);

        // Create right side, but wrap it in another composite to force
        // dimensions even when invisible
        Composite detailPanelWrapper = new Composite(sash, SWT.NONE);
        gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        detailPanelWrapper.setLayout(gl);
        createDetailPanel(detailPanelWrapper, resourceManager);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 200;
        detailPanel.setLayoutData(gd);

        sash.setWeights(new int[] { 70, 30 });

        connections.addAll(ConnectionPreferences.getConnections());
        updateState();

        String lastId = ConnectionPreferences.getLastUsedConnection();
        if (lastId != null) {
            selectConnection(lastId);
        } else {
            selectFirstConnection();
        }

        return contentArea;
    }

    private void updateState() {
        connViewer.refresh();
        IStructuredSelection sel = (IStructuredSelection) connViewer.getSelection();
        if (sel.isEmpty()) {
            selectedConfiguration = null;
            detailPanel.setVisible(false);
            removeServerButton.setEnabled(false);
            if (testButton != null) {
                testButton.setEnabled(false);
            }
        } else {
            selectedConfiguration = (YamcsConfiguration) sel.getFirstElement();
            detailPanel.setVisible(true);
            removeServerButton.setEnabled(true);
            clearPasswordButton.setEnabled(selectedConfiguration.isSecureHint());
            if (testButton != null) {
                testButton.setEnabled(true);
            }

            if (selectedConfiguration.getAuthType() == null
                    || selectedConfiguration.getAuthType() == AuthType.STANDARD) {
                yamcsUserLabel.setVisible(true);
                yamcsUserText.setVisible(true);
                yamcsPasswordLabel.setVisible(true);
                passwordButtons.setVisible(true);
            } else {
                yamcsUserLabel.setVisible(false);
                yamcsUserText.setVisible(false);
                yamcsPasswordLabel.setVisible(false);
                passwordButtons.setVisible(false);
            }
        }
    }

    private void saveChanges() {
        ConnectionPreferences.setConnections(new ArrayList<>(connections));
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label filler = new Label(parent, SWT.NONE);
        filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        GridLayout layout = (GridLayout) parent.getLayout();
        layout.numColumns++;
        layout.makeColumnsEqualWidth = false;

        testButton = createButton(parent, 124, "Test Connection", false);
        testButton.addListener(SWT.Selection, evt -> {
            if (selectedConfiguration != null) {
                testConnection(selectedConfiguration);
            }
        });
        testButton.setEnabled(!connViewer.getSelection().isEmpty());

        super.createButtonsForButtonBar(parent);

        Button ok = getButton(IDialogConstants.OK_ID);
        ok.setText("Connect");
        setButtonLayoutData(ok);

        Button cancel = getButton(IDialogConstants.CANCEL_ID);
        cancel.setText("Close"); // Because of autosave, 'Close' is more appropriate than 'Cancel'
        setButtonLayoutData(cancel);
    }

    private void testConnection(YamcsConfiguration conf) {
        YamcsClient yamcsClient = null;
        try {
            YamcsClient.Builder clientBuilder = YamcsClient
                    .newBuilder(conf.getURL())
                    .withVerifyTls(false);

            if (conf.getCaCertFile() != null) {
                clientBuilder.withCaCertFile(Paths.get(conf.getCaCertFile()));
            }
            yamcsClient = clientBuilder.build();

            if (conf.getAuthType() == AuthType.KERBEROS) {
                yamcsClient.loginWithKerberos();
                yamcsClient.connectWebSocket();
            } else if (conf.getUser() == null) {
                yamcsClient.connectWebSocket();
            } else {
                String password = null;
                if (conf.isSecureHint()) { // Avoid unnecessary prompts
                    try {
                        ISecurePreferences node = getSecureNode();
                        password = node.get(conf.getId(), null);
                    } catch (StorageException e) {
                        log.log(Level.SEVERE, "Cannot read password from secure storage", e);
                    }
                }
                if (password != null && !password.isEmpty()) {
                    yamcsClient.login(conf.getUser(), password.toCharArray());
                    yamcsClient.connectWebSocket();
                } else {
                    LoginDialog loginDialog = new LoginDialog(getShell(), conf.getURL(), conf.getUser());
                    if (loginDialog.open() == Window.OK) {
                        String username = loginDialog.getUser();
                        password = loginDialog.getPassword();
                        yamcsClient.login(username, password.toCharArray());
                        yamcsClient.connectWebSocket();
                    } else {
                        return;
                    }
                }
            }

            Display.getDefault().asyncExec(() -> {
                MessageDialog.openInformation(getShell(), "Connection OK", "Connection OK");
            });
        } catch (ClientException e) {
            Display.getDefault().asyncExec(() -> {
                MessageDialog.openError(getShell(), "Failed to connect", e.getMessage());
            });
        } finally {
            if (yamcsClient != null) {
                yamcsClient.close();
            }
        }
    }

    private ISecurePreferences getSecureNode() {
        // Disable the default behaviour of showing an
        // (annoying) dialog inviting the user to set up
        // a master password recovery hint.
        Map<String, Object> options = new HashMap<>();
        options.put(IProviderHints.PROMPT_USER, false);

        // Use Eclipse default location, then it also shows in preference dialog:
        // ~/.eclipse/org.eclipse.equinox.security/secure_storage
        try {
            ISecurePreferences preferences = SecurePreferencesFactory.open(null, options);
            return preferences.node("org.yamcs.connect/passwords");
        } catch (IOException e) {
            log.log(Level.SEVERE, "Cannot access keyring", e);
            return null;
        }
    }

    private void selectConnection(String id) {
        for (int i = 0; i < connViewer.getTable().getItemCount(); i++) {
            YamcsConfiguration configuration = (YamcsConfiguration) connViewer.getElementAt(i);
            if (configuration.getId().equals(id)) {
                connViewer.setSelection(new StructuredSelection(configuration), true);
                return;
            }
        }
    }

    private void selectFirstConnection() {
        if (connViewer.getTable().getItemCount() > 0) {
            connViewer.setSelection(new StructuredSelection(connViewer.getElementAt(0)), true);
        }
    }

    private void addServer() {
        YamcsConfiguration conf = new YamcsConfiguration();
        conf.setURL("http://localhost:8090");
        conf.setAuthType(AuthType.STANDARD);
        connections.add(conf);
        connViewer.refresh();
        connViewer.setSelection(new StructuredSelection(conf), true);
        yamcsURLText.setFocus();
    }

    private void removeSelectedServer() {
        IStructuredSelection sel = (IStructuredSelection) connViewer.getSelection();
        YamcsConfiguration conf = (YamcsConfiguration) sel.getFirstElement();
        boolean confirmed = MessageDialog.openConfirm(connViewer.getTable().getShell(), "",
                "Do you really want to remove the server configuration '" + conf + "'?");
        if (confirmed) {
            connections.remove(conf);
            connViewer.refresh();
            selectFirstConnection();
        }
    }

    private Composite createServerPanel(Composite parent, ResourceManager resourceManager) {
        Composite serverPanel = new Composite(parent, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        serverPanel.setLayoutData(gd);
        TableColumnLayout tcl = new TableColumnLayout();
        serverPanel.setLayout(tcl);

        Image serverImage = resourceManager
                .createImage(getImageDescriptor(ConnectionsDialog.class, "icons/obj16/server.gif"));

        connViewer = new TableViewer(serverPanel, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        connViewer.getTable().setHeaderVisible(true);
        connViewer.getTable().setLinesVisible(true);

        TableViewerColumn urlColumn = new TableViewerColumn(connViewer, SWT.NONE);
        urlColumn.getColumn().setText("Server URL");
        urlColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public Image getImage(Object element) {
                return serverImage;
            }

            @Override
            public String getText(Object element) {
                YamcsConfiguration conf = (YamcsConfiguration) element;
                return conf.getURL();
            }
        });
        tcl.setColumnData(urlColumn.getColumn(), new ColumnPixelData(200));

        TableViewerColumn instanceColumn = new TableViewerColumn(connViewer, SWT.NONE);
        instanceColumn.getColumn().setText("Instance");
        instanceColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                YamcsConfiguration conf = (YamcsConfiguration) element;
                return conf.getInstance();
            }
        });
        tcl.setColumnData(instanceColumn.getColumn(), new ColumnPixelData(90));

        TableViewerColumn userColumn = new TableViewerColumn(connViewer, SWT.NONE);
        userColumn.getColumn().setText("User");
        userColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                YamcsConfiguration conf = (YamcsConfiguration) element;
                if (conf.getAuthType() != AuthType.KERBEROS) {
                    return conf.getUser();
                } else {
                    return null;
                }
            }
        });
        tcl.setColumnData(userColumn.getColumn(), new ColumnPixelData(90));

        TableViewerColumn nameColumn = new TableViewerColumn(connViewer, SWT.NONE);
        nameColumn.getColumn().setText("Comment");
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                YamcsConfiguration conf = (YamcsConfiguration) element;
                return conf.getComment();
            }
        });
        tcl.setColumnData(nameColumn.getColumn(), new ColumnPixelData(200));

        IContentProvider contentProvider = (IStructuredContentProvider) inputElement -> connections.toArray();
        connViewer.setContentProvider(contentProvider);
        connViewer.setInput(contentProvider);
        connViewer.addSelectionChangedListener(evt -> {
            IStructuredSelection sel = (IStructuredSelection) evt.getSelection();
            if (sel.getFirstElement() != null) {
                YamcsConfiguration conf = (YamcsConfiguration) sel.getFirstElement();

                yamcsInstanceText.setText(forceString(conf.getInstance()));
                yamcsURLText.setText(forceString(conf.getURL()));
                /// caCertFileText.setText(forceString(conf.getCaCertFile()));
                commentText.setText(forceString(conf.getComment()));

                AuthType authType = (conf.getAuthType() == null) ? AuthType.STANDARD : conf.getAuthType();
                if (authType == AuthType.STANDARD) {
                    authTypeCombo.select(0);
                    yamcsUserText.setText(forceString(conf.getUser()));
                } else if (authType == AuthType.KERBEROS) {
                    authTypeCombo.select(1);
                    yamcsUserText.setText("");
                } else {
                    throw new IllegalArgumentException("Unexpected auth type " + authType);
                }

                updateState();
            }
        });

        connViewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object o1, Object o2) {
                YamcsConfiguration c1 = (YamcsConfiguration) o1;
                YamcsConfiguration c2 = (YamcsConfiguration) o2;
                if (c1.getURL() != null && c2.getURL() != null) {
                    return c1.getURL().compareTo(c2.getURL());
                } else {
                    return 0;
                }
            }
        });

        return serverPanel;
    }

    private Composite createDetailPanel(Composite parent, ResourceManager resourceManager) {
        detailPanel = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.marginWidth = 0;
        detailPanel.setLayout(gl);

        Label lbl = new Label(detailPanel, SWT.NONE);
        lbl.setText("Server URL:");
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        lbl.setLayoutData(gd);
        yamcsURLText = new Text(detailPanel, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        yamcsURLText.setLayoutData(gd);
        yamcsURLText.addListener(SWT.KeyUp, evt -> {
            if (!isBlank(yamcsURLText.getText()) && selectedConfiguration != null) {
                selectedConfiguration.setURL(yamcsURLText.getText());
            } else if (selectedConfiguration != null) {
                selectedConfiguration.setURL(null);
            }
            updateState();
            saveChanges();
        });

        lbl = new Label(detailPanel, SWT.NONE);
        lbl.setText("Instance:");
        gd = new GridData();
        gd.horizontalSpan = 2;
        lbl.setLayoutData(gd);
        yamcsInstanceText = new Text(detailPanel, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        yamcsInstanceText.setLayoutData(gd);
        yamcsInstanceText.addListener(SWT.KeyUp, evt -> {
            if (!isBlank(yamcsInstanceText.getText()) && selectedConfiguration != null) {
                selectedConfiguration.setInstance(yamcsInstanceText.getText());
            } else if (selectedConfiguration != null) {
                selectedConfiguration.setInstance(null);
            }
            updateState();
            saveChanges();
        });

        /*lbl = new Label(detailPanel, SWT.NONE);
        lbl.setText("CA Certificate:");
        caCertFileText = new Text(detailPanel, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        caCertFileText.setLayoutData(gd);
        caCertFileText.addListener(SWT.KeyUp, evt -> {
            if (!isBlank(caCertFileText.getText()) && selectedConfiguration != null) {
                // selectedConfiguration.setUser(caCertFileText.getText());
            } else if (selectedConfiguration != null) {
                // selectedConfiguration.setUser(null);
            }
        });*/

        lbl = new Label(detailPanel, SWT.NONE);
        lbl.setText("Comment:");
        gd = new GridData();
        gd.horizontalSpan = 2;
        lbl.setLayoutData(gd);

        commentText = new Text(detailPanel, SWT.BORDER | SWT.MULTI);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        gd.heightHint = 32;
        commentText.setLayoutData(gd);
        // Update the label in the left panel too
        commentText.addListener(SWT.KeyUp, evt -> {
            if (!isBlank(commentText.getText()) && selectedConfiguration != null) {
                selectedConfiguration.setComment(commentText.getText());
            } else if (selectedConfiguration != null) {
                selectedConfiguration.setComment(null);
            }

            updateState();
            saveChanges();
        });

        // Spacer
        lbl = new Label(detailPanel, SWT.NONE);
        gd = new GridData();
        gd.horizontalSpan = 2;
        lbl.setLayoutData(gd);

        lbl = new Label(detailPanel, SWT.NONE);
        lbl.setText("Authentication:");
        gd = new GridData();
        gd.horizontalSpan = 2;
        lbl.setLayoutData(gd);

        lbl = new Label(detailPanel, SWT.NONE);
        lbl.setText("Type:");
        authTypeCombo = new Combo(detailPanel, SWT.READ_ONLY);
        authTypeCombo.setItems(ITEM_STANDARD, ITEM_KERBEROS);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        authTypeCombo.setLayoutData(gd);
        authTypeCombo.addListener(SWT.Selection, evt -> {
            if (authTypeCombo.getSelectionIndex() == 0) {
                selectedConfiguration.setAuthType(AuthType.STANDARD);
            } else if (authTypeCombo.getSelectionIndex() == 1) {
                selectedConfiguration.setAuthType(AuthType.KERBEROS);
            } else {
                throw new IllegalArgumentException("Unexpected auth type " + authTypeCombo.getSelectionIndex());
            }
            updateState();
            saveChanges();
        });

        yamcsUserLabel = new Label(detailPanel, SWT.NONE);
        yamcsUserLabel.setText("User:");
        yamcsUserText = new Text(detailPanel, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        yamcsUserText.setLayoutData(gd);
        yamcsUserText.addListener(SWT.KeyUp, evt -> {
            if (!isBlank(yamcsUserText.getText()) && selectedConfiguration != null) {
                selectedConfiguration.setUser(yamcsUserText.getText());
            } else if (selectedConfiguration != null) {
                selectedConfiguration.setUser(null);
            }
            updateState();
            saveChanges();
        });

        yamcsPasswordLabel = new Label(detailPanel, SWT.NONE);
        yamcsPasswordLabel.setText("Password:");

        passwordButtons = new Composite(detailPanel, SWT.NONE);
        detailPanel.setLayoutData(new GridData());
        gl = new GridLayout(3, false);
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        passwordButtons.setLayout(gl);

        storePasswordButton = new Button(passwordButtons, SWT.PUSH);
        storePasswordButton.setText("Store ...");
        gd = new GridData();
        storePasswordButton.setLayoutData(gd);
        storePasswordButton.addListener(SWT.Selection, evt -> {
            if (isBlank(yamcsUserText.getText())) {
                MessageDialog.openError(getShell(), "Cannot set password", "Please fill the user first");
            } else {
                StorePasswordDialog dialog = new StorePasswordDialog(getShell(), selectedConfiguration);
                if (dialog.open() == Window.OK) {
                    String password = dialog.getPassword();
                    try {
                        ISecurePreferences node = getSecureNode();
                        node.put(selectedConfiguration.getId(), password, true);
                        node.flush();
                        selectedConfiguration.setSecureHint(true);
                        updateState();
                        saveChanges();
                    } catch (StorageException | IOException e) {
                        log.log(Level.SEVERE, "Error while saving password", e);
                        MessageDialog.openError(getShell(), "Error while saving password", e.getMessage());
                    }
                }
            }
        });

        clearPasswordButton = new Button(passwordButtons, SWT.PUSH);
        clearPasswordButton.setText("Clear");
        gd = new GridData();
        clearPasswordButton.setLayoutData(gd);
        clearPasswordButton.addListener(SWT.Selection, evt -> {
            try {
                ISecurePreferences node = getSecureNode();
                node.remove(selectedConfiguration.getId());
                node.flush();
                selectedConfiguration.setSecureHint(false);
                updateState();
                saveChanges();
            } catch (IOException e) {
                log.log(Level.SEVERE, "Error while clearing password", e);
                MessageDialog.openError(getShell(), "Error while clearing password", e.getMessage());
            }
        });

        Label stretch = new Label(passwordButtons, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        stretch.setLayoutData(gd);

        return detailPanel;
    }

    @Override
    protected void okPressed() {
        if (selectedConfiguration.getUser() != null) {
            String password = null;
            if (selectedConfiguration.isSecureHint()) { // Avoid unnecessary prompts
                try {
                    ISecurePreferences node = getSecureNode();
                    password = node.get(selectedConfiguration.getId(), null);
                } catch (StorageException e) {
                    log.log(Level.SEVERE, "Cannot read password from secure storage", e);
                }
            }
            if (password != null && !password.isEmpty()) {
                selectedConfiguration.setTransientPassword(password);
            } else {
                LoginDialog loginDialog = new LoginDialog(getShell(), selectedConfiguration.getURL(),
                        selectedConfiguration.getUser());
                if (loginDialog.open() == Window.OK) {
                    yamcsUserText.setText(loginDialog.getUser());
                    selectedConfiguration.setUser(loginDialog.getUser());
                    selectedConfiguration.setTransientPassword(loginDialog.getPassword());
                    updateState();
                    saveChanges();
                } else {
                    return;
                }
            }
        }
        super.okPressed();
    }

    public YamcsConfiguration getSelectedConfiguration() {
        return selectedConfiguration;
    }

    public static ImageDescriptor getImageDescriptor(Class<?> classFromBundle, String path) {
        Bundle bundle = FrameworkUtil.getBundle(classFromBundle);
        return ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path(path), null));
    }

    private static boolean isBlank(String string) {
        return string == null || "".equals(string);
    }

    private static String forceString(Object obj) {
        return (obj != null) ? obj.toString() : "";
    }
}
