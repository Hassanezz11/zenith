package atlantafx.sampler.zenith.database;

import java.sql.*;

public class GameRepository {

    // SELECT — Get all games
    public ResultSet getAll() throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "SELECT g.*, c.Name as CategoryName " +
                "FROM Games g " +
                "LEFT JOIN Categories c ON g.CategoryId = c.CategoryId " +
                "WHERE g.IsActive = 1";
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }

    // SELECT — Get game by ID
    public ResultSet getById(int gameId) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "SELECT * FROM Games WHERE GameId = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, gameId);
        return ps.executeQuery();
    }

    // SELECT — Get games by category
    public ResultSet getByCategory(int categoryId) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "SELECT * FROM Games WHERE CategoryId = ? AND IsActive = 1";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, categoryId);
        return ps.executeQuery();
    }

    // SELECT — Search games by title
    public ResultSet search(String keyword) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "SELECT * FROM Games WHERE Title LIKE ? AND IsActive = 1";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, "%" + keyword + "%");
        return ps.executeQuery();
    }

    // INSERT — Add new game
    public boolean insert(String title, String description, double price, int categoryId) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "INSERT INTO Games (Title, Description, Price, CategoryId) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, title);
        ps.setString(2, description);
        ps.setDouble(3, price);
        ps.setInt(4, categoryId);
        return ps.executeUpdate() > 0;
    }

    // UPDATE — Update game
    public boolean update(int gameId, String title, double price) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "UPDATE Games SET Title = ?, Price = ? WHERE GameId = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, title);
        ps.setDouble(2, price);
        ps.setInt(3, gameId);
        return ps.executeUpdate() > 0;
    }

    // DELETE — Soft delete
    public boolean delete(int gameId) throws SQLException {
        Connection conn = ConnectionDB.getCnx();
        String query = "UPDATE Games SET IsActive = 0 WHERE GameId = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, gameId);
        return ps.executeUpdate() > 0;
    }
}