package ru.peshekhonov.client.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.peshekhonov.client.ClientChat;
import ru.peshekhonov.client.Network;

import java.io.IOException;
import java.util.function.Consumer;

public class AuthController {

    public static final String AUTH_COMMAND = "/auth";
    public static final String AUTH_OK_COMMAND = "/authOk";

    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button authButton;

    private ClientChat clientChat;

    @FXML
    public void executeAuth(ActionEvent actionEvent) {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            clientChat.showErrorDialog("Логин и пароль должны быть указаны");
            return;
        }

        String authCommandMessage = String.format("%s %s %s", AUTH_COMMAND, login, password);

        try {
            Network.getInstance().sendMessage(authCommandMessage);
        } catch (IOException e) {
            clientChat.showErrorDialog("Ошибка передачи данных по сети");
            e.printStackTrace();
        }
    }

    public void setClientChat(ClientChat clientChat) {
        this.clientChat = clientChat;
    }

    public void initializeMessageHandler() {
        Network.getInstance().waitMessages(new Consumer<String>() {
            @Override
            public void accept(String message) {
                if (message.startsWith(AUTH_OK_COMMAND)) {
                    String[] parts = message.split(" ");
                    String userName = parts[1];
                    Thread.currentThread().interrupt();
                    Platform.runLater(() -> {
                        clientChat.getChatStage().setTitle(userName);
                        clientChat.getAuthStage().close();
                    });
                } else {
                    Platform.runLater(() -> {
                        clientChat.showErrorDialog(message);
                    });
                }
            }
        });
    }

}
