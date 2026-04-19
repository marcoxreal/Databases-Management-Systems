package org.example.demonstrations;

import java.sql.*;
import java.util.function.Consumer;

public class LostUpdateDemo {
    private String url, user, pass;

    public LostUpdateDemo(String url, String user, String pass) {
        this.url = url; this.user = user; this.pass = pass;
    }

    public void run(Consumer<String> logger) {
        logger.accept("\n=== DEMONSTRATIE 1.D: LOST UPDATE (Permisiv: READ COMMITTED) ===");

        // resetam anul unei carti la 2000 pentru test (ID 1)
        prepareData(1, 2000, logger);

        Thread tA = new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                conn.setAutoCommit(false);
                logger.accept("[T-A]: BEGIN (READ COMMITTED)");

                // citeste valoarea
                int anInitial = getAn(conn, 1);
                logger.accept("[T-A]: Citeste an initial = " + anInitial);

                // calcul local (vrea sa faca 2001)
                int noulAn = anInitial + 1;
                logger.accept("[T-A]: Calculeaza local noul an = " + noulAn);

                //  asteapta ca T-B sa faca acelasi lucru
                logger.accept("[T-A]: Asteapta (sleep 3s)...");
                Thread.sleep(3000);

                // update
                updateAn(conn, 1, noulAn);
                conn.commit();
                logger.accept("[T-A]: COMMIT efectuat (an setat la " + noulAn + ")");
            } catch (Exception e) {
                logger.accept("[T-A] EROARE: " + e.getMessage());
            }
        });

        Thread tB = new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                Thread.sleep(1000); // Așteaptă ca T-A să citească valoarea inițială
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                conn.setAutoCommit(false);
                logger.accept("[T-B]: BEGIN (READ COMMITTED)");

                // citeste aceeasi val (tot 2000)
                int anInitial = getAn(conn, 1);
                logger.accept("[T-B]: Citește an initial = " + anInitial);

                // calcul local (vrea sa faca 2005)
                int noulAn = anInitial + 5;
                logger.accept("[T-B]: Calculează local noul an = " + noulAn);

                // update (T-B termina inaintea lui T-A)
                updateAn(conn, 1, noulAn);
                conn.commit();
                logger.accept("[T-B]: COMMIT efectuat (an setat la " + noulAn + ")");
            } catch (Exception e) {
                logger.accept("[T-B] EROARE: " + e.getMessage());
            }
        });

        tA.start(); tB.start();
        try {
            tA.join();
            tB.join();
        } catch (Exception e) {
            logger.accept("Eroare Threading: " + e.getMessage());
        }

        // verificarea rezultatului final
        checkFinalResult(1, logger);
    }

    private void updateAn(Connection conn, int id, int val) throws SQLException {
        String sql = "UPDATE Carti SET an_aparitie = ? WHERE id_carte = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, val);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private int getAn(Connection conn, int id) throws SQLException {
        String sql = "SELECT an_aparitie FROM Carti WHERE id_carte = " + id;
        try (ResultSet rs = conn.createStatement().executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void prepareData(int id, int an, Consumer<String> logger) {
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            updateAn(conn, id, an);
            logger.accept("[SETUP]: Cartea ID " + id + " a fost resetata la anul " + an);
        } catch (SQLException e) {
            logger.accept("[SETUP EROARE]: " + e.getMessage());
        }
    }

    private void checkFinalResult(int id, Consumer<String> logger) {
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            int anFinal = getAn(conn, id);
            logger.accept("\n[REZULTAT FINAL]: Anul în DB este: " + anFinal);
            if (anFinal == 2001) {
                logger.accept("[ANALIZĂ]: Update-ul lui T-B (2005) s-a PIERDUT! T-A a suprascris datele deoarece nu a știut de modificarea lui T-B.");
            } else {
                logger.accept("[ANALIZĂ]: Lost Update nu s-a produs sau a fost gestionat.");
            }
        } catch (SQLException e) {
            logger.accept("[CHECK EROARE]: " + e.getMessage());
        }
    }
}