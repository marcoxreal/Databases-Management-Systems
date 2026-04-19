package org.example.demonstrations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BatchPerformanceDemo {
    private String url, user, pass;
    private static final int NR_CARTI = 5000;
    private static final int NR_RULARI = 3;

    public BatchPerformanceDemo(String url, String user, String pass) {
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    public void run(Consumer<String> logger) {
        logger.accept("=== TEST PERFORMANTA: INSERARE 5000 CARTI (Postgres) ===");

        try {
            long t1 = runExperiment(1, logger); // Auto-commit
            long t2 = runExperiment(2, logger); // Commit la 100
            long t3 = runExperiment(3, logger); // Batch Processing

            logger.accept("\nREZULTATE FINALE (Media celor " + NR_RULARI + " rulari):");
            logger.accept("--------------------------------------------------");
            logger.accept(String.format("| %-25s | %-15s |", "Metoda", "Timp Mediu (ms)"));
            logger.accept("--------------------------------------------------");
            logger.accept(String.format("| %-25s | %-15d |", "1. Auto-commit", t1));
            logger.accept(String.format("| %-25s | %-15d |", "2. Commit la 100", t2));
            logger.accept(String.format("| %-25s | %-15d |", "3. Batch (Transaction)", t3));
            logger.accept("--------------------------------------------------");
            logger.accept("\nExperiment finalizat cu succes.");

        } catch (Exception e) {
            logger.accept("EROARE in timpul experimentului: " + e.getMessage());
        }
    }

    private long runExperiment(int abordare, Consumer<String> logger) throws SQLException {
        List<Long> timpi = new ArrayList<>();
        String numeAbordare = switch (abordare) {
            case 1 -> "Auto-commit";
            case 2 -> "Commit la 100";
            default -> "Batch Processing";
        };

        for (int i = 0; i < NR_RULARI; i++) {
            clearCartiTest();

            long start = System.currentTimeMillis();
            if (abordare == 1) abordarea1();
            else if (abordare == 2) abordarea2();
            else abordarea3();
            long end = System.currentTimeMillis();

            long durata = end - start;
            timpi.add(durata);
            logger.accept("[" + numeAbordare + "] Rularea " + (i + 1) + ": " + durata + " ms");
        }
        return timpi.stream().mapToLong(Long::longValue).sum() / NR_RULARI;
    }

    private void abordarea1() throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sql = "INSERT INTO Carti (titlu, an_aparitie, isbn, id_editura) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < NR_CARTI; i++) {
                    stmt.setString(1, "Carte Test #" + i);
                    stmt.setInt(2, 2024);
                    stmt.setString(3, "B1-" + i);
                    stmt.setInt(4, 1);
                    stmt.executeUpdate();
                }
            }
        }
    }

    private void abordarea2() throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            conn.setAutoCommit(false);
            String sql = "INSERT INTO Carti (titlu, an_aparitie, isbn, id_editura) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 1; i <= NR_CARTI; i++) {
                    stmt.setString(1, "Carte Test #" + i);
                    stmt.setInt(2, 2024);
                    stmt.setString(3, "B2-" + i);
                    stmt.setInt(4, 1);
                    stmt.executeUpdate();
                    if (i % 100 == 0) conn.commit();
                }
                conn.commit();
            } catch (SQLException e) { conn.rollback(); throw e; }
        }
    }

    private void abordarea3() throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            conn.setAutoCommit(false);
            String sql = "INSERT INTO Carti (titlu, an_aparitie, isbn, id_editura) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 1; i <= NR_CARTI; i++) {
                    stmt.setString(1, "Carte Test #" + i);
                    stmt.setInt(2, 2024);
                    stmt.setString(3, "B3-" + i);
                    stmt.setInt(4, 1);
                    stmt.addBatch();
                    if (i % 50 == 0) stmt.executeBatch();
                }
                stmt.executeBatch();
                conn.commit();
            } catch (SQLException e) { conn.rollback(); throw e; }
        }
    }

    private void clearCartiTest() throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM Carti WHERE titlu LIKE 'Carte Test %'");
                st.execute("SELECT setval(pg_get_serial_sequence('carti', 'id_carte'), COALESCE(MAX(id_carte), 0) + 1, false) FROM Carti");
            }
        }
    }
}