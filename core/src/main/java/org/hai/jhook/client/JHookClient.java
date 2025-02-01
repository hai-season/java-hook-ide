package org.hai.jhook.client;

import org.hai.jhook.CommandType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class JHookClient {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public JHookClient(Socket socket) {
        this.socket = socket;
    }

    public static JHookClient connect() throws Exception {
        Socket client = new Socket("127.0.0.1", 9090);
        return new JHookClient(client);
    }

    public byte[] getClassByteCode() throws IOException {
        output = new ObjectOutputStream(socket.getOutputStream());
        input = new ObjectInputStream(socket.getInputStream());
        output.writeUTF(CommandType.GET_CLASS.name());
        int len = input.readInt();
        byte[] data = new byte[len];
        input.readFully(data);
        return data;
    }
}
