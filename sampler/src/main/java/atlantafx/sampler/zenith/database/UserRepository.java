package atlantafx.sampler.zenith.database;

import java.sql.*;

public class UserRepository {

    // SELECT — Get all users
    public ResultSet getAll() throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "SELECT * FROM Users";
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }

    // SELECT — Get user by ID
    public ResultSet getById(int userId) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "SELECT * FROM Users WHERE UserId = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, userId);
        return ps.executeQuery();
    }

    // SELECT — Login
    public ResultSet login(String email, String passwordHash) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "SELECT * FROM Users WHERE Email = ? AND PasswordHash = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, email);
        ps.setString(2, passwordHash);
        return ps.executeQuery();
    }

    // INSERT — Register new user
    public boolean insert(String username, String email, String passwordHash) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "INSERT INTO Users (Username, Email, PasswordHash) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, username);
        ps.setString(2, email);
        ps.setString(3, passwordHash);
        return ps.executeUpdate() > 0;
    }

    // UPDATE — Update profile
    public boolean update(int userId, String username, String email) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "UPDATE Users SET Username = ?, Email = ? WHERE UserId = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, username);
        ps.setString(2, email);
        ps.setInt(3, userId);
        return ps.executeUpdate() > 0;
    }

    // DELETE — Delete user
    public boolean delete(int userId) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "DELETE FROM Users WHERE UserId = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, userId);
        return ps.executeUpdate() > 0;
    }
}