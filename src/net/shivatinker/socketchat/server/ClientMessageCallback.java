package net.shivatinker.socketchat.server;

@FunctionalInterface
public interface ClientMessageCallback {
    void onMessage(ClientConnection connection, String message);
}
