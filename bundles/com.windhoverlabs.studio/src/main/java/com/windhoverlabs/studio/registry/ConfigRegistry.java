package com.windhoverlabs.studio.registry;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;

import org.eclipse.core.runtime.CoreException;

import com.windhoverlabs.studio.properties.CFSPropertiesPage;

/**
 * @author lgomez
 *Abstract class configuration registry that behaves like a dictionary.
 *This is meant to make loading CFS configuration format-agnostic. Meaning it does not matter
 *if the configuration is stored in a YAML file, SQLite or even XML file; this class enforces implementors to treat the
 *data as a dictionary, or a LinkedHashMap concretely speaking.
 */
public abstract class ConfigRegistry {
	public final String PATH_SEPARATOR = "/"; 
	protected LinkedHashMap<?, ?> registry;
	
	/**
	 * Helper function for subclasses to get the current registry path that is currently set by the user.
	 * @param projectName
	 * @return
	 * @throws URISyntaxException
	 * @throws CoreException
	 */
	protected String getCurrentPath(String projectName) throws URISyntaxException, CoreException 
	{
		return CFSPropertiesPage.getCurrentPath(projectName);
	}
	
	/**
	 * @param registryPath The path to the location of inside the configuration registry. The path is expected to be in the "/A/C/D" format.
	 * A "/" means root, which returns the entire registry.
	 * @return A LinkedHashMap containing the data at registryPath.
	 * @throws Exception
	 * TODO Not sure if having this function return an object is the best design. Perhaps a String?
	 */
	public Object get(String registryPath) throws Exception
	{
		if(registry == null) 
		{
			throw new Exception("The registry has not been loaded. Make sure you call loadRegistry before accessing anything.");
		}
		
		if(registryPath.charAt(0) != PATH_SEPARATOR.charAt(0)) 
		{
			throw new Exception("The path to a registry node must start with " + PATH_SEPARATOR);
		}
		
		if(registryPath.length() == 1) 
		{
			return registry;
		}
		
		String[] node_keys = registryPath.split(PATH_SEPARATOR);
		
		LinkedHashMap<?, ?> registryPartition  = (LinkedHashMap<?, ?>) registry.clone();
		
		//Start at second node to avoid the first node which has empty("") string
		for(int i = 1;i<node_keys.length;i++) 
		{
			if(registryPartition.containsKey(node_keys[i])) 
			{
				if(registryPartition.get(node_keys[i]) instanceof LinkedHashMap<?, ?>) 
				{
					registryPartition = (LinkedHashMap<?, ?>) registryPartition.get(node_keys[i]);
				}
				else 
				{
					if(node_keys.length-1 == i) 
					{
						return registryPartition.get(node_keys[i]);
					}
					else 
					{
						throw new Exception("The node " + "\"" + node_keys[i] + "\"" + " does not point to a dictinaory. Revise your registry path");
					}
				}
			}
			else 
			{
				throw new Exception("The node " + "\"" + node_keys[i] + "\"" + " does not exist in the registry. Revise your registry path");
			}
		}

		return registryPartition;
		
	}
	
	/**
	 * Subclasses should load the registry(YAML, SQLite, etc) and store it in registry.
	 * @param projectName
	 * @throws FileNotFoundException
	 * @throws URISyntaxException
	 * @throws CoreException
	 */
	public abstract void loadRegistry(String projectName) throws FileNotFoundException, URISyntaxException, CoreException;
}