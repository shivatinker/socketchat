package net.shivatinker.socketchat.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

class ChatServerTest {

    private static final int PORT = 4355;
    private ChatServer chatServer;

    @BeforeEach
    void setUp() {
        chatServer = new ChatServer("127.0.0.1", PORT);
    }

    @Test
    @Order(1)
    void startStop() {
        Assertions.assertDoesNotThrow(chatServer::start);
        Assertions.assertTrue(chatServer.isListening());
        Assertions.assertThrows(IllegalStateException.class, chatServer::start);
        Assertions.assertTrue(chatServer.isListening());
        Assertions.assertDoesNotThrow(chatServer::stop);
        Assertions.assertFalse(chatServer.isListening());
        Assertions.assertDoesNotThrow(chatServer::stop);
        Assertions.assertFalse(chatServer.isListening());

    }


    @Test
    @Order(2)
    void testConnect() throws IOException, InterruptedException {
        Assertions.assertDoesNotThrow(chatServer::start);
        Assertions.assertTrue(chatServer.isListening());
        Thread.sleep(100);

        Socket socket;
        PrintWriter out;

        socket = new Socket("127.0.0.1", PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        out.println("Hello, World!1");
        socket.close();

        socket = new Socket("127.0.0.1", PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        out.println("Hello, World!2");
        //socket.close();

        Thread.sleep(200);
        out.println("Hello, World!2");
        Thread.sleep(200);

        Assertions.assertDoesNotThrow(chatServer::stop);
    }
}