package ru.peshekhonov.client.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.peshekhonov.client.ClientChat;
import ru.peshekhonov.client.dialogs.Dialogs;
import ru.peshekhonov.client.model.Network;
import ru.peshekhonov.client.model.ReadCommandListener;
import ru.peshekhonov.clientserver.Command;
import ru.peshekhonov.clientserver.CommandType;
import ru.peshekhonov.clientserver.commands.AuthOkCommandData;
import ru.peshekhonov.clientserver.commands.ErrorCommandData;

import java.io.IOException;

public class AuthController {

    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button authButton;

    private ReadCommandListener readMessageListener;

    @FXML
    public void executeAuth(ActionEvent actionEvent) {
        String login = loginField.getText();
        String password = passwordField.getText();
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            Dialogs.AuthError.EMPTY_CREDENTIALS.show();
            return;
        }

        if (!connectToServer()) {
            Dialogs.NetworkError.SERVER_CONNECT.show();
        }

        try {
            Network.getInstance().sendAuthMessage(login, password);
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            Dialogs.NetworkError.SEND_MESSAGE.show();
            e.printStackTrace();
        }
    }

    private boolean connectToServer() {
        Network network = Network.getInstance();
        return network.isConnected() || network.connect();
    }

    public void initializeMessageHandler() {
        readMessageListener = getNetwork().addReadMessageListener(new ReadCommandListener() {
            @Override
            public void processReceivedCommand(Command command) {
                if (command.getType() == CommandType.AUTH_OK) {
                    AuthOkCommandData data = (AuthOkCommandData) command.getData();
                    String username = data.getUsername();
                    Platform.runLater(() -> ClientChat.INSTANCE.switchToMainChatWindow(username));
                } else if (command.getType() == CommandType.ERROR) {
                    ErrorCommandData data = (ErrorCommandData) command.getData();
                    Platform.runLater(() -> {
                        Dialogs.AuthError.INVALID_CREDENTIALS.show(data.getErrorMessage());
                    });
                }
            }
        });
    }

    public void close() {
        getNetwork().removeReadMessageListener(readMessageListener);
    }

    private Network getNetwork() {
        return Network.getInstance();
    }

    public ReadCommandListener getReadMessageListener() {
        return readMessageListener;
    }
}
