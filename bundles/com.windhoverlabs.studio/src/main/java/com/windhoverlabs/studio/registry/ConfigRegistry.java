package com.windhoverlabs.studio.registry;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.yamcs.client.YamcsClient;
import org.yamcs.studio.core.YamcsPlugin;
import org.eclipse.core.runtime.CoreException;

import com.windhoverlabs.studio.registry.preferences.RegistryPreferencePage;

/**
 * @author lgomez Abstract class configuration registry that behaves like a dictionary. This is meant to make loading
 *         CFS configuration format-agnostic. Meaning it does not matter if the configuration is stored in a YAML file,
 *         SQLite or even XML file; this class enforces implementors to treat the data as a dictionary, or a
 *         LinkedHashMap concretely speaking. The only prerequisite for the ConfigRegistry is that the file
 *         storing(SQLite, YAML, etc) the registry follows the modules schema. Read docs for details about the schema.
 */
public abstract class ConfigRegistry {
    public final static String PATH_SEPARATOR = "/";
    protected LinkedHashMap<?, ?> registry;

    enum MSGType {
        COMMAND,
        TELEMETRY
    }

    /**
     * Subclasses should load the registry(YAML, SQLite, etc) and store it in registry on the constructor.
     * 
     * @throws FileNotFoundException
     * @throws URISyntaxException
     * @throws CoreException
     */
    protected ConfigRegistry() {

    }

    /**
     * Helper function for subclasses to get the current registry path that is currently set by the user.
     * 
     * @param projectName
     * @return
     * @throws URISyntaxException
     * @throws CoreException
     */
    protected String getCurrentPath() throws URISyntaxException, CoreException {
        return RegistryPreferencePage.getCurrentPath();
    }

    /**
     * @param registryPath
     *            The path to the location of inside the configuration registry. The path is expected to be in the
     *            "/A/C/D" format. A "/" means root, which returns the entire registry.
     * @return A LinkedHashMap containing the data at registryPath.
     * @throws Exception
     */
    public Object get(String registryPath) throws Exception {
        if (registry == null) {
            throw new Exception(
                    "The registry has not been loaded. Make sure this implementation is loading the registry in the constructor.");
        }

        if (registryPath.charAt(0) != PATH_SEPARATOR.charAt(0)) {
            throw new Exception("The path to a registry node must start with " + PATH_SEPARATOR);
        }

        if (registryPath.length() == 1) {
            return registry;
        }

        String[] node_keys = registryPath.split(PATH_SEPARATOR);

        LinkedHashMap<?, ?> registryPartition = (LinkedHashMap<?, ?>) registry.clone();

        // Start at second node to avoid the first node which has empty("") string
        for (int i = 1; i < node_keys.length; i++) {
            if (registryPartition.containsKey(node_keys[i])) {
                if (registryPartition.get(node_keys[i]) instanceof LinkedHashMap<?, ?>) {
                    registryPartition = (LinkedHashMap<?, ?>) registryPartition.get(node_keys[i]);
                } else {
                    if (node_keys.length - 1 == i) {
                        return registryPartition.get(node_keys[i]);
                    } else {
                        throw new Exception("The node " + "\"" + node_keys[i] + "\""
                                + " does not point to a dictinaory. Revise your registry path");
                    }
                }
            } else {
                throw new Exception("The node " + "\"" + node_keys[i] + "\""
                        + " does not exist in the registry. Revise your registry path");
            }
        }

        return registryPartition;

    }

    public static boolean isPathValid(String path) {
        boolean isValid = true;

        if (path.isEmpty()) {
            isValid = false;
        }

        else if (path.charAt(0) != PATH_SEPARATOR.charAt(0)) {
            isValid = false;
        }

        return isValid;
    }

    /**
     * Convenience function to append more nodes to path. For example; the call appendPath("/root/path", "new/node")
     * will return "/root/path/new/node".
     * 
     * @param rootPath
     * @param newNodes
     * @return
     * @throws Exception
     */

    public static String appendPath(String rootPath, String newNodes) throws Exception {

        if (newNodes.isEmpty()) {
            throw new Exception("The new path must NOT be empty.");
        }

        if (isPathValid(rootPath) == false) {
            throw new Exception(String.format("Root path %s is not valid.", rootPath));
        }

        // Cleanup the paths
        if (rootPath.charAt(rootPath.length() - 1) == PATH_SEPARATOR.charAt(0)) {
            rootPath = rootPath.substring(0, rootPath.length() - 1);
        }

        if (newNodes.charAt(newNodes.length() - 1) == PATH_SEPARATOR.charAt(0)) {
            newNodes = newNodes.substring(0, newNodes.length() - 1);
        }

        if (newNodes.charAt(0) == PATH_SEPARATOR.charAt(0)) {
            newNodes = newNodes.substring(1);
        }

        String newPath = rootPath + PATH_SEPARATOR + newNodes;
        return newPath;
    }

    private void getAllTelemetry(LinkedHashMap<?, ?> modules, LinkedHashMap<Object, Object> outMsgIds) {
        for (Map.Entry<?, ?> moduleSet : modules.entrySet()) {
            LinkedHashMap<?, ?> module = ((LinkedHashMap<?, ?>) modules.get(moduleSet.getKey()));

            if (module.get("modules") != null) {
                getAllTelemetry((LinkedHashMap<?, ?>) module.get("modules"), outMsgIds);
            }
            if (module.get("telemetry") != null) {
                LinkedHashMap<?, ?> Alltlm = (LinkedHashMap<?, ?>) module.get("telemetry");

                for (Map.Entry<?, ?> tlmSet : Alltlm.entrySet()) {
                    LinkedHashMap<Object, Object> tlm = (LinkedHashMap<Object, Object>) Alltlm.get(tlmSet.getKey());
                    tlm.put("type", MSGType.TELEMETRY);
                    tlm.put("macro", tlmSet.getKey());
                    tlm.put("app", moduleSet.getKey());

                    if (tlm.get("struct") != null) {
                        tlm.remove("struct");
                    }
                    outMsgIds.put(tlmSet.getKey(), tlmSet.getValue());
                }

            }
        }

    }

    /**
     * 
     * @param modules
     * @param outConfiguration
     */
    private void getAllConfiguration(LinkedHashMap<?, ?> modules, LinkedHashMap<Object, Object> outConfiguration) {
        for (Map.Entry<?, ?> moduleSet : modules.entrySet()) {
            LinkedHashMap<?, ?> module = ((LinkedHashMap<?, ?>) modules.get(moduleSet.getKey()));

            if (module.get("modules") != null) {
                getAllConfiguration((LinkedHashMap<?, ?>) module.get("modules"), outConfiguration);
            }
            if (module.get("config") != null) {
                LinkedHashMap<?, ?> AllConfig = (LinkedHashMap<?, ?>) module.get("config");

                outConfiguration.put(moduleSet.getKey(), AllConfig);

            }
        }

    }

    private void getAllTeleCommands(LinkedHashMap<?, ?> modules, LinkedHashMap<Object, Object> outCmdMsgIds) {
        for (Map.Entry<?, ?> moduleSet : modules.entrySet()) {
            LinkedHashMap<?, ?> module = ((LinkedHashMap<?, ?>) modules.get(moduleSet.getKey()));

            if (module.get("modules") != null) {
                getAllTeleCommands((LinkedHashMap<?, ?>) module.get("modules"), outCmdMsgIds);
            }
            if (module.get("commands") != null) {
                LinkedHashMap<?, ?> cmds = (LinkedHashMap<?, ?>) module.get("commands");

                for (Map.Entry<?, ?> cmdSet : cmds.entrySet()) {
                    LinkedHashMap<Object, Object> cmd = (LinkedHashMap<Object, Object>) cmds.get(cmdSet.getKey());

                    LinkedHashMap<Object, Object> subCommands = (LinkedHashMap<Object, Object>) cmd.get("commands");

                    if (subCommands != null) {
                        cmd.put("type", MSGType.COMMAND);
                        cmd.put("macro", cmdSet.getKey());

                        cmd.put("app", moduleSet.getKey());

                        for (Map.Entry<?, ?> subCommandSet : subCommands.entrySet()) {
                            LinkedHashMap<Object, Object> subCommand = (LinkedHashMap<Object, Object>) subCommands
                                    .get(subCommandSet.getKey());

                            // Remove the struct node
                            if (subCommand.get("struct") != null) {
                                subCommand.remove("struct");
                            }
                        }
                        outCmdMsgIds.put(cmdSet.getKey(), cmdSet.getValue());
                    }
                }
            }
        }

    }

    /**
     * Get all messages from the registry. This includes only telemetry.
     * 
     * @return A map of messages in the following format: { HK_HK_TLM_MID: { msgID: 2158, type: TELEMETRY, macro:
     *         HK_HK_TLM_MID, app: hk } }
     * @throws Exception
     */
    public LinkedHashMap<Object, Object> getAllTelemetry() throws Exception {
        LinkedHashMap<Object, Object> outMsgMap = new LinkedHashMap<Object, Object>();

        // Access the registry through the get method for error-checking
        LinkedHashMap<?, ?> wholeRegistry = (LinkedHashMap<?, ?>) this.get("/modules");
        getAllTelemetry(wholeRegistry, outMsgMap);

        return outMsgMap;

    }

    /**
     * Get all messages from the registry. This includes only commands.
     * 
     * @return A map of messages in the following format: { HK_SEND_COMBINED_PKT_MID: { msgID: 6256, commands:
     *         {SendCombinedPkt={cc=0}}, type: COMMAND, macro: HK_SEND_COMBINED_PKT_MID, app: hk } }
     * @throws Exception
     */
    public LinkedHashMap<Object, Object> getAllCommands() throws Exception {
        LinkedHashMap<Object, Object> outCmdMap = new LinkedHashMap<Object, Object>();

        // Access the registry through the get method for error-checking
        LinkedHashMap<?, ?> wholeRegistry = (LinkedHashMap<?, ?>) this.get("/modules");
        getAllTeleCommands(wholeRegistry, outCmdMap);

        return outCmdMap;

    }

    /**
     * Get all messages from the registry. This includes commands and telemetry messages.
     * 
     * @return A map of messages in the following format: { HK_HK_TLM_MID: { msgID: 2158, type: TELEMETRY, macro:
     *         HK_HK_TLM_MID, app: hk }, HK_SEND_COMBINED_PKT_MID: { msgID: 6256, commands: {SendCombinedPkt={cc=0}},
     *         type: COMMAND, macro: HK_SEND_COMBINED_PKT_MID, app: hk } }
     * @throws Exception
     */
    public LinkedHashMap<Object, Object> getAllMessages() throws Exception {
        LinkedHashMap<Object, Object> outCmdMap = new LinkedHashMap<Object, Object>();

        // Access the registry through the get method for error-checking
        LinkedHashMap<?, ?> wholeRegistry = (LinkedHashMap<?, ?>) this.get("/modules");
        getAllTeleCommands(wholeRegistry, outCmdMap);
        getAllTelemetry(wholeRegistry, outCmdMap);

        return outCmdMap;

    }

    /**
     * 
     * @return All of the configuration from all of the apps/modules stored in the registry in the following format:
     * 
     *         { cfe_es: { CFE_ES_CDS_MAX_NAME_LENGTH={name=CFE_ES_CDS_MAX_NAME_LENGTH, value=16} }, to: {
     *         TO_MAX_MESSAGE_FLOWS={name=TO_MAX_MESSAGE_FLOWS, value=200} } }
     * 
     * 
     * @throws Exception
     */
    public LinkedHashMap<Object, Object> getAllConfig() throws Exception {
        LinkedHashMap<Object, Object> outConfigMap = new LinkedHashMap<Object, Object>();

        // Access the registry through the get method for error-checking
        LinkedHashMap<?, ?> wholeRegistry = (LinkedHashMap<?, ?>) this.get("/modules");
        getAllConfiguration(wholeRegistry, outConfigMap);

        return outConfigMap;

    }
}
