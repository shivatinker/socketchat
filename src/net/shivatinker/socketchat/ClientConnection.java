package net.shivatinker.socketchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnection {
    private static int threadIndex = 1;

    private Socket socket;
    private Thread thread;


    private void listen() {

    }

    public ClientConnection(Socket socket) throws IOException {
        this.socket = socket;
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        thread = new Thread(this::listen, String.format("Client #%d", threadIndex++));
    }

    public void startListening(){

    }
}
