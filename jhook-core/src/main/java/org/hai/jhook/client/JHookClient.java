package org.hai.jhook.client;

import org.hai.jhook.CommandType;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class JHookClient {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public JHookClient(Socket socket) {
        this.socket = socket;
    }

    public static JHookClient connect() throws Exception {
        Socket client = new Socket("127.0.0.1", 9090);
        JHookClient jHookClient = new JHookClient(client);
        System.out.println("writeStreamHeader");
        jHookClient.output = new ObjectOutputStream(client.getOutputStream());
        System.err.println("readStreamHeader");
        jHookClient.input = new ObjectInputStream(client.getInputStream());
        return jHookClient;
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getClassByteCode(String className) throws IOException {
        output.writeUTF(CommandType.GET_CLASS.name());
        output.writeUTF(className);
        output.flush();
        int len = input.readInt();
        byte[] data = new byte[len];
        input.readFully(data);
        return data;
    }

    public String redefineClass(String className, String methodName, String position, Integer line, String code) throws IOException {
        output.writeUTF(CommandType.REDEFINE_CLASS.name());
        output.writeUTF(className);
        output.writeUTF(methodName);
        output.writeUTF(position);
        output.writeUTF(code);
        output.flush();
        return input.readUTF();
    }

    public List<String> listClass() throws IOException, ClassNotFoundException {
        output.writeUTF(CommandType.LIST_CLASS.name());
        output.flush();
        return (List<String>) input.readObject();
    }

    public List<String> listMethod(String className) throws IOException, ClassNotFoundException {
        output.writeUTF(CommandType.LIST_METHOD.name());
        output.writeUTF(className);
        output.flush();
        return (List<String>) input.readObject();
    }
}
