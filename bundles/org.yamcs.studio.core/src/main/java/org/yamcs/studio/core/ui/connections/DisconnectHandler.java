package org.yamcs.studio.core.ui.connections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.yamcs.studio.core.YamcsPlugin;

public class DisconnectHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        YamcsPlugin.disconnect(false /* lost */);
        return null;
    }
}
