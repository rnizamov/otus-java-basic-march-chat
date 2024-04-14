package ru.rnizamov.march.chat.server;

import java.sql.SQLException;

public interface AuthenticationService {
    String getNicknameByLoginAndPassword(String login, String password) throws SQLException;
    boolean register(String login, String password, String nickname, Role role) throws SQLException;
    boolean isLoginAlreadyExist(String login) throws SQLException;
    boolean isNicknameAlreadyExist(String nickname) throws SQLException;
    boolean isAdmin(String nickname) throws SQLException;
}