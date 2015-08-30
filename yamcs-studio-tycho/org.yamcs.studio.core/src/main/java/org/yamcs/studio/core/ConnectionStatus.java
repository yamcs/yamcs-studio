package org.yamcs.studio.core;

public enum ConnectionStatus {
    Disconnected, // no clients (WebSocket, HornetQ) are connected to Yamcs server
    Connecting,
    Connected, // all clients are connected
    Disconnecting,
    ConnectionFailure; // something prevented the connection from ever being established
}
