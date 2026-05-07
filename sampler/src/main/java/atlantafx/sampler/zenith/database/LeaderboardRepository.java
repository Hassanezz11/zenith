package atlantafx.sampler.zenith.database;

import java.sql.*;

public class LeaderboardRepository {

    // SELECT — Get top 10 scores by game
    public ResultSet getTop10(int gameId) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "SELECT TOP 10 u.Username, l.Score, l.UpdatedAt " +
                "FROM Leaderboard l " +
                "JOIN Users u ON l.UserId = u.UserId " +
                "WHERE l.GameId = ? " +
                "ORDER BY l.Score DESC";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, gameId);
        return ps.executeQuery();
    }

    // SELECT — Get score by user and game
    public ResultSet getByUserAndGame(int userId, int gameId) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "SELECT * FROM Leaderboard WHERE UserId = ? AND GameId = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, userId);
        ps.setInt(2, gameId);
        return ps.executeQuery();
    }

    // INSERT — Add score
    public boolean insert(int userId, int gameId, int score) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "INSERT INTO Leaderboard (UserId, GameId, Score) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, userId);
        ps.setInt(2, gameId);
        ps.setInt(3, score);
        return ps.executeUpdate() > 0;
    }

    // UPDATE — Update score
    public boolean update(int userId, int gameId, int newScore) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "UPDATE Leaderboard SET Score = ?, UpdatedAt = GETDATE() " +
                "WHERE UserId = ? AND GameId = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, newScore);
        ps.setInt(2, userId);
        ps.setInt(3, gameId);
        return ps.executeUpdate() > 0;
    }

    // DELETE — Delete score
    public boolean delete(int userId, int gameId) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "DELETE FROM Leaderboard WHERE UserId = ? AND GameId = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, userId);
        ps.setInt(2, gameId);
        return ps.executeUpdate() > 0;
    }
}
