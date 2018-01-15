package org.csstudio.opibuilder.util;

import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;

public class ImplementationLoader {

    /**
     * Load the instance of a class in other plugins.
     * 
     * @param bundleID
     *            symbolic name of the bundle.
     * @param fullClassName
     *            class name with package name included.
     * @param logError
     *            if error should be logged.
     * @return the instance of the loaded class. null if failed.
     */
    public static Object loadObjectInPlugin(String bundleID, String fullClassName, boolean logError) {
        try {
            return Platform.getBundle(bundleID).loadClass(
                    fullClassName).newInstance();
        } catch (Exception e) {
            if (logError)
                OPIBuilderPlugin.getLogger().log(Level.SEVERE,
                        NLS.bind("Failed to load class {0} from plugin {1}.",
                                fullClassName, bundleID),
                        e); // $NON-NLS-2$
            return null;
        }
    }

}
