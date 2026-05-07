package atlantafx.sampler.zenith.dao;

import atlantafx.sampler.zenith.Review;
import atlantafx.sampler.zenith.database.ConnectionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AvisDAO {

    public static List<Review> getByJeuId(int jeuId) throws SQLException {
        List<Review> list = new ArrayList<>();
        // LEFT JOIN UsersJeux to flag verified purchases (reviewer owns the game)
        String sql = """
            SELECT a.Note, a.Commentaire, u.NomComplet, u.Rang,
                   CASE WHEN uj.UserId IS NOT NULL THEN 1 ELSE 0 END AS IsVerified
            FROM Avis a
            JOIN Users u ON u.UserId = a.UserId
            LEFT JOIN UsersJeux uj
                   ON uj.UserId = a.UserId AND uj.JeuId = a.JeuId AND uj.EstWishlist = 0
            WHERE a.JeuId = ?
            ORDER BY a.DateAvis DESC
            """;
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, jeuId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Review(
                        rs.getString("NomComplet"),
                        rs.getString("Rang"),
                        rs.getInt("Note"),
                        rs.getString("Commentaire"),
                        rs.getInt("IsVerified") == 1
                    ));
                }
            }
        }
        return list;
    }

    public static void save(int jeuId, int userId, int note, String commentaire) throws SQLException {
        try (Connection cnx = ConnectionDB.getCnx()) {
            boolean exists;
            try (PreparedStatement check = cnx.prepareStatement(
                "SELECT COUNT(*) FROM Avis WHERE JeuId = ? AND UserId = ?")) {
                check.setInt(1, jeuId);
                check.setInt(2, userId);
                try (ResultSet rs = check.executeQuery()) {
                    rs.next();
                    exists = rs.getInt(1) > 0;
                }
            }
            String sql = exists
                ? "UPDATE Avis SET Note = ?, Commentaire = ? WHERE JeuId = ? AND UserId = ?"
                : "INSERT INTO Avis (Note, Commentaire, JeuId, UserId) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, note);
                ps.setString(2, commentaire);
                ps.setInt(3, jeuId);
                ps.setInt(4, userId);
                ps.executeUpdate();
            }
        }
    }
}
