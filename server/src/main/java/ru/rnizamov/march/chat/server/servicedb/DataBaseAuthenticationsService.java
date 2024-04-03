package ru.rnizamov.march.chat.server.servicedb;
import ru.rnizamov.march.chat.server.AuthenticationService;
import ru.rnizamov.march.chat.server.Role;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBaseAuthenticationsService implements AuthenticationService {
    private static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/otus_chat";
    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement preparedStatement;

    static {
        try {
            connection = getConnection();
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, "root", "root");
    }

    public static void addNickName(String nick) throws SQLException {
        preparedStatement = connection.prepareStatement("INSERT INTO users (nickname) VALUES (?);");
        preparedStatement.setString(1, nick);
        preparedStatement.executeUpdate();
    }

    public static void deleteNickName(String nick) throws SQLException {
        preparedStatement = connection.prepareStatement("DELETE FROM users WHERE nickname = (?)");
        preparedStatement.setString(1, nick);
        preparedStatement.executeUpdate();
    }

    public static void deleteNickNameById(int id) throws SQLException {
        preparedStatement = connection.prepareStatement("DELETE FROM users WHERE id = (?)");
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
    }

    public static String getNickNameById(int id) throws SQLException {
        preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE id = (?)");
        preparedStatement.setInt(1, id);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString("nickname");
        }
        return null;
    }

    public static Integer getIdByNickName(String nick) throws SQLException {
        preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE nickname = (?)");
        preparedStatement.setString(1, nick);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt("id");
        }
        return null;
    }

    public static void updateNickNameById(int id, String nick) throws SQLException {
        preparedStatement = connection.prepareStatement("UPDATE users SET nickname = (?) WHERE id = (?)");
        preparedStatement.setString(1, nick);
        preparedStatement.setInt(2, id);
        preparedStatement.executeUpdate();
    }

    public static boolean isNickNameExist(String nickname) throws SQLException {
        preparedStatement = connection.prepareStatement("select nickname FROM users");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            if (resultSet.getString("nickname").equals(nickname)) return true;
        }
        return false;
    }

    public static void addRole(String role) throws SQLException {
        preparedStatement = connection.prepareStatement("INSERT INTO role (name) VALUES (?);");
        preparedStatement.setString(1, role);
        preparedStatement.executeUpdate();
    }

    public static void deleteRole(String role) throws SQLException {
        preparedStatement = connection.prepareStatement("DELETE FROM role WHERE name=(?)");
        preparedStatement.setString(1, role);
        preparedStatement.executeUpdate();
    }

    public static void updateRoleById(int id, String name) throws SQLException {
        preparedStatement = connection.prepareStatement("UPDATE role SET name = (?) WHERE id = (?)");
        preparedStatement.setString(1, name);
        preparedStatement.setInt(2, id);
        preparedStatement.executeUpdate();
    }

    public static Integer getRoleId(String name) throws SQLException {
        preparedStatement = connection.prepareStatement("SELECT id FROM role WHERE name=(?)");
        preparedStatement.setString(1, name);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt("id");
        }
        return null;
    }

    public static void addLogAndPassToUser(int userId, String login, String password) throws SQLException {
        preparedStatement = connection.prepareStatement("INSERT INTO auth (user_id, login, password) VALUES (?, ?, ?);");
        preparedStatement.setInt(1, userId);
        preparedStatement.setString(2, login);
        preparedStatement.setString(3, password);
        preparedStatement.executeUpdate();
    }

    public static void addLogAndPassToUser(String nickName, String login, String password) throws SQLException {
        addLogAndPassToUser(getIdByNickName(nickName), login, password);
    }

    public static void deleteLogPassToUser(int userId) throws SQLException {
        preparedStatement = connection.prepareStatement("DELETE FROM auth WHERE user_id=(?)");
        preparedStatement.setInt(1, userId);
        preparedStatement.executeUpdate();
    }

    public static boolean isLoginExist(String login) throws SQLException {
        preparedStatement = connection.prepareStatement("select login FROM auth");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            if (resultSet.getString("login").equals(login)) return true;
        }
        return false;
    }

    public static void addRoleToUser(String nickName, Role role) throws SQLException {
        int userId = getIdByNickName(nickName);
        int roleId = getRoleId(role.name());

        preparedStatement = connection.prepareStatement("INSERT INTO user_to_role (role_id, user_id) VALUES (?, ?);");
        preparedStatement.setInt(1, roleId);
        preparedStatement.setInt(2, userId);
        preparedStatement.executeUpdate();
    }

    public static List<Role> getRolesByNickName(String nickName) throws SQLException {
        List<Role> roles = new ArrayList<>();
        preparedStatement = connection.prepareStatement("SELECT r.name AS name FROM user_to_role ur LEFT JOIN " +
                "role r ON r.id=ur.role_id LEFT JOIN users u ON u.id=ur.user_id WHERE u.nickname = (?)");
        preparedStatement.setString(1, nickName);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            roles.add(Role.valueOf(resultSet.getString("name")));
        }
        return roles;
    }

    public static boolean userHasRoleByNickName(String nickName, Role role) throws SQLException {
        return getRolesByNickName(nickName).contains(role);
    }

    public static void deleteAllRoleUser(String nickName) throws SQLException {
        int id = getIdByNickName(nickName);
        preparedStatement = connection.prepareStatement("DELETE FROM user_to_role WHERE user_id = (?)");
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
    }

    public static void deleteRoleToUser(String nickNane, Role role) throws SQLException {
        int roleId = getRoleId(role.name());
        int userId = getIdByNickName(nickNane);

        preparedStatement = connection.prepareStatement("DELETE FROM user_to_role WHERE user_id = (?) AND role_id = (?)");
        preparedStatement.setInt(1, userId);
        preparedStatement.setInt(2, roleId);
        preparedStatement.executeUpdate();
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) throws SQLException {
        preparedStatement = connection.prepareStatement("SELECT nickname FROM users u LEFT JOIN " +
                "auth a ON u.id=a.user_id WHERE login=(?) AND password=(?)");
        preparedStatement.setString(1, login);
        preparedStatement.setString(2, password);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString("nickname");
        }
        return null;
    }

    @Override
    public boolean register(String login, String password, String nickname, Role role) throws SQLException {
        if (isLoginAlreadyExist(login)) {
            return false;
        }
        if (isNicknameAlreadyExist(nickname)) {
            return false;
        }
        addNickName(nickname);
        addLogAndPassToUser(nickname, login, password);
        addRoleToUser(nickname, role);
        return true;
    }

    public static void deleteUser(String nickname) throws SQLException {
        int id = getIdByNickName(nickname);
        deleteLogPassToUser(id);
        deleteAllRoleUser(nickname);
        deleteNickName(nickname);
    }

    @Override
    public boolean isLoginAlreadyExist(String login) throws SQLException {
        return isLoginExist(login);
    }

    @Override
    public boolean isNicknameAlreadyExist(String nickname) throws SQLException {
        return isNickNameExist(nickname);
    }

    @Override
    public boolean isAdmin(String nickname) throws SQLException {
        return userHasRoleByNickName(nickname, Role.ADMIN);
    }

    public static void closeConnection() {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}