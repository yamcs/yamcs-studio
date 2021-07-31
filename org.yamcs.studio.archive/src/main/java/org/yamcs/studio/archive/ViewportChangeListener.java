package org.yamcs.studio.archive;

@FunctionalInterface
public interface ViewportChangeListener {

    void onEvent(ViewportChangeEvent event);
}
