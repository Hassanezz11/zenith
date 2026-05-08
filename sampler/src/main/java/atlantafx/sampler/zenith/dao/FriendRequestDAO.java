package atlantafx.sampler.zenith.dao;

import atlantafx.sampler.zenith.Administrateur;
import atlantafx.sampler.zenith.Joueur;
import atlantafx.sampler.zenith.database.ConnectionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendRequestDAO {

    public static void sendRequest(int senderId, int receiverId) throws SQLException {
        try (Connection cnx = ConnectionDB.getCnx()) {
            // Check if already friends or a PENDING request exists in either direction
            try (PreparedStatement check = cnx.prepareStatement(
                "SELECT Status FROM Amis WHERE (UserId1 = ? AND UserId2 = ?) OR (UserId1 = ? AND UserId2 = ?)")) {
                check.setInt(1, senderId); check.setInt(2, receiverId);
                check.setInt(3, receiverId); check.setInt(4, senderId);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        String status = rs.getString("Status");
                        if ("ACCEPTED".equals(status) || "PENDING".equals(status)) return;
                        // REJECTED — delete the old row so we can re-send
                        try (PreparedStatement del = cnx.prepareStatement(
                            "DELETE FROM Amis WHERE (UserId1 = ? AND UserId2 = ?) OR (UserId1 = ? AND UserId2 = ?)")) {
                            del.setInt(1, senderId); del.setInt(2, receiverId);
                            del.setInt(3, receiverId); del.setInt(4, senderId);
                            del.executeUpdate();
                        }
                    }
                }
            }
            try (PreparedStatement ps = cnx.prepareStatement(
                "INSERT INTO Amis (UserId1, UserId2, Status) VALUES (?, ?, 'PENDING')")) {
                ps.setInt(1, senderId);
                ps.setInt(2, receiverId);
                ps.executeUpdate();
            }
        }
    }

    public static void acceptRequest(int senderId, int receiverId) throws SQLException {
        String sql = "UPDATE Amis SET Status = 'ACCEPTED' WHERE UserId1 = ? AND UserId2 = ?";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.executeUpdate();
        }
    }

    public static void rejectRequest(int senderId, int receiverId) throws SQLException {
        String sql = "UPDATE Amis SET Status = 'REJECTED' WHERE UserId1 = ? AND UserId2 = ?";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.executeUpdate();
        }
    }

    public static List<Joueur> getFriends(int userId) throws SQLException {
        List<Joueur> list = new ArrayList<>();
        String sql = """
            SELECT u.*
            FROM Users u
            JOIN Amis a ON (a.UserId1 = ? AND a.UserId2 = u.UserId)
                        OR (a.UserId2 = ? AND a.UserId1 = u.UserId)
            WHERE a.Status = 'ACCEPTED'
            """;
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapUser(rs));
            }
        }
        return list;
    }

    public static int getPendingCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Amis WHERE UserId2 = ? AND Status = 'PENDING'";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public static List<Joueur> getPendingRequests(int userId) throws SQLException {
        List<Joueur> list = new ArrayList<>();
        String sql = """
            SELECT u.*
            FROM Users u
            JOIN Amis a ON a.UserId1 = u.UserId
            WHERE a.UserId2 = ? AND a.Status = 'PENDING'
            """;
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapUser(rs));
            }
        }
        return list;
    }

    public static List<Joueur> searchUsers(int currentUserId, String query) throws SQLException {
        List<Joueur> list = new ArrayList<>();
        String q = "%" + query + "%";
        String sql = "SELECT * FROM Users WHERE UserId != ? AND (NomComplet LIKE ? OR Email LIKE ?)";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, currentUserId);
            ps.setString(2, q);
            ps.setString(3, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapUser(rs));
            }
        }
        return list;
    }

    public static boolean areFriends(int userId1, int userId2) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Amis WHERE " +
            "((UserId1 = ? AND UserId2 = ?) OR (UserId1 = ? AND UserId2 = ?)) AND Status = 'ACCEPTED'";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId1); ps.setInt(2, userId2);
            ps.setInt(3, userId2); ps.setInt(4, userId1);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public static boolean hasPendingRequest(int senderId, int receiverId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Amis WHERE UserId1 = ? AND UserId2 = ? AND Status = 'PENDING'";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static Joueur mapUser(ResultSet rs) throws SQLException {
        int userId    = rs.getInt("UserId");
        String nom    = rs.getString("NomComplet");
        String email  = rs.getString("Email");
        String pwd    = rs.getString("MotDePasse");
        String rang   = rs.getString("Rang");
        boolean admin = rs.getBoolean("EstAdmin");
        if (admin) {
            return new Administrateur(userId, nom, email, pwd, rang, new ArrayList<>(), new ArrayList<>());
        }
        return new Joueur(userId, nom, email, pwd, rang, new ArrayList<>(), new ArrayList<>());
    }
}
