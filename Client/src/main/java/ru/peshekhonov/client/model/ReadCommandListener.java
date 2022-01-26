package ru.peshekhonov.client.model;

import ru.peshekhonov.clientserver.Command;

public interface ReadCommandListener {

    void processReceivedCommand(Command command);

}