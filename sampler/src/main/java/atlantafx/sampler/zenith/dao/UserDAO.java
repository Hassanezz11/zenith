package atlantafx.sampler.zenith.dao;

import atlantafx.sampler.zenith.Administrateur;
import atlantafx.sampler.zenith.Joueur;
import atlantafx.sampler.zenith.database.ConnectionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
