package com.windhoverlabs.studio.registry;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import org.eclipse.core.runtime.CoreException;
import org.yaml.snakeyaml.Yaml;

import com.windhoverlabs.studio.registry.preferences.RegistryPreferencePage;

/**
 * 
 * @author lgomez
 *The YAML Registry represents a registry(a collection of configuration) that is written to some YAML file.
 */
public class YAMLRegistry extends ConfigRegistry {

	/**
	 * Initializes the YAML registry.
	 * @throws FileNotFoundException
	 * @throws URISyntaxException
	 * @throws CoreException
	 * @note Should we have a constructor with side-effects? Side-effects in the sense that this constructor will
	 * load the configuration registry from a file on disk.	
	 */
	public YAMLRegistry() throws FileNotFoundException, URISyntaxException, CoreException {
		super();
		String yamlPath = getCurrentPath();
		if(yamlPath.isEmpty()) 
		{
			RegistryPreferencePage.LOGGER.warning("Registry load failed. Path provided is empty. Please check you configuration registry.");
		}
		
		else 
		{
			File yamlFile = new File(yamlPath);
			if(yamlFile.exists() == false) 
			{
				RegistryPreferencePage.LOGGER.warning("Registry load failed. File provided does not exist. "
													 +"Please check you configuration registry.");
			}
			else 
			{
				Yaml yaml = new Yaml();
				InputStream input = new FileInputStream(yamlFile);
				registry = (LinkedHashMap<?, ?>) yaml.load(input);
			}
		}
	}

}