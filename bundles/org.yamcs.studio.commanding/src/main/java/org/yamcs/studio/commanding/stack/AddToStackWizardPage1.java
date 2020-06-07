package org.yamcs.studio.commanding.stack;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.protobuf.Mdb.SignificanceInfo;
import org.yamcs.protobuf.Mdb.SignificanceInfo.SignificanceLevelType;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.ui.XtceSubSystemNode;
import org.yamcs.studio.core.ui.utils.CenteredImageLabelProvider;
import org.yamcs.studio.core.ui.utils.RCPUtils;

public class AddToStackWizardPage1 extends WizardPage {

    public static final String COL_PATH = "Command";
    public static final String COL_SIGN = "Sig.";

    private Image level0Image;
    private Image level1Image;
    private Image level2Image;
    private Image level3Image;
    private Image level4Image;
    private Image level5Image;

    private StackedCommand command;
    private TreeViewer commandsTreeTable;
    private TreeColumnLayout tcl;

    private List<String> namespaces = new ArrayList<>();

    public AddToStackWizardPage1(StackedCommand command) {
        super("Choose a Command");
        this.command = command;
        setTitle("Choose a Command");
        setPageComplete(false);
    }

    // Add dynamically columns for each alias of a command
    private void addAliasColumn(String namespace) {

        TreeViewerColumn aliasColumn = new TreeViewerColumn(commandsTreeTable, SWT.NONE);
        aliasColumn.getColumn().setText(namespace);

        aliasColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof XtceCommandNode) {
                    XtceCommandNode node = (XtceCommandNode) element;
                    CommandInfo cmd = node.getCommandInfo();
                    List<NamedObjectId> aliases = cmd.getAliasList();
                    for (NamedObjectId aliase : aliases) {
                        if (aliase.getNamespace().equals(namespace)) {
                            return aliase.getName();
                        }
                    }
                }
                return "";
            }
        });
        tcl.setColumnData(aliasColumn.getColumn(), new ColumnPixelData(200));
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        composite.setLayout(gl);

        // grid for expand+filter box
        GridLayout gl2 = new GridLayout(3, false);
        gl2.marginHeight = 0;
        gl2.marginWidth = 0;
        Composite row = new Composite(composite, SWT.NONE);
        row.setLayout(gl2);
        row.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // add expand all / collapse all button
        Button expandAll = new Button(row, SWT.ARROW | SWT.DOWN);
        GridData expandAllData = new GridData(SWT.NONE);
        expandAll.setLayoutData(expandAllData);
        expandAll.setText("Expand All");
        expandAll.setToolTipText("Expand All");
        expandAll.setVisible(false);
        expandAllData.exclude = true;

        Button collapseAll = new Button(row, SWT.ARROW | SWT.RIGHT);
        GridData collapseAllData = new GridData(SWT.NONE);
        collapseAll.setLayoutData(collapseAllData);
        collapseAll.setText("Collapse All");
        collapseAll.setToolTipText("Collapse All");
        collapseAll.setVisible(true);

        // add filter box
        Text searchbox = new Text(row, SWT.SEARCH | SWT.BORDER | SWT.ICON_CANCEL);
        searchbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        expandAll.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                commandsTreeTable.expandAll();
                expandAll.setVisible(false);
                collapseAll.setVisible(true);
                expandAllData.exclude = true;
                collapseAllData.exclude = false;
                expandAll.pack();
                collapseAll.pack();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                commandsTreeTable.expandAll();
                expandAll.setVisible(false);
                collapseAll.setVisible(true);
                expandAllData.exclude = true;
                collapseAllData.exclude = false;
                expandAll.pack();
                collapseAll.pack();

            }
        });
        collapseAll.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                commandsTreeTable.collapseAll();
                expandAll.setVisible(true);
                collapseAll.setVisible(false);
                expandAllData.exclude = false;
                collapseAllData.exclude = true;
                expandAll.pack();
                collapseAll.pack();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                commandsTreeTable.collapseAll();
                expandAll.setVisible(true);
                collapseAll.setVisible(false);
                expandAllData.exclude = false;
                collapseAllData.exclude = true;
                expandAll.pack();
                collapseAll.pack();
            }
        });

        // build tree table
        ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources(), composite);
        level0Image = resourceManager
                .createImage(RCPUtils.getImageDescriptor(AddToStackWizardPage1.class, "icons/level0s.png"));
        level1Image = resourceManager
                .createImage(RCPUtils.getImageDescriptor(AddToStackWizardPage1.class, "icons/level1s.png"));
        level2Image = resourceManager
                .createImage(RCPUtils.getImageDescriptor(AddToStackWizardPage1.class, "icons/level2s.png"));
        level3Image = resourceManager
                .createImage(RCPUtils.getImageDescriptor(AddToStackWizardPage1.class, "icons/level3s.png"));
        level4Image = resourceManager
                .createImage(RCPUtils.getImageDescriptor(AddToStackWizardPage1.class, "icons/level4s.png"));
        level5Image = resourceManager
                .createImage(RCPUtils.getImageDescriptor(AddToStackWizardPage1.class, "icons/level5s.png"));

        Composite treeWrapper = new Composite(composite, SWT.NONE);
        tcl = new TreeColumnLayout();
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 800;
        gd.heightHint = 500;
        treeWrapper.setLayoutData(gd);
        treeWrapper.setLayout(tcl);

        commandsTreeTable = new TreeViewer(treeWrapper, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        commandsTreeTable.getTree().setHeaderVisible(true);
        commandsTreeTable.getTree().setLinesVisible(true);

        // column xtce path
        TreeViewerColumn pathColumn = new TreeViewerColumn(commandsTreeTable, SWT.NONE);
        pathColumn.getColumn().setText(COL_PATH);
        pathColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof XtceSubSystemNode) {
                    XtceSubSystemNode node = (XtceSubSystemNode) element;
                    return node.getName();
                } else {
                    XtceCommandNode node = (XtceCommandNode) element;
                    return node.getCommandInfo().getName();
                }
            }
        });
        tcl.setColumnData(pathColumn.getColumn(), new ColumnPixelData(300));

        // column significance
        TreeViewerColumn significanceColumn = new TreeViewerColumn(commandsTreeTable, SWT.NONE);
        significanceColumn.getColumn().setText(COL_SIGN);
        significanceColumn.getColumn().setToolTipText("Significance Level");
        significanceColumn.getColumn().setAlignment(SWT.CENTER);
        significanceColumn.setLabelProvider(new CenteredImageLabelProvider() {
            @Override
            public Image getImage(Object element) {
                if (element instanceof XtceCommandNode) {
                    XtceCommandNode node = (XtceCommandNode) element;
                    CommandInfo cmd = node.getCommandInfo();
                    if (cmd.getSignificance() == null) {
                        return level0Image;
                    }
                    switch (cmd.getSignificance().getConsequenceLevel()) {
                    case NONE:
                        return level0Image;
                    case WATCH:
                        return level1Image;
                    case WARNING:
                        return level2Image;
                    case DISTRESS:
                        return level3Image;
                    case CRITICAL:
                        return level4Image;
                    case SEVERE:
                        return level5Image;
                    default:
                        return null;
                    }
                }
                return null;
            }
        });
        tcl.setColumnData(significanceColumn.getColumn(), new ColumnPixelData(50));

        // on item selection update significance message and page completion status
        commandsTreeTable.addSelectionChangedListener(evt -> {
            IStructuredSelection sel = (IStructuredSelection) evt.getSelection();
            if (sel.isEmpty()) {
                setMessage(null);
                return;
            }

            Object selectedElement = sel.getFirstElement();
            if (selectedElement instanceof XtceCommandNode) {
                CommandInfo cmd = ((XtceCommandNode) sel.getFirstElement()).getCommandInfo();
                // setMessage(getMessage(cmd));

                command.setMetaCommand(cmd);
                command.setSelectedAliase(cmd.getQualifiedName());
                setPageComplete(true);
            } else {
                setPageComplete(false);
            }
        });
        CommandTreeContentProvider commandTreeContentProvider = new CommandTreeContentProvider();
        commandsTreeTable.setContentProvider(commandTreeContentProvider);
        commandsTreeTable.setInput(commandTreeContentProvider);

        YamcsPlugin.getMissionDatabase().getCommands().forEach(cmd -> {
            if (!cmd.hasAbstract() || !cmd.getAbstract()) {
                // add aliases columns
                for (NamedObjectId alias : cmd.getAliasList()) {
                    String namespace = alias.getNamespace();
                    if (!namespaces.contains(namespace) && !namespace.startsWith("/")) {
                        namespaces.add(namespace);
                        addAliasColumn(namespace);
                    }
                }
                commandTreeContentProvider.addElement(cmd.getQualifiedName(), cmd);
            }
        });

        commandsTreeTable.expandAll();

        CommandTreeViewerFilter filter = new CommandTreeViewerFilter(commandTreeContentProvider);
        commandsTreeTable.addFilter(filter);
        searchbox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ke) {
                if (ke.keyCode == SWT.ARROW_DOWN) {
                    TreeItem[] items = commandsTreeTable.getTree().getItems();
                    if (items.length > 0) {
                        IStructuredSelection sel = new StructuredSelection(items[0].getData());
                        commandsTreeTable.setSelection(sel);
                        commandsTreeTable.getTree().setFocus();
                    }
                } else {
                    filter.setSearchTerm(searchbox.getText());
                    commandsTreeTable.refresh();
                    commandsTreeTable.expandAll();
                }
            }
        });

        commandsTreeTable.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object o1, Object o2) {
                String n1;
                String n2;
                if (o1 instanceof XtceSubSystemNode) {
                    n1 = ((XtceSubSystemNode) o1).getName();
                } else {
                    n1 = ((XtceCommandNode) o1).getCommandInfo().getQualifiedName();
                }

                if (o2 instanceof XtceSubSystemNode) {
                    n2 = ((XtceSubSystemNode) o2).getName();
                } else {
                    n2 = ((XtceCommandNode) o2).getCommandInfo().getQualifiedName();
                }

                return n1.compareTo(n2);
            }
        });
    }

    public static String getMessage(CommandInfo cmd) {
        if (cmd == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder();

        buf.append(cmd.getQualifiedName());

        SignificanceInfo significance = cmd.getSignificance();
        if (significance != null) {
            buf.append("\n");
            if (significance.getConsequenceLevel() != SignificanceLevelType.NONE) {
                buf.append("[");
                buf.append(significance.getConsequenceLevel().toString());
                buf.append("] ");
            }
            if (significance.getReasonForWarning() != null) {
                buf.append(significance.getReasonForWarning());
            }
            return buf.toString();

        } else {
            return null;
        }
    }
}
