package net.shivatinker.socketchat.server;

import net.shivatinker.socketchat.User;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

/**
 * Class representing socketchat server.
 */
public class ChatServer {

    private static final Logger log = LoggerFactory.getLogger(ChatServer.class);

    private static final User systemUser = new User("Server");

    private final int port;
    private ServerSocket serverSocket;
    private final Set<ClientConnection> clients = new HashSet<>();
    private final Thread serverThread = new Thread(this::listen, "Chat Server");

    /**
     * Creates a new instance of {@link ChatServer}
     *
     * @param host Host where server socket will bind
     * @param port Socket port
     */
    public ChatServer(String host, int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer(args[0], Integer.valueOf(args[1]));
        try {
            server.start();
        } catch (IOException e) {
            log.error("Unable to start server", e);
            System.exit(-1);
        }
        Thread consoleListener = new Thread(server::consoleListener, "Console listener");
        consoleListener.start();
    }

    private void consoleListener() {
        BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (isListening()) if (cin.ready()) {
                broadcastMessage(systemUser, cin.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeConnection(ClientConnection connection) {
        clients.remove(connection);
    }

    private void processIncomingMessage(@NotNull ClientConnection connection, String message) {
        //connection.sendMessage(String.format("Your ID is %d. Recieved: %s", connection.getID(), message));
        broadcastMessage(connection.getUser(), message);
    }

    private void broadcastMessage(User from, String message) {
        System.out.println(getMessageString(from, message));
        synchronized (clients) {
            for (ClientConnection connection : clients)
                connection.sendMessage(getMessageString(from, message));
        }
    }

    private String getMessageString(User from, String message) {
        return String.format("[%s]: %s", from.getNickname(), message);
    }

    private void listen() {
        log.info("Listening on port {}", port);
        try {
            while (!Thread.interrupted()) {
                Socket accepted;
                accepted = serverSocket.accept();
                ClientConnection connection = new ClientConnection(accepted);
                connection.setOnMessage(this::processIncomingMessage);
                connection.setOnClose(this::removeConnection);
                log.info("Connected client {}", connection);
                connection.startListening();
                clients.add(connection);
            }
        } catch (SocketException e) {
            log.info("Socket closed");
        } catch (IOException e) {
            log.error("Exception in listening loop", e);
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                log.error("Cannot close socket!", e);
            }
        }
    }


    /**
     * Starts socketchat server.<br/>
     *
     * @throws IOException           if there was an exception creating socket<br/>
     * @throws IllegalStateException if server is already running<br/>
     */
    public void start() throws IOException {
        if (serverThread.isAlive())
            throw new IllegalStateException("Server is already running");
        log.info("Starting listening on port {}", port);
        serverSocket = new ServerSocket(port);
        serverThread.start();
    }

    /**
     * Requests main server loop to stop.<br/>
     * <b>This method is blocking</b><br/>
     *
     * @throws InterruptedException if any thread interrupted current thread
     */
    public void stop() throws InterruptedException, IOException {
        if (!serverThread.isAlive())
            return;
        log.info("Stopping listening for new connections...");
        serverThread.interrupt();
        serverSocket.close();
        serverThread.join();
        log.info("Closing existing connections...");
        for (ClientConnection a : clients)
            a.stopListening();
        log.info("Server stopped");
    }

    /**
     * Broadcasts message to all users from server's name
     *
     * @param message Message to be broadcasted
     */
    public void broadcastFromServer(String message) {
        broadcastMessage(systemUser, message);
    }

    /**
     * Returns true if server is listening for new connections
     *
     * @return <code>serverThread.isAlive()</code>
     */
    public boolean isListening() {
        return serverThread.isAlive();
    }
}
