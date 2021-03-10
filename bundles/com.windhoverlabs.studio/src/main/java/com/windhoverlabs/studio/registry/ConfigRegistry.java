package com.windhoverlabs.studio.registry;

import java.util.LinkedHashMap;

/**
 * @author lgomez
 *Very simple interface to abstract a configuration registry that behaves like a dictionary.
 *This is meant to make loading CFS configuration format-agnostic. Meaning it does not matter
 *if the configuration is stored in a YAML file, SQLite or even XML file; this interface enforces implementors to treat the
 *data as a dictionary, or a LinkedHashMap concretely speaking.
 */
public interface ConfigRegistry {
	
	/**
	 * @param registryPath The path to the location of inside the configuration registry. 
	 * @return A LinkedHashMap containing the data at registryPath.
	 */
	public LinkedHashMap<?, ?> get(String registryPath);
}