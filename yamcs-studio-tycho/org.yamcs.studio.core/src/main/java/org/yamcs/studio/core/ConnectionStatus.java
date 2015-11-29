package org.yamcs.studio.core;

/**
 * Connection status of the single shared websocket connection
 */
public enum ConnectionStatus {
    Disconnected, // not connected to Yamcs server
    Connecting,
    Connected,
    Disconnecting,
    ConnectionFailure; // something prevented the connection from ever being established
}
