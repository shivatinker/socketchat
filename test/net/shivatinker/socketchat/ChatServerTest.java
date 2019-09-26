package net.shivatinker.socketchat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

class ChatServerTest {

    ChatServer chatServer;

    @BeforeEach
    void setUp() {
        chatServer = new ChatServer("127.0.0.1", 4355);
    }

    @Order(1)
    @Test
    void start() {
        Assertions.assertDoesNotThrow(chatServer::start);
        Assertions.assertTrue(chatServer.isListening());
        Assertions.assertThrows(IllegalStateException.class, chatServer::start);
        Assertions.assertTrue(chatServer.isListening());
    }

    @Order(2)
    @Test
    void stop() {
        Assertions.assertDoesNotThrow(chatServer::stop);
        Assertions.assertFalse(chatServer.isListening());
        Assertions.assertDoesNotThrow(chatServer::stop);
        Assertions.assertFalse(chatServer.isListening());
    }
}