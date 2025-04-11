package org.hai.jhook;

import javassist.*;
import org.hai.jhook.exception.JHookException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JHookServer implements Runnable {
    @Override
    public void run() {
        // TODO 同名的类 各自重定义
        // TODO 如何获取类的字节码
        System.out.println("start agent");
        try {
            startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startServer() throws IOException {
        InstrumentationHolder.getInst().addTransformer(JHookTransformer.getInstance(), true);
        ServerSocket server = new ServerSocket(7788);
        while (true) {
            Socket client = server.accept();
            new Thread(() -> {
                try {
                    ObjectInputStream input = new ObjectInputStream(client.getInputStream());
                    ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
                    while (!client.isClosed()) {
                        String cmdTypeStr = "";
                        try {
                            cmdTypeStr = input.readUTF();
                            System.out.println(cmdTypeStr);
                        } catch (Exception e) {
                            break;
                        }
                        CommandType cmdType = CommandType.valueOf(cmdTypeStr);
                        ICommand command = CommandFactory.getCommand(cmdType);
                        if (command == null) {
                            throw new JHookException("命令不存在：" + cmdType);
                        }
                        command.execute(input, output);
                        output.flush();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
