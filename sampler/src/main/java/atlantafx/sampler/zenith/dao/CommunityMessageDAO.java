package atlantafx.sampler.zenith.dao;

import atlantafx.sampler.zenith.database.ConnectionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommunityMessageDAO {

    public static List<String[]> getAll() throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql = """
            SELECT cm.Contenu, cm.DateEnvoi, u.NomComplet
            FROM CommunityMessages cm
            JOIN Users u ON u.UserId = cm.UserId
            ORDER BY cm.DateEnvoi ASC
            """;
        try (Connection cnx = ConnectionDB.getCnx();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("NomComplet"),
                    rs.getString("Contenu"),
                    rs.getTimestamp("DateEnvoi").toString()
                });
            }
        }
        return list;
    }

    public static void save(int userId, String contenu) throws SQLException {
        String sql = "INSERT INTO CommunityMessages (UserId, Contenu) VALUES (?, ?)";
        try (Connection cnx = ConnectionDB.getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, contenu);
            ps.executeUpdate();
        }
    }
}
