package ru.rnizamov.march.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Server {
    private int port;
    private List<ClientHandler> clients;

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("Сервер запущен на порту: %d, ожидаем подключения клиентов\n", port);
            while (true) {
                Socket socket = serverSocket.accept();
                subscribe(new ClientHandler(this, socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }

    public synchronized void sendMessageToUser(String recipient, String
            sender, String msg) {
        List<ClientHandler> listRecipient = clients.stream().filter(e -> e.getUsername().equals(recipient)).collect(Collectors.toList());
        List<ClientHandler> listSender = clients.stream().filter(e -> e.getUsername().equals(sender)).collect(Collectors.toList());

        if (listRecipient.size() > 0) {
            listRecipient.get(0).sendMessage(msg);
        } else {
            if (listSender.size() > 0) {
                listSender.get(0).sendMessage("ответ от сервера: нет пользователя с ником " + recipient);
            }
        }
    }
}
