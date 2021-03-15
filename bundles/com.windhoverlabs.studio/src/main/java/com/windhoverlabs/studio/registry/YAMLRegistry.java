package com.windhoverlabs.studio.registry;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import org.eclipse.core.runtime.CoreException;
import org.yaml.snakeyaml.Yaml;

/**
 * 
 * @author lgomez
 *The YAML Registry represents a registry(a collection of configuration) that is written to some YAML file.
 */
public class YAMLRegistry extends ConfigRegistry {

	public YAMLRegistry() throws FileNotFoundException, URISyntaxException, CoreException {
		super();
		System.out.println("YAMLRegistry");
		Yaml yaml = new Yaml();
		InputStream input = new FileInputStream(new File(getCurrentPath("Displays")));
		registry = (LinkedHashMap<?, ?>) yaml.load(input);
	}

}