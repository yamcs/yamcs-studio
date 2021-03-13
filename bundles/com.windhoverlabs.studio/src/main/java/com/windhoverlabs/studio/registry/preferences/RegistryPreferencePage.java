package com.windhoverlabs.studio.registry.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import com.windhoverlabs.studio.properties.Activator;
import com.windhoverlabs.studio.properties.PropertiesConstants;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class RegistryPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {
	
	private FileFieldEditor fileEditor;
	private IPreferenceStore preferenceStore;
	public RegistryPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Configure Registry");
		prepareProperties();
	}
	
  	/**
     * 
     * Finds the project properties in the preference store and links it to the GUI interfaces on the CFS preference page.
     * 
     */
 	private void prepareProperties() {
       // Retrieve the preference store, and associate the preference page with it.
       
       preferenceStore = Activator.getDefault().getPreferenceStore();
       setPreferenceStore(preferenceStore);
 	}
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
        Composite parent = getFieldEditorParent();
  		  		
  		// Create the file field editor, and assign it with the preference variable 'path', set it to the associated preference store.
  		fileEditor = new FileFieldEditor(PropertiesConstants.DEF_CONFIG_PATHS, "Path to Registry", parent);
  		/**
  		 *@note These extensions are platform-specific. These were tested on Ubuntu 18.04 LTS.
  		 *The ones in FileDialog are not accessible for some reason.
  		 */
  		fileEditor.setFileExtensions(PropertiesConstants.SUPPORTED_EXTENSIONS);
  		fileEditor.load();
  				
		addField(fileEditor);
	}
	
  	/**
     * 
     * Saves the input data.
     * 
     * @return performedStatus
     * 
     */
 	public boolean performOk() {
 		preferenceStore.setValue(PropertiesConstants.DEF_CONFIG_PATHS, fileEditor.getStringValue());
 		return true;
 	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}