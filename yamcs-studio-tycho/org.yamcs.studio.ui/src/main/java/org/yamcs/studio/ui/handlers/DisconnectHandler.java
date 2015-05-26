package org.yamcs.studio.ui.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.yamcs.studio.core.YamcsPlugin;

public class DisconnectHandler extends AbstractHandler {
    private static final Logger log = Logger.getLogger(DisconnectHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try
        {
            YamcsPlugin.getDefault().disconnect();
        } catch (Exception ex)
        {
            log.log(Level.SEVERE, "", ex);
        }

        return null;
    }
}
