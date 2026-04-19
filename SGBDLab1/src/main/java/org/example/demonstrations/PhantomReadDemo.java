package org.example.demonstrations;

import java.sql.*;
import java.util.function.Consumer;
public class PhantomReadDemo {

    // T-A numara cartile unei edituri, T-B mai adauga una, T-A numara iar si vede o "fantoma".

    private String url, user, pass;

    public PhantomReadDemo(String url, String user, String pass) {
        this.url = url; this.user = user; this.pass = pass;
    }

    public void run(Consumer<String> logger) {
        logger.accept("\n=== DEMONSTRATIE 1.C: PHANTOM READ (Permisiv: REPEATABLE READ) ===");

        Thread tA = new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                conn.setAutoCommit(false);

                logger.accept("[T-A]: Prima numaratoare (Editura 1): " + countCarti(conn, 1));

                Thread.sleep(3000);

                logger.accept("[T-A]: A doua numaratoare (Editura 1): " + countCarti(conn, 1));
                conn.commit();
            } catch (Exception e) { e.printStackTrace(); }
        });

        Thread tB = new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                Thread.sleep(1000);
                conn.setAutoCommit(false);
                String sql = "INSERT INTO Carti (titlu, an_aparitie, isbn, id_editura) VALUES ('Phantom Book', 2024, 'ISBN-X', 1)";
                conn.createStatement().executeUpdate(sql);
                conn.commit();
                logger.accept("[T-B]: Carte noua inserata si COMISA");
            } catch (Exception e) { e.printStackTrace(); }
        });

        tA.start(); tB.start();
        try { tA.join(); tB.join(); } catch (Exception e) {}
    }

    private int countCarti(Connection conn, int idEd) throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM Carti WHERE id_editura = " + idEd);
        return rs.next() ? rs.getInt(1) : 0;
    }
}
