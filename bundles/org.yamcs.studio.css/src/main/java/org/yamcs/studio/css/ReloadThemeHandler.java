package org.yamcs.studio.css;

import org.csstudio.opibuilder.util.MediaService;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class ReloadThemeHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        MediaService.getInstance().reload();
        return null;
    }
}
