package com.windhoverlabs.studio.registry.preferences;

import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import com.windhoverlabs.studio.registry.Activator;

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
	
	
	private static final Logger LOGGER = Logger.getLogger(Thread.currentThread().getStackTrace()[0].getClassName() );
	
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
        
        setPreferenceStore(preferenceStore);
  		  		
  		// Create the file field editor, and assign it with the preference variable 'path', set it to the associated preference store.
        
        preferenceStore.setDefault(PreferenceConstants.REGISTRY_DB, getDefaultPath());
  		fileEditor = new FileFieldEditor(PreferenceConstants.REGISTRY_DB, "Path to Registry", parent);
  		/**
  		 *@note These extensions are platform-specific. These were tested on Ubuntu 18.04 LTS.
  		 *The ones in FileDialog are not accessible for some reason.
  		 */
  		
  		fileEditor.setFileExtensions(PreferenceConstants.SUPPORTED_EXTENSIONS);
  		fileEditor.setPreferenceStore(preferenceStore);
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
 		preferenceStore.setValue(PreferenceConstants.REGISTRY_DB, fileEditor.getStringValue());	
 		return super.performOk();
 	}
 	
  	/**
  	 * Convert a variable string of the form "${project_loc}/path/to/file" to  "/actual_path/path/to/file".
  	 * Very useful for paths that stored in Eclipse's reference store.
  	 * 
  	 * @note Note that IF there are multiple values(such as paths)
  	 * in the string, the Eclipse API uses a ":" as a delimiter.
  	 * For example: the string "/path/a:path/b:path/c" implies that there are THREE strings stored in this variable.
  	 * I'm still investigating this; this cold be a byproduct of the way we handle the preferenceStore object.
  	 * If you have a string with multiple value(or multiple paths), have a look at {@link #parseString(String) parseString}.
  	 * 
  	 * @param varString The variable string to convert.
  	 * 
  	 * @return If varString is valid, the new string with all of its variables translated to its actual values. Otherwise,
  	 * this function returns null. 
  	 */
  	public static String convertVarString(String varString) 
  	{
  		
        /* To convert '${project_loc}' to an actual path... */
  		
		VariablesPlugin variablesPlugin = VariablesPlugin.getDefault();
		IStringVariableManager manager = variablesPlugin.getStringVariableManager();
		String updatedString = null;
		try {
			updatedString = manager.performStringSubstitution(varString);
		} catch (CoreException e) {
			
//			LOGGER.info(String.format("String variable \"%s\" could not converted.", varString));
			e.printStackTrace();
		}
		
		return updatedString;
  	}
  	
  	private static String getDefaultPath() 
  	{
  		String defaultPath = "";
  		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
  		
  		if(projects.length > 0) 
  		{
  			IPath projectPath = projects[0].getLocation();
  			IPath defaultRegistryPath = projectPath.append("Resources/definitions.yaml"); 
  	  		defaultPath = defaultRegistryPath.toString() ;
  		}
  		else 
  		{
  			LOGGER.warning("No project present. Default path could not be fetched.");
  		}
  		
		return defaultPath;
  	}
 	
  	protected void performDefaults() {
  		fileEditor.load();
  		super.performDefaults();
  	}
  	
  	/**
  	 * @note Leaving the projectName as an argument for now until we decide what to do about path conventions
  	 * @param projectName
  	 * @return
  	 */
  	public static String  getCurrentPath(String projectName) 
  	{
  		return  Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.REGISTRY_DB);
  	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}