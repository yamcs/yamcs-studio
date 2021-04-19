package com.windhoverlabs.studio.registry;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;

import com.windhoverlabs.studio.registry.preferences.RegistryPreferencePage;

/**
 * @author lgomez
 *Abstract class configuration registry that behaves like a dictionary.
 *This is meant to make loading CFS configuration format-agnostic. Meaning it does not matter
 *if the configuration is stored in a YAML file, SQLite or even XML file; this class enforces implementors to treat the
 *data as a dictionary, or a LinkedHashMap concretely speaking.
 */
public abstract class ConfigRegistry {
	public final static String PATH_SEPARATOR = "/"; 
	protected LinkedHashMap<?, ?> registry;
	
	/**
	 * Subclasses should load the registry(YAML, SQLite, etc) and store it in registry on the constructor.
	 * @throws FileNotFoundException
	 * @throws URISyntaxException
	 * @throws CoreException
	 */
	protected ConfigRegistry() 
	{
		
	}
	
	/**
	 * Helper function for subclasses to get the current registry path that is currently set by the user.
	 * @param projectName
	 * @return
	 * @throws URISyntaxException
	 * @throws CoreException
	 */
	protected String getCurrentPath() throws URISyntaxException, CoreException 
	{
		return RegistryPreferencePage.getCurrentPath();
	}
	
	/**
	 * @param registryPath The path to the location of inside the configuration registry. The path is expected to be in the "/A/C/D" format.
	 * A "/" means root, which returns the entire registry.
	 * @return A LinkedHashMap containing the data at registryPath.
	 * @throws Exception
	 */
	public Object get(String registryPath) throws Exception
	{
		if(registry == null) 
		{
			throw new Exception("The registry has not been loaded. Make sure this implementation is loading the registry in the constructor.");
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
	
	public static boolean isPathValid(String path) 
	{
		boolean isValid = true;
		
		if(path.isEmpty()) 
		{
			isValid= false;
		}
		
		else if(path.charAt(0) != PATH_SEPARATOR.charAt(0)) 
		{
			isValid = false;
		}
		
		
		return isValid;
	}
	
	/**
	 * Convenience function to append more nodes to path. For example; the call appendPath("/root/path", "new/node") will return
	 * "/root/path/new/node".
	 * @param rootPath
	 * @param newNodes
	 * @return
	 * @throws Exception 
	 */
	
	public static String appendPath(String rootPath, String newNodes) throws Exception 
	{
		
		if(newNodes.isEmpty()) 
		{
			throw new Exception("The new path must NOT be empty.");
		}
		
		if(isPathValid(rootPath) == false) 
		{
			throw new Exception(String.format("Root path %s is not valid.",rootPath));
		}
		
		//Cleanup the paths
		if (rootPath.charAt(rootPath.length()-1)== PATH_SEPARATOR.charAt(0)) 
		{
			rootPath = rootPath.substring(0, rootPath.length()-1);
		}
		
		if (newNodes.charAt(newNodes.length()-1)== PATH_SEPARATOR.charAt(0)) 
		{
			newNodes = newNodes.substring(0, newNodes.length()-1);
		}
		
		if (newNodes.charAt(0)== PATH_SEPARATOR.charAt(0)) 
		{
			newNodes = newNodes.substring(1);
		}
		
		String newPath = rootPath + PATH_SEPARATOR + newNodes;
		return newPath;
	}
	
	

	private void getAllTelmetry(LinkedHashMap<?,?>  modules, LinkedHashMap<Object, Object>  outMsgIds) 
	{
		  for (Map.Entry<?, ?> set : modules.entrySet()) 
		  { 
			  LinkedHashMap<?,?> module = ((LinkedHashMap<?,?>) modules.get(set.getKey() ));
			  
				if (module.get("modules") != null) 
				{
					getAllTelmetry((LinkedHashMap<?, ?>) module.get("modules"), outMsgIds);
				}
				if(module.get("telemetry") != null) 
				{
					outMsgIds.put((Object) module.values().toArray()[0], module.get("telemetry"));
				}
	        }
		
	}
	
	private void getAllTeleCommands(LinkedHashMap<?,?>  modules, LinkedHashMap<Object, Object>  outMsgIds) 
	{
		  for (Map.Entry<?, ?> set : modules.entrySet()) 
		  { 
			  LinkedHashMap<?,?> module = ((LinkedHashMap<?,?>) modules.get(set.getKey() ));
			  
				if (module.get("modules") != null) 
				{
					getAllTeleCommands((LinkedHashMap<?, ?>) module.get("modules"), outMsgIds);
				}
				if(module.get("commands") != null) 
				{
					outMsgIds.put((Object) module.values().toArray()[0], module.get("commands"));
				}
	        }
		
	}
	
	public LinkedHashMap<Object, Object> getAllMsgIDs() throws Exception 
	{
		LinkedHashMap<Object, Object> outMsgMap = new  LinkedHashMap<Object, Object>();
		
		//Access the registry through the get method for error-checking
		LinkedHashMap<?, ?> wholeRegistry = (LinkedHashMap<?, ?>) this.get("/modules");
		getAllTelmetry(wholeRegistry, outMsgMap);
		
		
		return outMsgMap;
		
	}
	
	public LinkedHashMap<Object, Object> getAllCommands() throws Exception 
	{
		LinkedHashMap<Object, Object> outCmdMap = new  LinkedHashMap<Object, Object>();
		
		//Access the registry through the get method for error-checking
		LinkedHashMap<?, ?> wholeRegistry = (LinkedHashMap<?, ?>) this.get("/modules");
		getAllTeleCommands(wholeRegistry, outCmdMap);
		
		
		return outCmdMap;
		
	}
}