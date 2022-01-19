package ru.peshekhonov.server.chat;

import ru.peshekhonov.server.chat.auth.AuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {

    private final List<ClientHandler> clients = new ArrayList<>();
    private AuthService authService;

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server has been started");
            authService = new AuthService();
            while (true) {
                waitAndProcessClientConnection(serverSocket);
            }
        } catch (IOException e) {
            System.err.println("Failed to bind port " + port);
            e.printStackTrace();
        }
    }

    private void waitAndProcessClientConnection(ServerSocket serverSocket) throws IOException {
        System.out.println("Waiting for new client connections");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client has been connected");
        ClientHandler clientHandler = new ClientHandler(this, clientSocket);
        clientHandler.handle();
    }

    public synchronized void broadcastMessage(String message, ClientHandler sender) throws IOException {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public synchronized void transmitPersonalMessage(String message, ClientHandler sender, String username) throws IOException {
        for (ClientHandler client : clients) {
            if (client != sender && client.getUser().getUserName().equals(username)) {
                client.sendMessage(message);
                return;
            }
        }
    }

    public synchronized boolean isConnected(String username) {
        for (ClientHandler client : clients) {
            if (client.getUser().getUserName().equals(username)) return true;
        }
        return false;
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        this.clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        this.clients.remove(clientHandler);
    }

    public AuthService getAuthService() {
        return authService;
    }

}
