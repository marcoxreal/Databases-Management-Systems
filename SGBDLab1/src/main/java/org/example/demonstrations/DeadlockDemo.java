package org.example.demonstrations;

import java.sql.*;
import java.util.function.Consumer;

public class DeadlockDemo {
//    T-A: blocheaza Cartea 1 -> asteapta -> incearca sa blocheze Cartea 2.

//    T-B: blocheaza Cartea 2 -> asteapta -> incearca sa blocheze Cartea 1.

    private String url, user, pass;

    public DeadlockDemo(String url, String user, String pass) {
        this.url = url; this.user = user; this.pass = pass;
    }

    public void run(Consumer<String> logger) {
        logger.accept("\n=== DEMONSTRATIE 2: DEADLOCK (Conflict de resurse) ===");

        Thread tA = new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                conn.setAutoCommit(false);
                logger.accept("[T-A]: BEGIN. Blochez Cartea ID=1...");

                // T-A blocheaza ID 1
                updateAn(conn, 1, 2010);

                logger.accept("[T-A]: Cartea 1 blocata. Astept 2 secunde...");
                Thread.sleep(2000);

                // T-A vrea ID 2 (care e blocat de T-B)
                logger.accept("[T-A]: Incerc sa blochez Cartea ID=2...");
                updateAn(conn, 2, 2010);

                conn.commit();
                logger.accept("[T-A]: COMMIT (Succes)");
            } catch (SQLException e) {
                System.err.println("[T-A]: EROARE DETECTATA: " + e.getMessage());
                // prindem eroarea de deadlock
            } catch (InterruptedException e) { e.printStackTrace(); }
        });

        Thread tB = new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                conn.setAutoCommit(false);
                logger.accept("[T-B]: BEGIN. Blochez Cartea ID=2...");

                // T-B blocheaza ID 2
                updateAn(conn, 2, 2020);

                logger.accept("[T-B]: Cartea 2 blocata. Astept 2 secunde...");
                Thread.sleep(2000);

                // T-B vrea ID 1 (care e blocat de T-A)
                logger.accept("[T-B]: Incerc sa blochez Cartea ID=1...");
                updateAn(conn, 1, 2020);

                conn.commit();
                logger.accept("[T-B]: COMMIT (Succes)");
            } catch (SQLException e) {
                logger.accept("[T-B]: EROARE DETECTATA: " + e.getMessage());
            } catch (InterruptedException e) { e.printStackTrace(); }
        });

        tA.start(); tB.start();
        try { tA.join(); tB.join(); } catch (Exception e) {}
    }

    private void updateAn(Connection conn, int id, int val) throws SQLException {
        String sql = "UPDATE Carti SET an_aparitie = ? WHERE id_carte = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, val);
            ps.setInt(2, id);
            ps.executeUpdate(); // firul de executie e inghetat
        }
    }
}
