package com.windhoverlabs.studio.registry;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedHashMap;

import org.yaml.snakeyaml.Yaml;

/**
 * 
 * @author lgomez
 *The YAML Registry represents a registry(a collection of configuration) that is written to some YAML file.
 */
public class YAMLRegistry extends ConfigRegistry {

	@Override
	public void loadRegistry(String filePath) throws FileNotFoundException {
		Yaml yaml = new Yaml();
		InputStream input = new FileInputStream(new File(filePath));
		registry =  (LinkedHashMap<String, ?>) yaml.load(input);
		
	}

}