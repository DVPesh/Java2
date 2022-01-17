package ru.geekbrains.java2.lesson6;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {

    public static final int SERVER_PORT = 8100;
    private static DataInputStream in;
    private static DataOutputStream out;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {

            System.out.println("Сервер запущен");
            Socket socket = serverSocket.accept();
            System.out.println("Клиент подключился");
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            Thread receiver = new Thread(Server::receiveMessage);
            Thread transmitter = new Thread(Server::sendMessage);

            transmitter.setDaemon(true);
            receiver.start();
            transmitter.start();
            receiver.join();
            System.out.println("Сервер штатно отключился");

        } catch (IOException | InterruptedException e) {
            System.err.println("основной поток -> " + e.getMessage());
        }
    }

    private static void receiveMessage() {
        try {
            while (true) {
                String message = in.readUTF();
                if (message.equals("/end")) {
                    break;
                }
                System.out.println(message);
            }
        } catch (IOException e) {
            System.err.println("поток ввода -> " + e.getMessage());
        }
    }

    private static void sendMessage() {
        Scanner scanner = new Scanner(System.in);
        try {
            while (true) {
                String message = scanner.nextLine();
                out.writeUTF(message);
            }
        } catch (IOException e) {
            System.err.println("поток вывода -> " + e.getMessage());
        }
    }

}
