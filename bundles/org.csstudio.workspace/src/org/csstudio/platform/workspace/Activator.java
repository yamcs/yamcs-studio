package org.csstudio.platform.workspace;

import org.eclipse.core.runtime.Plugin;

/** Plugin activator.
 *  @author Kay Kasemir
 */
public class Activator extends Plugin
{
    final public static String ID = "org.csstudio.platform.workspace"; 
    private static Activator plugin = null;

    public Activator()
    {
        plugin = this;
    }

    public static Activator getInstance()
    {
        return plugin;
    }
}
