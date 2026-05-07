package atlantafx.sampler.zenith.dao;

import atlantafx.sampler.zenith.Formation;
import atlantafx.sampler.zenith.database.ConnectionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FormationDAO {

    public static List<Formation> getByUserId(int userId) throws SQLException {
        List<Formation> list = new ArrayList<>();
        String sql = "SELECT * FROM Formations WHERE UserId = ? ORDER BY Annee DESC";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Formation(
                        rs.getInt("FormationId"),
                        rs.getString("Titre"),
                        rs.getString("Etablissement"),
                        rs.getString("Annee"),
                        rs.getString("Description")
                    ));
                }
            }
        }
        return list;
    }

    public static void save(int userId, String titre, String etablissement, String annee, String description) throws SQLException {
        String sql = "INSERT INTO Formations (UserId, Titre, Etablissement, Annee, Description) VALUES (?, ?, ?, ?, ?)";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, titre);
            ps.setString(3, etablissement);
            ps.setString(4, annee);
            ps.setString(5, description);
            ps.executeUpdate();
        }
    }

    public static void delete(int formationId) throws SQLException {
        String sql = "DELETE FROM Formations WHERE FormationId = ?";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, formationId);
            ps.executeUpdate();
        }
    }
}
