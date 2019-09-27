package net.shivatinker.socketchat.server;

import net.shivatinker.socketchat.User;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Class representing connection with remote client
 */
class ClientConnection {
    private static int currentID = 1;

    private static final Logger log = LoggerFactory.getLogger(ClientConnection.class);

    private final int ID;

    private final User user;
    private final Thread thread;

    private final BufferedReader in;
    private final PrintWriter out;

    private Socket socket;

    private ClientMessageCallback onMessage;
    private Consumer<ClientConnection> onClose;

    private void listen() {
        String line;
        try {
            while ((line = in.readLine()) != null) {
                if (onMessage != null)
                    onMessage.onMessage(this, line);
            }
        } catch (SocketException e) {
            log.info("Socket exception - {}", e.getMessage());
        } catch (IOException e) {
            log.error("Error during client listening", e);
        } finally {
            log.info("Disconnected client {}", this);
            if (onClose != null)
                onClose.accept(this);
        }
    }

    /**
     * Creates new instance of {@link ClientConnection} using pre-opened socket
     *
     * @param socket Opened socket for communication
     * @throws IOException if error occurred while opening IO streams
     */
    ClientConnection(@NotNull Socket socket) throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        this.socket = socket;
        ID = currentID++;
        thread = new Thread(this::listen, String.format("User connection #%d", ID));
        user = new User(String.format("User%d", ID));
    }

    /**
     * Binds onClose interface, which will be called upon connection closing
     *
     * @param onClose Functional interface
     */
    void setOnClose(Consumer<ClientConnection> onClose) {
        this.onClose = onClose;
    }


    /**
     * Binds onMessage interface, which will be called upon receiving message from client
     *
     * @param onMessage Functional interface
     */
    void setOnMessage(ClientMessageCallback onMessage) {
        this.onMessage = onMessage;
    }

    /**
     * @return <b>Unique</b> ID of connection
     */
    public int getID() {
        return ID;
    }


    /**
     * Sends message to client
     *
     * @param message Message to send
     */
    void sendMessage(String message) {
        out.println(message);
    }


    /**
     * Starts listening for messages
     */
    void startListening() {
        thread.start();
    }

    /**
     * Stops listening and closes socket
     *
     * @throws InterruptedException if any thread interrupted current thread
     * @throws IOException          if there was error while closing socket
     */
    void stopListening() throws InterruptedException, IOException {
        socket.close();
        thread.join();
    }

    public boolean isListening() {
        return thread.isAlive();
    }

    @Override
    public String toString() {
        return String.format("%s@%s", user.getNickname(), socket.getInetAddress().getHostName());
    }

    User getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientConnection that = (ClientConnection) o;
        return ID == that.ID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID);
    }
}
