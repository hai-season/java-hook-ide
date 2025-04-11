package org.hai.jhook.client;

import org.hai.jhook.CommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class JHookClient {
    private static String ip = "127.0.0.1";
    private static int port = 7788;
    private final Logger logger = LoggerFactory.getLogger(JHookClient.class);
    private final Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public JHookClient(Socket socket) {
        this.socket = socket;
    }

    public static JHookClient connect() throws Exception {
        Socket client = new Socket(ip, port);
        JHookClient jHookClient = new JHookClient(client);
        jHookClient.output = new ObjectOutputStream(client.getOutputStream());
        jHookClient.input = new ObjectInputStream(client.getInputStream());
        return jHookClient;
    }

    public static void setConnectInfo(String ip, int port) {
        JHookClient.ip = ip;
        JHookClient.port = port;
    }

    public void disconnect() {
        try {
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
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
        output.writeInt(line);
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
