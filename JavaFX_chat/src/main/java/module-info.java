module com.example.javafx_chat {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.javafx_chat to javafx.fxml;
    exports com.example.javafx_chat;
}