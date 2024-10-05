package service;

import dao.UserMapper;
import lombok.AllArgsConstructor;
import util.User;

import java.sql.SQLException;

@AllArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public User authenticate(String account, String password) throws SQLException {
        User user = userMapper.authenticate(account, password);
        if (user == null) {
            throw new SQLException("Authentication failed");
        }
        return user;
    }

    public void register(User user) throws SQLException {
        try {
            userMapper.insertUser(user);
        }
        catch (Exception e) {
            throw new SQLException("Failed to register user", e);
        }
    }
}
