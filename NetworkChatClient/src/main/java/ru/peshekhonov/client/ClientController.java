package ru.peshekhonov.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.function.Consumer;

public class ClientController {

    @FXML
    private TextArea textArea;
    @FXML
    private TextField textField;
    @FXML
    private Button sendButton;
    @FXML
    public ListView<String> userList;

    private ClientChat application;

    public void sendMessage() {
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
            if (!message.startsWith("/w ") && sender != null) message = String.join(": ", sender, message);
            Network.getInstance().sendMessage(message);
        } catch (IOException e) {
            application.showErrorDialog("Ошибка передачи данных по сети");
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

    public void setApplication(ClientChat application) {
        this.application = application;
    }

    public void initializeMessageHandler() {
        Network.getInstance().waitMessages(new Consumer<String>() {
            @Override
            public void accept(String message) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        appendMessageToChat("Server", message);
                    }
                });
            }
        });
    }

}
