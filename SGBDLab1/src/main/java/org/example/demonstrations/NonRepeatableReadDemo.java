package org.example.demonstrations;

import java.sql.*;
import java.util.function.Consumer;

public class NonRepeatableReadDemo {

//    T-A citeste, T-B modifica si da commit, T-A citeste iar si vede altceva.

    private String url, user, pass;

    public NonRepeatableReadDemo(String url, String user, String pass) {
        this.url = url; this.user = user; this.pass = pass;
    }

    public void run(Consumer<String> logger) {
        logger.accept("\n=== DEMONSTRATIE 1.B: NON-REPEATABLE READ (Permisiv: READ COMMITTED) ===");

        Thread tA = new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                conn.setAutoCommit(false);
                logger.accept("[T-A]: BEGIN (READ COMMITTED)");

                // Prima citire
                int val1 = getAn(conn, 1);
                logger.accept("[T-A]: Prima citire: " + val1);

                Thread.sleep(3000); // asteptam ca T-B sa faca update si commit

                // A doua citire
                int val2 = getAn(conn, 1);
                logger.accept("[T-A]: A doua citire: " + val2);

                conn.commit();
            } catch (Exception e) { e.printStackTrace(); }
        });

        Thread tB = new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                Thread.sleep(1000); // asteapta ca t-a sa citeasca prima data
                conn.setAutoCommit(false);

                String sql = "UPDATE Carti SET an_aparitie = 1999 WHERE id_carte = 1";
                conn.createStatement().executeUpdate(sql);
                conn.commit();
                logger.accept("[T-B]: Actualizat la 1999 și COMIS");
            } catch (Exception e) { e.printStackTrace(); }
        });

        tA.start(); tB.start();
        try { tA.join(); tB.join(); } catch (Exception e) {}
    }

    private int getAn(Connection conn, int id) throws SQLException {
        String sql = "SELECT an_aparitie FROM Carti WHERE id_carte = " + id;
        ResultSet rs = conn.createStatement().executeQuery(sql);
        return rs.next() ? rs.getInt(1) : 0;
    }
}
