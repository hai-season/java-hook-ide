package org.hai.jhook;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GetStatusCommand implements Command {
    ObjectInputStream input;
    ObjectOutputStream output;

    public GetStatusCommand(ObjectInputStream input, ObjectOutputStream output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public String getCmdType() {
        return CommandType.GET_STATUS.name();
    }

    @Override
    public String execute() throws IOException {
        output.writeUTF(getCmdType());
        output.writeUTF("hello");
        output.flush();
        return input.readUTF();
    }
}
