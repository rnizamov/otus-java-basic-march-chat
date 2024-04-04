package ru.rnizamov.march.chat.server;

import ru.rnizamov.march.chat.server.servicedb.DataBaseAuthenticationsService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Server {
    private int port;
    private List<ClientHandler> clients;
    private AuthenticationService authenticationService;

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            this.authenticationService = new DataBaseAuthenticationsService("jdbc:postgresql://localhost:5432/otus_chat",
                    "root", "root");
            System.out.println("Сервис аутентификации запущен: " + authenticationService.getClass().getSimpleName());
            System.out.printf("Сервер запущен на порту: %d, ожидаем подключения клиентов\n", port);
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    new ClientHandler(this, socket);
                } catch (Exception e) {
                    System.out.println("Возникла ошибка при обработке подключившегося клиента");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        broadcastMessage("К чату присоединился " + clientHandler.getNickname());
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        if (clients.contains(clientHandler)) {
            clients.remove(clientHandler);
            broadcastMessage("Из чата вышел " + clientHandler.getNickname());
        }
    }

    public synchronized void kickUser(String nickname) {
        ClientHandler client = getClientByNickname(nickname);
        if (client !=null) {
            client.sendMessage("kicked");
        }
    }

    public synchronized ClientHandler getClientByNickname(String nickname) {
        List<ClientHandler> list = clients.stream().filter(e -> e.getNickname().equals(nickname)).collect(Collectors.toList());
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }

    public synchronized void sendMessageToUser(String recipient, String
            sender, String msg) {
        ClientHandler recipientClient = getClientByNickname(recipient);
        ClientHandler senderClient = getClientByNickname(sender);

        if (recipientClient != null) {
            recipientClient.sendMessage(msg);
        } else {
            if (senderClient != null) {
                senderClient.sendMessage("ответ от сервера: нет пользователя с ником " + recipient);
            }
        }
    }

    public synchronized boolean isNicknameBusy(String nickname) {
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(nickname)) {
                return true;
            }
        }
        return false;
    }
}