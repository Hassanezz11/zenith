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
