package atlantafx.sampler.zenith.database;

import java.sql.*;

public class ReviewRepository {

    // SELECT — Get reviews by game
    public ResultSet getByGame(int gameId) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "SELECT r.*, u.Username FROM Reviews r " +
                "JOIN Users u ON r.UserId = u.UserId " +
                "WHERE r.GameId = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, gameId);
        return ps.executeQuery();
    }

    // SELECT — Get reviews by user
    public ResultSet getByUser(int userId) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "SELECT * FROM Reviews WHERE UserId = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, userId);
        return ps.executeQuery();
    }

    // SELECT — Get average rating for a game
    public ResultSet getAverageRating(int gameId) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "SELECT AVG(CAST(Rating AS FLOAT)) as AvgRating FROM Reviews WHERE GameId = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, gameId);
        return ps.executeQuery();
    }

    // INSERT — Add review
    public boolean insert(int userId, int gameId, int rating, String comment) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "INSERT INTO Reviews (UserId, GameId, Rating, Comment) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, userId);
        ps.setInt(2, gameId);
        ps.setInt(3, rating);
        ps.setString(4, comment);
        return ps.executeUpdate() > 0;
    }

    // UPDATE — Update review
    public boolean update(int reviewId, int rating, String comment) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "UPDATE Reviews SET Rating = ?, Comment = ? WHERE ReviewId = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, rating);
        ps.setString(2, comment);
        ps.setInt(3, reviewId);
        return ps.executeUpdate() > 0;
    }

    // DELETE — Delete review
    public boolean delete(int reviewId) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "DELETE FROM Reviews WHERE ReviewId = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, reviewId);
        return ps.executeUpdate() > 0;
    }
}
