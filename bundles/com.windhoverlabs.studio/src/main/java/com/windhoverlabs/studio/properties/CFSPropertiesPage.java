package com.windhoverlabs.studio.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.core.resources.ResourcesPlugin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

  /**
   * 
   * GUI Interface for Airliner section in project properties.
   * 
   * @author lgomez
   *
   */
  public class CFSPropertiesPage extends PropertyPage implements IWorkbenchPropertyPage {
  		
  	private FileFieldEditor fileEditor;
  	private IProject currentProject;
  	private IPreferenceStore preferenceStore;
  	private IScopeContext context;
  	private Composite pathEditorHolder;
  	
  	/**
  	 *@todo Not a great way of declaring the logger. Will revisit.
  	 */
	private static final Logger LOGGER = Logger.getLogger(Thread.currentThread().getStackTrace()[0].getClassName() );
  	
  	public CFSPropertiesPage() {
  		
  	}
  	/**
      * 
      * Main function executes to create and return the GUI interface of the Airliner property page.
      * 
      * @param parent
      * @return Control
      * 
      */
  	@Override
  	protected Control createContents(Composite parent) {
  		prepareProperties();
  		prepareHolder(parent);
  		createPathChooser();
  		return getControl();
  	}
  	
  	/**
      * 
      * Finds the project properties in the preference store and links it to the GUI interfaces on the CFS property page.
      * 
      */
  	private void prepareProperties() {
  		// Retrieve the currently selected project.
  		IAdaptable adaptable = getElement();
  		currentProject = (IProject) adaptable.getAdapter(IProject.class);
        context = new ProjectScope(currentProject);
        // Retrieve the current project's preference store, and associate the property page with it.
        preferenceStore = new ScopedPreferenceStore(context, "com.windhoverlabs.studio.registryDB");
        setPreferenceStore(preferenceStore);
  	}
  	
  	/**
      * 
      * Creates the parent composite to hold the path editor composite.
      * 
      */
  	private void prepareHolder(Composite parent) {
  		// Create the composite which will hold the path chooser.
  		pathEditorHolder = new Composite(parent, SWT.NONE);
  		GridData gridData = new GridData();
  		gridData.horizontalAlignment = GridData.FILL;
  		gridData.grabExcessHorizontalSpace = true;
  		pathEditorHolder.setLayoutData(gridData);
  		pathEditorHolder.setLayout(new GridLayout(1, false));
  	}
  	
  	/**
     * 
     * Returns the default path of the configuration file. This could be YAML, XML, SQLite, etc.
     * 
     * @return defaultPath
     * 
     */
 	private static String getDefaultPath() {
// 		TODO:Quick and dirty fix for now. Still thinking about this.
 		String defaultPath = "${project_loc:Displays}/Resources/definitions.yaml";
 		
 		return defaultPath;
 	}
 	
  	/**
      * 
      * Creates the path editor composite and sets the contents.
      * 
      */
  	private void createPathChooser() {
  		// Retrieve the current properties from the associated preference store.
  		// Retrieve the preferenceStore string representation of path
  		String defaultPath = getDefaultPath();
  		preferenceStore.setDefault(PropertiesConstants.DEF_CONFIG_PATHS, defaultPath);  
  		  		
  		// Create the file field editor, and assign it with the preference variable 'path', set it to the associated preference store.
  		fileEditor = new FileFieldEditor(PropertiesConstants.DEF_CONFIG_PATHS, "Path to Registry", pathEditorHolder);
  		fileEditor.setPreferenceStore(preferenceStore);  		
  		/**
  		 *@note These extensions are platform-specific. These were tested on Ubuntu 18.04 LTS.
  		 *The ones in FileDialog are not accessible for some reason.
  		 */
  		fileEditor.setFileExtensions(PropertiesConstants.SUPPORTED_EXTENSIONS);
  		fileEditor.load();
  		
  	}
  	
  	/**
      * 
      * Loads the default of the Airliner property page. For the paths editor it is ${project_loc}/apps
      * 
      */
  	protected void performDefaults() {
  		fileEditor.load();
  		super.performDefaults();
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
			
			LOGGER.info(String.format("String variable \"%s\" could not converted.", varString));
			e.printStackTrace();
		}
		
		return updatedString;
  	}
  	
  	/**
  	 *@author lgomez
  	 *
  	 *Gets the current registry path stored on the property page.
  	 * @return
  	 * @throws URISyntaxException 
  	 * @throws CoreException 
  	 */
  	public static String getCurrentPath(String projectName) throws URISyntaxException, CoreException 
  	{
  		IProject currentProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
  		
  		
  		IScopeContext context = new ProjectScope(currentProject);
  		IPreferenceStore pStore = new ScopedPreferenceStore(context, "com.windhoverlabs.studio.registryDB");
  		
  		String defaultPath = getDefaultPath();
  		pStore.setDefault(PropertiesConstants.DEF_CONFIG_PATHS, defaultPath); 

  		
  		String path = pStore.getString(PropertiesConstants.DEF_CONFIG_PATHS);
				
		path = convertVarString(path);
  		
  		if(path == null) 
  		{
  			//If there are no variable inside the string, just get the raw string.
  			path = pStore.getString(PropertiesConstants.DEF_CONFIG_PATHS);
  		}
  		
  		return path;
  	}
  }

