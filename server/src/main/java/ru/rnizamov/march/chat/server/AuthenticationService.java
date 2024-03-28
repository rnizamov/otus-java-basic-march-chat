package ru.rnizamov.march.chat.server;

public interface AuthenticationService {
    String getNicknameByLoginAndPassword(String login, String password);
    boolean register(String login, String password, String nickname, Role role);
    boolean isLoginAlreadyExist(String login);
    boolean isNicknameAlreadyExist(String nickname);
    boolean isAdmin(String nickname);
}