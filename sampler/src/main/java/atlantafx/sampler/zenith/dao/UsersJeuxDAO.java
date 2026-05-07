package atlantafx.sampler.zenith.dao;

import atlantafx.sampler.zenith.Jeu;
import atlantafx.sampler.zenith.database.ConnectionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsersJeuxDAO {

    public static void addToLibrary(int userId, int jeuId) throws SQLException {
        try (Connection cnx = ConnectionDB.getCnx()) {
            boolean exists;
            try (PreparedStatement check = cnx.prepareStatement(
                "SELECT COUNT(*) FROM UsersJeux WHERE UserId = ? AND JeuId = ?")) {
                check.setInt(1, userId);
                check.setInt(2, jeuId);
                try (ResultSet rs = check.executeQuery()) {
                    rs.next();
                    exists = rs.getInt(1) > 0;
                }
            }
            String sql = exists
                ? "UPDATE UsersJeux SET EstWishlist = 0 WHERE UserId = ? AND JeuId = ?"
                : "INSERT INTO UsersJeux (UserId, JeuId, EstWishlist) VALUES (?, ?, 0)";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, jeuId);
                ps.executeUpdate();
            }
        }
    }

    public static void addToWishlist(int userId, int jeuId) throws SQLException {
        try (Connection cnx = ConnectionDB.getCnx()) {
            boolean exists = false;
            boolean alreadyOwned = false;
            try (PreparedStatement check = cnx.prepareStatement(
                "SELECT EstWishlist FROM UsersJeux WHERE UserId = ? AND JeuId = ?")) {
                check.setInt(1, userId);
                check.setInt(2, jeuId);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        exists = true;
                        alreadyOwned = rs.getInt("EstWishlist") == 0;
                    }
                }
            }
            if (alreadyOwned) return;
            String sql = exists
                ? "UPDATE UsersJeux SET EstWishlist = 1 WHERE UserId = ? AND JeuId = ?"
                : "INSERT INTO UsersJeux (UserId, JeuId, EstWishlist) VALUES (?, ?, 1)";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, jeuId);
                ps.executeUpdate();
            }
        }
    }

    public static boolean isOwned(int userId, int jeuId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM UsersJeux WHERE UserId = ? AND JeuId = ? AND EstWishlist = 0";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, jeuId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public static boolean isWishlisted(int userId, int jeuId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM UsersJeux WHERE UserId = ? AND JeuId = ? AND EstWishlist = 1";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, jeuId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public static int countOwned(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM UsersJeux WHERE UserId = ? AND EstWishlist = 0";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public static List<Jeu> getOwnedGames(int userId) throws SQLException {
        return getByFlag(userId, 0);
    }

    public static List<Jeu> getWishlistGames(int userId) throws SQLException {
        return getByFlag(userId, 1);
    }

    private static List<Jeu> getByFlag(int userId, int flag) throws SQLException {
        List<Jeu> list = new ArrayList<>();
        String sql = "SELECT j.* FROM Jeux j JOIN UsersJeux uj ON j.JeuId = uj.JeuId " +
                     "WHERE uj.UserId = ? AND uj.EstWishlist = ?";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, flag);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(JeuDAO.map(rs));
                }
            }
        }
        return list;
    }
}
