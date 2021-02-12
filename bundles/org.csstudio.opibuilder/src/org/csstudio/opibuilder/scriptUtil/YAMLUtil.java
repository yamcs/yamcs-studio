package org.csstudio.opibuilder.scriptUtil;

import org.yaml.snakeyaml.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * @author lgomez
 *This class allows you to load a YAML file from disk.
 *Very useful for loading configuration into OPI Scripts.
 */
public class YAMLUtil {
	
public static Map parseYAML(String filePath) throws FileNotFoundException {
		Yaml yaml = new Yaml();
		InputStream input = new FileInputStream(new File(filePath));
		LinkedHashMap<?, ?> yaml_data =  (LinkedHashMap<?, ?>) yaml.load(input);
		return yaml_data;
	}

}
