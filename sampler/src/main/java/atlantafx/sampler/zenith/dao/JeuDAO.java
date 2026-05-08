package atlantafx.sampler.zenith.dao;

import atlantafx.sampler.zenith.Jeu;
import atlantafx.sampler.zenith.database.ConnectionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JeuDAO {

    public static List<Jeu> getAll() throws SQLException {
        List<Jeu> list = new ArrayList<>();
        String sql = "SELECT * FROM Jeux";
        try (Connection cnx = ConnectionDB.getCnx();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public static Jeu getById(int id) throws SQLException {
        String sql = "SELECT * FROM Jeux WHERE JeuId = ?";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public static List<Jeu> getByCategory(String category) throws SQLException {
        List<Jeu> list = new ArrayList<>();
        String sql = "SELECT * FROM Jeux WHERE Categorie = ?";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public static List<Jeu> search(String query) throws SQLException {
        List<Jeu> list = new ArrayList<>();
        String sql = "SELECT * FROM Jeux WHERE Titre LIKE ? OR Categorie LIKE ?";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Ensures a RAWG game exists in the local DB and returns its local JeuId.
     * Matches by title; inserts if not found.
     */
    public static int upsertRawgGame(Jeu jeu) throws SQLException {
        String checkSql = "SELECT JeuId FROM Jeux WHERE Titre = ?";
        try (Connection cnx = ConnectionDB.getCnx()) {
            try (PreparedStatement check = cnx.prepareStatement(checkSql)) {
                check.setString(1, jeu.getTitre());
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
            String insertSql = "INSERT INTO Jeux (Titre, Categorie, Description, Prix, PromoLabel, CouleurAccent)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = cnx.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, jeu.getTitre());
                ps.setString(2, jeu.getCategory());
                ps.setString(3, jeu.getDescription());
                ps.setDouble(4, jeu.getPrix() != null ? jeu.getPrix() : 0.0);
                ps.setString(5, jeu.getPromoLabel());
                ps.setString(6, jeu.getAccentColor());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to upsert RAWG game: " + jeu.getTitre());
    }

    static Jeu map(ResultSet rs) throws SQLException {
        return new Jeu(
            rs.getInt("JeuId"),
            rs.getString("Titre"),
            rs.getString("Categorie"),
            rs.getString("Description"),
            rs.getDouble("Prix"),
            rs.getString("PromoLabel"),
            rs.getString("CouleurAccent"),
            new ArrayList<>()
        );
    }
}
