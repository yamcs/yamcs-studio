package org.yamcs.studio.ui.commanding.stack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.protobuf.Mdb.SignificanceInfo;
import org.yamcs.protobuf.Mdb.SignificanceInfo.SignificanceLevelType;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.core.ui.utils.CenteredImageLabelProvider;
import org.yamcs.studio.core.ui.utils.RCPUtils;

public class AddToStackWizardPage1 extends WizardPage {

    public static final String COL_SIGN = "Sig.";
    public static final String COL_QNAME = "Qualified Name";
    public static final int COLUMN_WIDTH = 10;

    private Image level1Image;
    private Image level2Image;
    private Image level3Image;
    private Image level4Image;
    private Image level5Image;

    private StackedCommand command;
    TableViewer commandsTable;
    TableColumnLayout tcl;

    List<String> namespaces = new ArrayList();

    public AddToStackWizardPage1(StackedCommand command) {
        super("Choose a Command");
        this.command = command;
        setTitle("Choose a Command");
        setPageComplete(false);
    }

    private void addAliasColumn(String namespace) {
        TableViewerColumn aliasColumn = new TableViewerColumn(commandsTable, SWT.NONE);
        aliasColumn.getColumn().setText(namespace);

        aliasColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                CommandInfo cmd = (CommandInfo) element;
                List<NamedObjectId> aliases = cmd.getAliasList();
                for (NamedObjectId aliase : aliases) {
                    if (aliase.getNamespace().equals(namespace))
                        return aliase.getName();
                }
                return "";
            }
        });
        tcl.setColumnData(aliasColumn.getColumn(), new ColumnPixelData(COLUMN_WIDTH));
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        composite.setLayout(gl);

        ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources(), composite);
        level1Image = resourceManager.createImage(RCPUtils.getImageDescriptor(AddToStackWizardPage1.class, "icons/level1s.png"));
        level2Image = resourceManager.createImage(RCPUtils.getImageDescriptor(AddToStackWizardPage1.class, "icons/level2s.png"));
        level3Image = resourceManager.createImage(RCPUtils.getImageDescriptor(AddToStackWizardPage1.class, "icons/level3s.png"));
        level4Image = resourceManager.createImage(RCPUtils.getImageDescriptor(AddToStackWizardPage1.class, "icons/level4s.png"));
        level5Image = resourceManager.createImage(RCPUtils.getImageDescriptor(AddToStackWizardPage1.class, "icons/level5s.png"));

        Text searchbox = new Text(composite, SWT.SEARCH | SWT.BORDER | SWT.ICON_CANCEL);
        searchbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite tableWrapper = new Composite(composite, SWT.NONE);
        tcl = new TableColumnLayout();
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        tableWrapper.setLayout(tcl);

        commandsTable = new TableViewer(tableWrapper, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
        commandsTable.getTable().setHeaderVisible(true);
        commandsTable.getTable().setLinesVisible(false);

        TableViewerColumn significanceColumn = new TableViewerColumn(commandsTable, SWT.NONE);
        significanceColumn.getColumn().setText(COL_SIGN);
        significanceColumn.getColumn().setToolTipText("Significance");
        significanceColumn.getColumn().setAlignment(SWT.CENTER);
        significanceColumn.setLabelProvider(new CenteredImageLabelProvider() {
            @Override
            public Image getImage(Object element) {
                CommandInfo cmd = (CommandInfo) element;
                if (cmd.getSignificance() == null)
                    return null;
                switch (cmd.getSignificance().getConsequenceLevel()) {
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
        });
        tcl.setColumnData(significanceColumn.getColumn(), new ColumnPixelData(40));

        TableViewerColumn nameColumn = new TableViewerColumn(commandsTable, SWT.NONE);
        nameColumn.getColumn().setText(COL_QNAME);
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                CommandInfo cmd = (CommandInfo) element;
                return cmd.getQualifiedName();
            }
        });
        tcl.setColumnData(nameColumn.getColumn(), new ColumnPixelData(COLUMN_WIDTH));

        commandsTable.addSelectionChangedListener(evt -> {
            IStructuredSelection sel = (IStructuredSelection) evt.getSelection();
            if (sel.isEmpty()) {
                setMessage(null);
                return;
            }

            CommandInfo cmd = (CommandInfo) sel.getFirstElement();
            SignificanceInfo significance = cmd.getSignificance();
            if (significance != null) {
                StringBuilder buf = new StringBuilder();
                if (significance.getConsequenceLevel() != SignificanceLevelType.NONE) {
                    buf.append("[");
                    buf.append(significance.getConsequenceLevel().toString());
                    buf.append("] ");
                }
                if (significance.getReasonForWarning() != null) {
                    buf.append(significance.getReasonForWarning());
                }
                setMessage(buf.toString());

            } else {
                setMessage(null);
            }
            command.setMetaCommand(cmd);
            setPageComplete(true);
        });

        commandsTable.setContentProvider(ArrayContentProvider.getInstance());

        // load command list
        Collection<CommandInfo> nonAbstract = new ArrayList<>();
        CommandingCatalogue.getInstance().getMetaCommands().forEach(cmd -> {
            if (!cmd.getAbstract()) {
                for (NamedObjectId alias : cmd.getAliasList()) {
                    String namespace = alias.getNamespace();
                    if (!namespaces.contains(namespace) && !namespace.startsWith("/")) {
                        namespaces.add(namespace);
                        addAliasColumn(namespace);
                    }
                }
                nonAbstract.add(cmd);
            }
        });
        commandsTable.setInput(nonAbstract);

        // adjust columns width to content
        for (TableColumn tc : commandsTable.getTable().getColumns())
            tc.pack();

        commandsTable.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object o1, Object o2) {
                CommandInfo c1 = (CommandInfo) o1;
                CommandInfo c2 = (CommandInfo) o2;
                return c1.getQualifiedName().compareTo(c2.getQualifiedName());
            }
        });

        CommandInfoViewerFilter filter = new CommandInfoViewerFilter();
        commandsTable.addFilter(filter);
        searchbox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ke) {
                filter.setSearchTerm(searchbox.getText());
                commandsTable.refresh();
            }
        });
    }
}
