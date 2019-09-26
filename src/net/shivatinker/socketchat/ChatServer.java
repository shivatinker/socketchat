package net.shivatinker.socketchat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class ChatServer {

    private final int port;

    private Thread serverThread = new Thread(this::listen, "Chat Server");


    private volatile boolean isListeting = false;
    private ServerSocket serverSocket;

    public ChatServer(String host, int port) {
        this.port = port;
    }

    private void listen() {
        try {
            while (isListeting) {
                Socket accepted = serverSocket.accept();

            }
        } catch (IOException e) {
            System.err.println("Exception in listening loop");
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Cannot close socket!");
                e.printStackTrace();
            }
            requestStop();
        }
    }

    private void requestStart() {
        isListeting = true;
    }

    private void requestStop() {
        isListeting = false;
    }

    /**
     * Starts server listening thread.<br/>
     *
     * @throws IOException           if there was an exception creating socket<br/>
     * @throws IllegalStateException if server is already running<br/>
     */
    public void start() throws IOException {
        if (isListeting)
            throw new IllegalStateException("Server is already running");
        serverSocket = new ServerSocket(port);
        requestStart();
        serverThread.start();
    }

    /**
     * Requests main server loop to stop.<br/>
     * <b>This method is blocking</b><br/>
     *
     * @throws InterruptedException if any thread interrupted current thread
     */
    public void stop() throws InterruptedException {
        requestStop();
        serverThread.join();
    }

    public boolean isListening() {
        return isListeting;
    }
}
