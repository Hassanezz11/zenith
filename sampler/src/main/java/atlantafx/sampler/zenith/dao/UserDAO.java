package atlantafx.sampler.zenith.dao;

import atlantafx.sampler.zenith.Administrateur;
import atlantafx.sampler.zenith.Joueur;
import atlantafx.sampler.zenith.UserActivityRow;
import atlantafx.sampler.zenith.database.ConnectionDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserDAO {

    public static Joueur getByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM Users WHERE Email = ?";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public static Joueur getById(int userId) throws SQLException {
        String sql = "SELECT * FROM Users WHERE UserId = ?";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public static boolean checkPassword(String email, String password) throws SQLException {
        String sql = "SELECT MotDePasse FROM Users WHERE Email = ?";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("MotDePasse").equals(password);
                }
            }
        }
        return false;
    }

    public static void save(String nom, String email, String password) throws SQLException {
        String sql = "INSERT INTO Users (NomComplet, Email, MotDePasse) VALUES (?, ?, ?)";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.executeUpdate();
        }
    }

    public static void updateName(int userId, String nom) throws SQLException {
        String sql = "UPDATE Users SET NomComplet = ? WHERE UserId = ?";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public static List<Joueur> getAllOtherUsers(int currentUserId) throws SQLException {
        String sql = "SELECT * FROM Users WHERE UserId != ?";
        List<Joueur> users = new ArrayList<>();
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, currentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(map(rs));
                }
            }
        }
        return users;
    }

    public static void updateRank(int userId, String rank) throws SQLException {
        String sql = "UPDATE Users SET Rang = ? WHERE UserId = ?";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, rank);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public static void updatePassword(int userId, String password) throws SQLException {
        String sql = "UPDATE Users SET MotDePasse = ? WHERE UserId = ?";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, password);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // ── Admin operations ──────────────────────────────────────────────────────

    public static List<UserActivityRow> getAllUsersWithActivity() throws SQLException {
        String sql = """
            SELECT u.UserId, u.NomComplet, u.Email, u.Rang, u.EstAdmin, u.DateCreation,
                (SELECT COUNT(*) FROM UsersJeux uj WHERE uj.UserId = u.UserId AND uj.EstWishlist = 0) AS OwnedGames,
                (SELECT COUNT(*) FROM Avis a    WHERE a.UserId  = u.UserId)                            AS ReviewCount,
                (SELECT COUNT(*) FROM Messages m WHERE m.ExpediteurId = u.UserId)                     AS MessagesSent
            FROM Users u
            ORDER BY u.DateCreation DESC
            """;
        List<UserActivityRow> rows = new ArrayList<>();
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("DateCreation");
                LocalDateTime joined = ts != null ? ts.toLocalDateTime() : null;
                rows.add(new UserActivityRow(
                    rs.getInt("UserId"),
                    rs.getString("NomComplet"),
                    rs.getString("Email"),
                    rs.getString("Rang"),
                    rs.getBoolean("EstAdmin"),
                    rs.getInt("OwnedGames"),
                    rs.getInt("ReviewCount"),
                    rs.getInt("MessagesSent"),
                    joined
                ));
            }
        }
        return rows;
    }

    public static Set<Integer> getBannedUserIds() throws SQLException {
        Set<Integer> ids = new HashSet<>();
        String sql = "SELECT UserId FROM BannedUsers";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ids.add(rs.getInt("UserId"));
        }
        return ids;
    }

    public static void promoteToAdmin(int requestingAdminId, int targetUserId) throws SQLException {
        // Verify the requester is actually an admin before promoting
        String check = "SELECT EstAdmin FROM Users WHERE UserId = ?";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(check)) {
            ps.setInt(1, requestingAdminId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !rs.getBoolean("EstAdmin")) {
                    throw new SQLException("Unauthorized: only admins can promote users.");
                }
            }
        }
        String sql = "UPDATE Users SET EstAdmin = 1 WHERE UserId = ?";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, targetUserId);
            ps.executeUpdate();
        }
    }

    public static void banUser(int targetId, int adminId) throws SQLException {
        String sql = "INSERT INTO BannedUsers (UserId, BannedByAdminId) VALUES (?, ?)";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, targetId);
            ps.setInt(2, adminId);
            ps.executeUpdate();
        }
    }

    public static void unbanUser(int targetId) throws SQLException {
        String sql = "DELETE FROM BannedUsers WHERE UserId = ?";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, targetId);
            ps.executeUpdate();
        }
    }

    public static boolean isBanned(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM BannedUsers WHERE UserId = ?";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static Joueur map(ResultSet rs) throws SQLException {
        int userId    = rs.getInt("UserId");
        String nom    = rs.getString("NomComplet");
        String mail   = rs.getString("Email");
        String pwd    = rs.getString("MotDePasse");
        String rang   = rs.getString("Rang");
        boolean admin = rs.getBoolean("EstAdmin");
        if (admin) {
            return new Administrateur(userId, nom, mail, pwd, rang, new ArrayList<>(), new ArrayList<>());
        }
        return new Joueur(userId, nom, mail, pwd, rang, new ArrayList<>(), new ArrayList<>());
    }
}
