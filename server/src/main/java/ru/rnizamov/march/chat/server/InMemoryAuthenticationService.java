package ru.rnizamov.march.chat.server;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAuthenticationService implements AuthenticationService {
    private class User {
        private String login;
        private String password;
        private String nickname;
        private Role role;

        public User(String login, String password, String nickname, Role role) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
            this.role = role;
        }

    }

    private List<User> users;

    public InMemoryAuthenticationService() {
        this.users = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            this.users.add(new User("login" + i, "pass" + i, "nick" + i, Role.USER));
        }
        this.users.add(new User("admin", "admin", "superAdmin", Role.ADMIN));
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        for (User u : users) {
            if (u.login.equals(login) && u.password.equals(password)) {
                return u.nickname;
            }
        }
        return null;
    }

    @Override
    public boolean register(String login, String password, String nickname, Role role) {
        if (isLoginAlreadyExist(login)) {
            return false;
        }
        if (isNicknameAlreadyExist(nickname)) {
            return false;
        }
        users.add(new User(login, password, nickname, role));
        return true;
    }

    @Override
    public boolean isLoginAlreadyExist(String login) {
        for (User u : users) {
            if (u.login.equals(login)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNicknameAlreadyExist(String nickname) {
        for (User u : users) {
            if (u.nickname.equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAdmin(String nickname) {
        for (User u : users) {
            if (u.nickname.equals(nickname)) {
                if (u.role == Role.ADMIN) {
                    return true;
                }
            }
        }
        return false;
    }
}