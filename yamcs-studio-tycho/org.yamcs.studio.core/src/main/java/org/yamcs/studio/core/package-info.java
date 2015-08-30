/**
 * MENTAL NOTE
 * <p>
 * The listener and connection stuff in this package is still in transitioning mode. The eventual
 * goal is that every type of listener interface inherits StudioConnectionListener (AlarmListener,
 * EventListener and so on).
 * <p>
 * Another goal is to refactor code so that views only need to register themselves once. Currently
 * it happens whenever the connection changes, but we could orchestrate that transparently i think.
 * <p>
 * This way, the UI only deals with the 'fact' that something connected or disconnected (for example
 * to clear a table or sth), and doesn't do much more than that.
 */
package org.yamcs.studio.core;
