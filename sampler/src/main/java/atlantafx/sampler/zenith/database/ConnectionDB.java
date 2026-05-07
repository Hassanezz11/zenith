package atlantafx.sampler.zenith.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionDB {
    private static String URL = "jdbc:sqlserver://localhost:1433;databaseName=db9;Encrypt=True;TrustServerCertificate=True";
    private static String USER = "sa";
    private static String PSWD = "hassan1234@";

    public static Connection getCnx() throws SQLException {
        return DriverManager.getConnection(URL, USER, PSWD);
    }

    public static void closeCnx(Connection cnx) throws SQLException {
        cnx.close();
    }
}
