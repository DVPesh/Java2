package ru.peshekhonov.client.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ru.peshekhonov.client.ClientChat;
import ru.peshekhonov.client.model.Network;
import ru.peshekhonov.client.model.ReadCommandListener;
import ru.peshekhonov.clientserver.Command;
import ru.peshekhonov.clientserver.CommandType;
import ru.peshekhonov.clientserver.commands.ClientMessageCommandData;
import ru.peshekhonov.clientserver.commands.UpdateUserListCommandData;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class ClientController {

    private final static String TO_ALL_USERS_ITEM = "всем";
    @FXML
    public Label label;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField textField;
    @FXML
    private Button sendButton;
    @FXML
    public ListView<String> userList;

    private ReadCommandListener readMessageListener;

    public void sendMessage() {
        Network network = Network.getInstance();
        ClientChat clientChat = ClientChat.INSTANCE;
        Stage authStage = clientChat.getAuthStage();
        Stage chatStage = clientChat.getChatStage();
        AuthController authController = clientChat.getAuthController();

        if (!network.isConnected() || network.isReadMessageListenerPresent(authController.getReadMessageListener())) {

            if (!authStage.isShowing()) {
                authStage.setX(chatStage.getX() + (chatStage.getWidth() - authStage.getWidth()) / 2);
                authStage.setY(chatStage.getY() + (chatStage.getHeight() - authStage.getHeight()) / 2);
                authStage.show();
            }

            if (!network.isReadMessageListenerPresent(authController.getReadMessageListener())) {
                authController.initializeMessageHandler();
            }

            if (readMessageListener != null) network.removeReadMessageListener(readMessageListener);

            textField.clear();
            return;
        }

        String message = textField.getText().trim();

        if (message.isEmpty()) {
            textField.clear();
            return;
        }

        String sender = null;
        if (!userList.getSelectionModel().isEmpty()) {
            sender = userList.getSelectionModel().getSelectedItem();
        }

        try {
            if (message.equals("/END")) {
                System.out.println("The client broke the connection");
                network.sendEndCommand();
                chatStage.setTitle("");
            } else if (sender == null || sender.equals(TO_ALL_USERS_ITEM)) {
                System.out.println("The client has sent a broadcast message");
                network.sendMessage(message);
            } else {
                network.sendPrivateMessage(sender, message);
            }

        } catch (IOException e) {
            clientChat.showErrorDialog("Ошибка передачи данных по сети");
        }

        appendMessageToChat("Я", message);
    }

    private void appendMessageToChat(String sender, String message) {
        textArea.appendText(DateFormat.getDateTimeInstance().format(new Date()));
        textArea.appendText(System.lineSeparator());

        if (sender != null) {
            textArea.appendText(sender + ":");
            textArea.appendText(System.lineSeparator());
        }

        textArea.appendText(message);
        textArea.appendText(System.lineSeparator());
        textArea.appendText(System.lineSeparator());
        textField.setFocusTraversable(true);
        textField.clear();
    }

    public void initializeMessageHandler() {
        readMessageListener = Network.getInstance().addReadMessageListener(new ReadCommandListener() {
            @Override
            public void processReceivedCommand(Command command) {
                if (command.getType() == CommandType.CLIENT_MESSAGE) {
                    ClientMessageCommandData data = (ClientMessageCommandData) command.getData();
                    appendMessageToChat(data.getSender(), data.getMessage());
                } else if (command.getType() == CommandType.UPDATE_USER_LIST) {
                    UpdateUserListCommandData data = (UpdateUserListCommandData) command.getData();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (data.getUsers().size() > 1) data.getUsers().add(TO_ALL_USERS_ITEM);
                            userList.setItems(FXCollections.observableList(data.getUsers()));
                            userList.getSelectionModel().select(TO_ALL_USERS_ITEM);
                        }
                    });
                }
            }
        });
    }
}
