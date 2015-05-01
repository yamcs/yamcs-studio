package org.yamcs.studio.core.archive;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

/**
 * Used in core-expressions define defined in plugin.xml to keep track of enabled/disabled state of
 * the zoom-out button in the ArchiveView toolbar.
 */
public class ZoomOutCommandState extends AbstractSourceProvider {

    public static final String STATE_KEY_ENABLED = "org.yamcs.studio.core.archive.state.zoomOutEnabled";
    private static final String[] SOURCE_NAMES = { STATE_KEY_ENABLED };

    private boolean enabled = false;

    public void setEnabled(boolean enabled) {
        fireSourceChanged(ISources.WORKBENCH, STATE_KEY_ENABLED, enabled);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map getCurrentState() {
        Map map = new HashMap(1);
        map.put(STATE_KEY_ENABLED, enabled);
        return map;
    }

    @Override
    public String[] getProvidedSourceNames() {
        return SOURCE_NAMES;
    }

    @Override
    public void dispose() {
    }
}
