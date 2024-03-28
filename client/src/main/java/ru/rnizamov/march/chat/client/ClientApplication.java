package ru.rnizamov.march.chat.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientApplication {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try (
                Socket socket = new Socket("localhost", 8189);
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream())
        ) {
            System.out.println("Подключились к серверу");
            AtomicBoolean isConnect = new AtomicBoolean(true);
            Thread thread = new Thread(() -> {
                try {
                    while (true) {
                        String inMessage = in.readUTF();
                        if (inMessage.equals("kicked")) {
                            System.out.println("Вероятно вы нарушили правила сообщества и были исключены с сервера!");
                            isConnect.set(false);
                            out.writeUTF("/exit");  //todo костыль, чтобы выкидывать с сервера клиента и не ловить эксепшн
                            break;
                        }
                        System.out.println(inMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thread.setDaemon(true);
            thread.start();
            while (true) {
                if (!isConnect.get()) break;
                String msg = scanner.nextLine();
                out.writeUTF(msg);
                if (msg.equals("/exit")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}