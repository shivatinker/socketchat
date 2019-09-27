package net.shivatinker.socketchat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;

/**
 * Represents socketchat client
 */
public class ChatClient {
    private static final Logger log = LoggerFactory.getLogger(ChatClient.class);
    private Socket socket;
    private Thread thread;

    private String host;
    private int port;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> onMessage;

    public static void main(String[] args) {
        ChatClient client = new ChatClient(args[0], Integer.valueOf(args[1]));
        client.setOnMessage(System.out::println);
        try {
            client.connect();
        } catch (IOException e) {
            log.error("Unable to connect to {}:{} - {}", client.host, client.port, e.getMessage());
            System.exit(-1);
        }

        BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (client.isConnected()) if (cin.ready()) client.sendMessage(cin.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param host Host to connect
     * @param port Remote port
     */
    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void listen() {
        log.info("Connected to {}", socket.getInetAddress());
        String line;
        try {
            while ((line = in.readLine()) != null)
                if (onMessage != null)
                    onMessage.accept(line);
        } catch (SocketException e) {
            log.info("Socket exception - {}", e.getMessage());
        } catch (IOException e) {
            log.error("Error during listening", e);
        } finally {
            log.info("Disconnected");
        }
    }

    /**
     * Connects this client with server, specified in constructor
     *
     * @throws IOException if error occurred while connecting to server
     */
    public void connect() throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        thread = new Thread(this::listen, "Chat listener");
        thread.start();
    }

    /**
     * Binds callback to server message events
     *
     * @param onMessage Functional interface, that will be called upon receiving message from server
     */
    public void setOnMessage(Consumer<String> onMessage) {
        this.onMessage = onMessage;
    }

    /**
     * Sends message to server
     *
     * @param message Message to send
     */
    public void sendMessage(String message) {
        out.println(message);
    }

    /**
     * Returns true if connection established
     *
     * @return <code>listeningThread.isAlive</code>
     */
    public boolean isConnected() {
        return thread.isAlive();
    }
}
