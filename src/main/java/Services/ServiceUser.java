package Services;

import Models.User;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser implements Iservice<User> {

    private final Connection connection;

    public ServiceUser() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(User user) throws SQLDataException {
        String sql = "INSERT INTO `user` (email, roles, password, first_name, last_name) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getRoles());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setString(4, user.getFirstName());
            setNullableLastName(preparedStatement, 5, user.getLastName());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw buildDataException("insert", e);
        }
    }

    @Override
    public void supprimer(User user) throws SQLDataException {
        if (user == null || user.getId() <= 0) {
            throw new SQLDataException("A valid user id is required for delete.");
        }

        String sql = "DELETE FROM `user` WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, user.getId());
            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLDataException("No user found with id " + user.getId());
            }
        } catch (SQLException e) {
            throw buildDataException("delete", e);
        }
    }

    @Override
    public void modifier(User user) throws SQLDataException {
        if (user == null || user.getId() <= 0) {
            throw new SQLDataException("A valid user id is required for update.");
        }

        String sql = "UPDATE `user` SET email = ?, roles = ?, password = ?, first_name = ?, last_name = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getRoles());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setString(4, user.getFirstName());
            setNullableLastName(preparedStatement, 5, user.getLastName());
            preparedStatement.setInt(6, user.getId());
            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLDataException("No user found with id " + user.getId());
            }
        } catch (SQLException e) {
            throw buildDataException("update", e);
        }
    }

    @Override
    public List<User> recuperer() throws SQLDataException {
        String sql = "SELECT id, email, roles, password, first_name, last_name FROM `user` ORDER BY id";
        List<User> users = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setEmail(resultSet.getString("email"));
                user.setRoles(resultSet.getString("roles"));
                user.setPassword(resultSet.getString("password"));
                user.setFirstName(resultSet.getString("first_name"));
                user.setLastName(resultSet.getString("last_name"));
                users.add(user);
            }
        } catch (SQLException e) {
            throw buildDataException("read", e);
        }

        return users;
    }

    private void setNullableLastName(PreparedStatement preparedStatement, int index, String lastName) throws SQLException {
        if (lastName == null || lastName.trim().isEmpty()) {
            preparedStatement.setNull(index, Types.VARCHAR);
            return;
        }
        preparedStatement.setString(index, lastName.trim());
    }

    private SQLDataException buildDataException(String action, SQLException exception) {
        SQLDataException sqlDataException = new SQLDataException("Unable to " + action + " user: " + exception.getMessage());
        sqlDataException.initCause(exception);
        return sqlDataException;
    }
}