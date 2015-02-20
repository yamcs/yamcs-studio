package org.csstudio.utility.pvmanager.yamcs.preferences;

import org.csstudio.utility.pvmanager.yamcs.Activator;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */
public class YamcsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;
    
    private StringFieldEditor yamcsHost;
	private IntegerFieldEditor yamcsPort;
	private StringFieldEditor yamcsInstance;

	public YamcsPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Set connection properties to a Yamcs server (as used for yamcs:// datasources)");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
    public void createFieldEditors() {
		yamcsHost = new StringFieldEditor("yamcs_host", "Host", getFieldEditorParent());
		addField(yamcsHost);
		yamcsPort = new IntegerFieldEditor("yamcs_port", "Port", getFieldEditorParent());
        addField(yamcsPort);
		yamcsInstance = new StringFieldEditor("yamcs_instance", "Instance", getFieldEditorParent());
		addField(yamcsInstance);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void checkState() {
		super.checkState();
		if (!isValid()) {
			return;
		}
		String yamcsHostText = yamcsHost.getStringValue();
		String yamcsInstanceText = yamcsInstance.getStringValue();
		if (!(yamcsHostText.trim().matches("[a-zA-Z0-9_]+")) || !(yamcsInstanceText.trim().matches("[a-zA-Z0-9_]+"))) {
			setErrorMessage("Host and instance must contain alphanumerics and _ with no white space");
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
			if (event.getSource() == yamcsHost
					|| event.getSource() == yamcsPort
					|| event.getSource() == yamcsInstance) {
				checkState();
			}
		}
	}
	
	@Override
	public boolean performOk() {
	    boolean ret = super.performOk();
	    // TODO replace yservice or do something smarter than that
	    return ret;
	}
}
