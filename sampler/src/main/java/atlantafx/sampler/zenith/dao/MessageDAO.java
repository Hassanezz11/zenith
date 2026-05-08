package atlantafx.sampler.zenith.dao;

import atlantafx.sampler.zenith.database.ConnectionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public static List<String[]> getConversation(int userId1, int userId2) throws SQLException {
        List<String[]> messages = new ArrayList<>();
        String sql = """
            SELECT m.Contenu, m.DateEnvoi, u.NomComplet
            FROM Messages m
            JOIN Users u ON u.UserId = m.ExpediteurId
            WHERE (ExpediteurId = ? AND DestinataireId = ?)
               OR (ExpediteurId = ? AND DestinataireId = ?)
            ORDER BY m.DateEnvoi ASC
            """;
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId1); ps.setInt(2, userId2);
            ps.setInt(3, userId2); ps.setInt(4, userId1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(new String[]{
                        rs.getString("NomComplet"),
                        rs.getString("Contenu"),
                        rs.getTimestamp("DateEnvoi").toString()
                    });
                }
            }
        }
        return messages;
    }

    public static int getUnreadCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Messages WHERE DestinataireId = ? AND EstLu = 0";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public static String[] getLastMessage(int userId1, int userId2) throws SQLException {
        String sql = """
            SELECT TOP 1 u.NomComplet, m.Contenu, m.DateEnvoi
            FROM Messages m
            JOIN Users u ON u.UserId = m.ExpediteurId
            WHERE (ExpediteurId = ? AND DestinataireId = ?)
               OR (ExpediteurId = ? AND DestinataireId = ?)
            ORDER BY m.DateEnvoi DESC
            """;
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId1); ps.setInt(2, userId2);
            ps.setInt(3, userId2); ps.setInt(4, userId1);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        rs.getString("NomComplet"),
                        rs.getString("Contenu"),
                        rs.getTimestamp("DateEnvoi").toString()
                    };
                }
            }
        }
        return null;
    }

    public static int getUnreadFromSender(int recipientId, int senderId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Messages WHERE DestinataireId = ? AND ExpediteurId = ? AND EstLu = 0";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, recipientId);
            ps.setInt(2, senderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public static int getMessageCount(int userId1, int userId2) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Messages WHERE " +
            "(ExpediteurId = ? AND DestinataireId = ?) OR (ExpediteurId = ? AND DestinataireId = ?)";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId1); ps.setInt(2, userId2);
            ps.setInt(3, userId2); ps.setInt(4, userId1);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public static void markAsRead(int recipientId, int senderId) throws SQLException {
        String sql = "UPDATE Messages SET EstLu = 1 WHERE DestinataireId = ? AND ExpediteurId = ?";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, recipientId);
            ps.setInt(2, senderId);
            ps.executeUpdate();
        }
    }

    public static void save(int expediteurId, int destinataireId, String contenu) throws SQLException {
        String sql = "INSERT INTO Messages (ExpediteurId, DestinataireId, Contenu) VALUES (?, ?, ?)";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, expediteurId);
            ps.setInt(2, destinataireId);
            ps.setString(3, contenu);
            ps.executeUpdate();
        }
    }
}
