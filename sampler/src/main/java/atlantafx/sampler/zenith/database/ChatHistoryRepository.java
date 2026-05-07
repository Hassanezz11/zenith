package atlantafx.sampler.zenith.database;

import java.sql.*;

public class ChatHistoryRepository {

    // SELECT — Get chat history by user
    public ResultSet getByUser(int userId) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "SELECT * FROM ChatHistory WHERE UserId = ? ORDER BY SentAt DESC";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, userId);
        return ps.executeQuery();
    }

    // SELECT — Get last 10 messages by user
    public ResultSet getLast10(int userId) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "SELECT TOP 10 * FROM ChatHistory WHERE UserId = ? ORDER BY SentAt DESC";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, userId);
        return ps.executeQuery();
    }

    // INSERT — Save chat message + response
    public boolean insert(int userId, String message, String response) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "INSERT INTO ChatHistory (UserId, Message, Response) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, userId);
        ps.setString(2, message);
        ps.setString(3, response);
        return ps.executeUpdate() > 0;
    }

    // DELETE — Delete chat history by user
    public boolean deleteByUser(int userId) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "DELETE FROM ChatHistory WHERE UserId = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, userId);
        return ps.executeUpdate() > 0;
    }
}
