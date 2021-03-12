package com.windhoverlabs.studio.registry;

import java.io.FileNotFoundException;
import java.util.LinkedHashMap;

/**
 * @author lgomez
 *Abstract class configuration registry that behaves like a dictionary.
 *This is meant to make loading CFS configuration format-agnostic. Meaning it does not matter
 *if the configuration is stored in a YAML file, SQLite or even XML file; this class enforces implementors to treat the
 *data as a dictionary, or a LinkedHashMap concretely speaking.
 */
public abstract class ConfigRegistry {
	public final String PATH_SEPARATOR = "."; 
	protected LinkedHashMap<?, ?> registry;
	
//	private getPartial
	/**
	 * @param registryPath The path to the location of inside the configuration registry. The path is expected to be in the ".A.C.D" format.
	 * @return A LinkedHashMap containing the data at registryPath.
	 * @throws Exception
	 * TODO Not sure if having this function return an object is the best design. Perhaps a String?
	 */
	public Object get(String registryPath) throws Exception
	{
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
				
		for(int i = 0;i<node_keys.length;i++) 
		{
			if(registryPartition.containsKey(node_keys[i])) 
			{
				if(registryPartition.get(node_keys[i]) instanceof LinkedHashMap<?, ?>) 
				{
					registryPartition = (LinkedHashMap<?, ?>) registryPartition.get(node_keys[i]);
				}
				else 
				{
					if(node_keys.length == i-1) 
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
	
	public abstract void loadRegistry(String filePath) throws FileNotFoundException;
}