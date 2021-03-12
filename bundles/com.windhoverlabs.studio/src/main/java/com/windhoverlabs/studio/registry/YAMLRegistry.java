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

	@Override
	public void loadRegistry(String projectName) throws FileNotFoundException, URISyntaxException, CoreException {
		Yaml yaml = new Yaml();
		InputStream input = new FileInputStream(new File(getCurrentPath(projectName)));
		registry = (LinkedHashMap<?, ?>) yaml.load(input);
	}

}