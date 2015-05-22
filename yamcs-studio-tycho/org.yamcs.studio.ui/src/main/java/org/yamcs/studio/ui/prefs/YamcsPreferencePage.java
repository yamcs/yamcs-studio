package org.yamcs.studio.ui.prefs;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.ui.ConnectHandler;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace
 * that allows us to create a page that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that
 * belongs to the main plug-in class. That way, preferences can be accessed directly via the
 * preference store.
 */
public class YamcsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    private class YamcsNode {
        int nodeNumber;
        StringFieldEditor yamcsHost;
        IntegerFieldEditor yamcsPort;
        IntegerFieldEditor yamcsHornetQPort;
        StringFieldEditor yamcsInstance;
        BooleanFieldEditor yamcsPrivileges;

        public boolean isSource(PropertyChangeEvent event)
        {
            return event.getSource() == yamcsHost
                    || event.getSource() == yamcsPort
                    || event.getSource() == yamcsHornetQPort
                    || event.getSource() == yamcsInstance
                    || event.getSource() == yamcsPrivileges;
        }

        public boolean hasChanged()
        {
            YamcsPlugin plugin = YamcsPlugin.getDefault();
            return !yamcsHost.getStringValue().equals(plugin.getHost()) ||
                    yamcsPort.getIntValue() != plugin.getWebPort() ||
                    yamcsHornetQPort.getIntValue() != plugin.getHornetQPort() ||
                    !yamcsInstance.getStringValue().equals(plugin.getInstance()) ||
                    !mdbNamespace.getStringValue().equals(plugin.getMdbNamespace()) ||
                    !yamcsPrivileges.getBooleanValue() == plugin.getPrivilegesEnabled();
        }

        public boolean isValid()
        {
            String yamcsHostText = yamcsHost.getStringValue();
            String yamcsInstanceText = yamcsInstance.getStringValue();
            return yamcsHostText.trim().matches("[a-zA-Z\\-\\.0-9_]+") && yamcsInstanceText.trim().matches("[a-zA-Z\\.\\-0-9_]+");
        }
    }

    private IntegerFieldEditor currentNode;

    private List<YamcsNode> nodes = new LinkedList<YamcsNode>();
    private StringFieldEditor mdbNamespace;

    public YamcsPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(YamcsPlugin.getDefault().getPreferenceStore());
        setDescription("Set connection properties to a Yamcs server");
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to
     * manipulate various types of preferences. Each field editor knows how to save and restore
     * itself.
     */
    @Override
    public void createFieldEditors() {

        // TODO: make this preference page more dynamic to add more nodes from the menu
        int numberOfNodes = YamcsPlugin.getDefault().getNumberOfNodes();
        for (int i = 0; i < numberOfNodes; i++)
        {
            YamcsNode yamcsNode = new YamcsNode();
            yamcsNode.nodeNumber = i + 1;
            yamcsNode.yamcsHost = new StringFieldEditor("node" + yamcsNode.nodeNumber + ".yamcs_host", "Host", getFieldEditorParent());
            yamcsNode.yamcsPort = new IntegerFieldEditor("node" + yamcsNode.nodeNumber + ".yamcs_port", "Port", getFieldEditorParent());
            yamcsNode.yamcsHornetQPort = new IntegerFieldEditor("node" + yamcsNode.nodeNumber + ".yamcs_hornetqport", "HornetQ Port",
                    getFieldEditorParent());
            yamcsNode.yamcsInstance = new StringFieldEditor("node" + yamcsNode.nodeNumber + ".yamcs_instance", "Instance", getFieldEditorParent());
            yamcsNode.yamcsPrivileges = new BooleanFieldEditor("node" + yamcsNode.nodeNumber + ".yamcs_privileges", "Secured", getFieldEditorParent());

            addField(new LabelFieldEditor("Node " + yamcsNode.nodeNumber, getFieldEditorParent()));
            addField(yamcsNode.yamcsHost);
            addField(yamcsNode.yamcsPort);
            addField(yamcsNode.yamcsHornetQPort);
            addField(yamcsNode.yamcsInstance);
            addField(yamcsNode.yamcsPrivileges);
            addField(new SpacerFieldEditor(getFieldEditorParent()));
        }

        currentNode = new IntegerFieldEditor("current_node", "Active node", getFieldEditorParent());
        addField(currentNode);
        addField(new SpacerFieldEditor(getFieldEditorParent()));

        mdbNamespace = new StringFieldEditor("mdb_namespace", "MDB Namespace", getFieldEditorParent());
        addField(mdbNamespace);
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void checkState() {
        super.checkState();
        if (!isValid())
            return;

        boolean isValid = true;
        for (YamcsNode node : nodes)
        {
            isValid &= node.isValid();
        }
        if (!isValid) {
            setErrorMessage("Not a valid host name");
            setValid(false);
        } else {
            setErrorMessage(null);
            setValid(true);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (event.getProperty().equals(FieldEditor.VALUE)) {

            // boolean checkState = nodes.map(n -> n.size()).reduce(false, (a, b) -> a || b);
            boolean checkState = false;
            for (YamcsNode node : nodes)
                checkState |= node.isSource(event);
            checkState |= event.getSource() == currentNode;
            if (checkState) {
                checkState();
            }
        }
    }

    @Override
    public boolean performOk() {
        // Detect changes (there's probably a better way to do this)
        YamcsPlugin plugin = YamcsPlugin.getDefault();
        boolean changed = false;
        for (YamcsNode node : nodes)
            changed |= node.hasChanged();
        changed |= !(currentNode.getIntValue() == plugin.getCurrentNode());
        // Save to store
        boolean ret = super.performOk();
        // Hint that user should reconnect
        if (changed)
            askApply();
        return ret;
    }

    /**
     * Shows a dialog asking to restart workspace if pkg-config preferences have been changed.
     */
    private static void askApply() {
        MessageDialog dialog = new MessageDialog(null, "Apply changes?", null, "Changes made to Yamcs" +
                " preferences require a re-establish connection to the Yamcs server in order to take effect.\n\n" +
                "Would you like to reconnect now?",
                MessageDialog.QUESTION, new String[] { "Yes", "No" }, 0);
        if (dialog.open() == 0)
            Display.getDefault().asyncExec(() -> {
                YamcsPlugin.getDefault().disconnect();
                try {
                    (new ConnectHandler()).execute(null);
                } catch (Exception e) {
                }
            });
    }
}
