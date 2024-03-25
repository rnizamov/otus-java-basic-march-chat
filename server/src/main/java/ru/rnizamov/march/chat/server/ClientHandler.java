package ru.rnizamov.march.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    private static int usersCounter = 0;

    public String getUsername() {
        return username;
    }

    private void generateUsername() {
        usersCounter++;
        this.username = "user" + usersCounter;
    }

    private Map<String, String> extractMsg(String msg) {
        String[] msgArr = msg.split(" ");
        String userName = msgArr[1];
        String leftPart = msgArr[0] + " " + msgArr[1];
        msgArr = msg.split(leftPart);
        String message = msgArr[1].strip();
        return new HashMap<>(){{
            put("recipient", userName);
            put("message", message);
        }};
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.generateUsername();
        new Thread(() -> {
            try {
                System.out.println("Подключился новый клиент");
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/")) {
                        if (msg.startsWith("/exit")) {
                            disconnect();
                            break;
                        }
                        if (msg.startsWith("/w user")) {
                            String recipient = extractMsg(msg).get("recipient");
                            String message = extractMsg(msg).get("message");
                            server.sendMessageToUser(recipient, "входящее сообщение от " + this.username + ": " + message);
                            server.sendMessageToUser(this.username, "исходящее сообщение для " + recipient + ": " + message);
                        }
                        continue;
                    }
                    server.broadcastMessage(username + ": " + msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
