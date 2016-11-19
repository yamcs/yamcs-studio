package org.yamcs.studio.ui.commanding.stack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.protobuf.Mdb.SignificanceInfo;
import org.yamcs.protobuf.Mdb.SignificanceInfo.SignificanceLevelType;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.core.ui.utils.CenteredImageLabelProvider;
import org.yamcs.studio.core.ui.utils.RCPUtils;

public class AddToStackWizardPage1 extends WizardPage {

    public static final String COL_PATH = "XTCE Path";
    public static final String COL_SIGN = "Sig.";
    public static final String COL_QNAME = "Qualified Name";
    public static final int COLUMN_WIDTH = 10;
    public static final int COLUMN_MAX_WIDTH = 600;

    private Image level1Image;
    private Image level2Image;
    private Image level3Image;
    private Image level4Image;
    private Image level5Image;

    private StackedCommand command;
    TreeViewer commandsTreeTable;
    TreeColumnLayout tcl;

    List<String> namespaces = new ArrayList();

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
                CommandInfo cmd = (CommandInfo) element;
                if (cmd.getAbstract()) {
                    // show a blank line if the command is abstract
                    return "";
                }
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

        // add filter box
        Text searchbox = new Text(composite, SWT.SEARCH | SWT.BORDER | SWT.ICON_CANCEL);
        searchbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // build tree table
        ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources(), composite);
        level1Image = resourceManager.createImage(RCPUtils.getImageDescriptor(AddToStackWizardPage1.class, "icons/level1s.png"));
        level2Image = resourceManager.createImage(RCPUtils.getImageDescriptor(AddToStackWizardPage1.class, "icons/level2s.png"));
        level3Image = resourceManager.createImage(RCPUtils.getImageDescriptor(AddToStackWizardPage1.class, "icons/level3s.png"));
        level4Image = resourceManager.createImage(RCPUtils.getImageDescriptor(AddToStackWizardPage1.class, "icons/level4s.png"));
        level5Image = resourceManager.createImage(RCPUtils.getImageDescriptor(AddToStackWizardPage1.class, "icons/level5s.png"));

        Composite tableWrapper = new Composite(composite, SWT.NONE);
        tcl = new TreeColumnLayout();
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        tableWrapper.setLayout(tcl);

        commandsTreeTable = new TreeViewer(tableWrapper, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        commandsTreeTable.getTree().setHeaderVisible(true);
        commandsTreeTable.getTree().setLinesVisible(false);

        // column xtce path
        TreeViewerColumn pathColumn = new TreeViewerColumn(commandsTreeTable, SWT.NONE);
        pathColumn.getColumn().setText(COL_PATH);
        pathColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                CommandInfo cmd = (CommandInfo) element;
                return cmd.getName();
            }
        });
        tcl.setColumnData(pathColumn.getColumn(), new ColumnPixelData(COLUMN_WIDTH));

        // column significance
        TreeViewerColumn significanceColumn = new TreeViewerColumn(commandsTreeTable, SWT.NONE);
        significanceColumn.getColumn().setText(COL_SIGN);
        significanceColumn.getColumn().setToolTipText("Significance Level");
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

        // column qualified name
        TreeViewerColumn nameColumn = new TreeViewerColumn(commandsTreeTable, SWT.NONE);
        nameColumn.getColumn().setText(COL_QNAME);
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                CommandInfo cmd = (CommandInfo) element;
                if (cmd.getAbstract()) {
                    // show a blank line if the command is abstract
                    return "";
                }
                return cmd.getQualifiedName();
            }
        });
        tcl.setColumnData(nameColumn.getColumn(), new ColumnPixelData(COLUMN_WIDTH));

        // on item selection update significance message and page completion status
        commandsTreeTable.addSelectionChangedListener(evt -> {
            IStructuredSelection sel = (IStructuredSelection) evt.getSelection();
            if (sel.isEmpty()) {
                setMessage(null);
                return;
            }

            CommandInfo cmd = (CommandInfo) sel.getFirstElement();
            setMessage(getMessage(cmd));

            command.setMetaCommand(cmd);
            command.setSelectedAliase(cmd.getQualifiedName());
            setPageComplete(!cmd.getAbstract());
        });
        CommandTreeContentProvider commandTreeContentProvider = new CommandTreeContentProvider();
        commandsTreeTable.setContentProvider(commandTreeContentProvider);

        // load command list
        Collection<CommandInfo> commandInfos = new ArrayList<>();
        CommandingCatalogue.getInstance().getMetaCommands().forEach(cmd -> {

            // add aliases columns
            for (NamedObjectId alias : cmd.getAliasList()) {
                String namespace = alias.getNamespace();
                if (!namespaces.contains(namespace) && !namespace.startsWith("/")) {
                    namespaces.add(namespace);
                    addAliasColumn(namespace);
                }
            }
            commandInfos.add(cmd);
        });
        commandsTreeTable.setInput(commandInfos);
        commandsTreeTable.expandAll();

        // adjust columns width to content up to COLUMN_MAX_WIDTH
        // with a small hack to display full data on the first column
        for (TreeColumn tc : commandsTreeTable.getTree().getColumns())
            tc.pack();
        pathColumn.getColumn().setWidth(pathColumn.getColumn().getWidth() + 11 * commandTreeContentProvider.nbLevels);
        for (TreeColumn tc : commandsTreeTable.getTree().getColumns()) {
            if (tc.getWidth() > COLUMN_MAX_WIDTH)
                tc.setWidth(COLUMN_MAX_WIDTH);
        }

        // filter
        CommandInfoTreeViewerFilter filter = new CommandInfoTreeViewerFilter(commandTreeContentProvider);
        commandsTreeTable.addFilter(filter);
        searchbox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ke) {
                filter.setSearchTerm(searchbox.getText());
                commandsTreeTable.refresh();
                commandsTreeTable.expandAll();
            }
        });

        commandsTreeTable.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object o1, Object o2) {
                CommandInfo c1 = (CommandInfo) o1;
                CommandInfo c2 = (CommandInfo) o2;
                return c1.getQualifiedName().compareTo(c2.getQualifiedName());
            }
        });

    }

    static public String getMessage(CommandInfo cmd) {
        if (cmd == null)
            return null;

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

    class CommandTreeContentProvider implements ITreeContentProvider {

        ArrayList<CommandInfo> commandInfos;
        public int nbLevels = 1;

        @Override
        public void dispose() {
            // TODO Auto-generated method stub

        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object[] getElements(Object inputElement) {
            commandInfos = (ArrayList<CommandInfo>) inputElement;
            ArrayList<CommandInfo> rootCommands = new ArrayList<>();

            // find root commands
            for (CommandInfo ci : commandInfos) {
                if (!hasParent(ci))
                    rootCommands.add(ci);
            }

            // compute number of inheritance level
            int currentNbLevels = 1;
            for (CommandInfo ci : commandInfos) {
                currentNbLevels = nbParents(ci) + 1;
                if (currentNbLevels > nbLevels)
                    nbLevels = currentNbLevels;
            }

            return rootCommands.toArray();
        }

        private boolean hasParent(CommandInfo ci) {
            return ci.getBaseCommand() != null && !ci.getBaseCommand().getName().equals("");
        }

        private int nbParents(CommandInfo ci) {
            if (hasParent(ci)) {
                CommandInfo parent = (CommandInfo) getParent(ci);
                return nbParents(parent) + 1;
            } else {
                return 0;
            }
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            ArrayList<CommandInfo> children = new ArrayList<>();
            CommandInfo parentCi = (CommandInfo) parentElement;
            for (CommandInfo ci : commandInfos) {
                if (ci.getBaseCommand().getQualifiedName().equals(parentCi.getQualifiedName()))
                    children.add(ci);
            }
            return children.toArray();
        }

        @Override
        public Object getParent(Object element) {
            CommandInfo baseCommand = ((CommandInfo) element).getBaseCommand();
            for (CommandInfo ci : commandInfos) {
                if (ci.getQualifiedName().equals(baseCommand.getQualifiedName()))
                    return ci;
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            Object[] children = getChildren(element);
            return children != null && children.length > 0;
        }

    }

}
