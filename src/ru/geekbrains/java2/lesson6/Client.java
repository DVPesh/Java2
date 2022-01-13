package ru.geekbrains.java2.lesson6;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static final String SERVER_ADDRESS = "localhost";

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, Server.SERVER_PORT);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            Thread receiver = new Thread(() -> {
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
            });

            Thread transmitter = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                try {
                    while (true) {
                        String message = scanner.nextLine();
                        out.writeUTF(message);
                    }
                } catch (IOException e) {
                    System.err.println("поток вывода -> " + e.getMessage());
                }
            });

            transmitter.setDaemon(true);
            receiver.start();
            transmitter.start();
            receiver.join();
            System.out.println("Клиент штатно отключился");

        } catch (IOException | InterruptedException e) {
            System.err.println("основной поток -> " + e.getMessage());
        }
    }

}
