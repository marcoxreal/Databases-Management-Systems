package org.example.transactions;

import java.sql.*;

public class DeadlockDemo {
    private static final String URL = "jdbc:postgresql://localhost:5432/biblioteca";
    private static final String USER = "postgres";
    private static final String PASS = "123skem2";

    public void runDemo() {
        System.out.println("--- DEMONSTRATIE DEADLOCK (Tabel Carti) ---");

        Thread t1 = new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                conn.setAutoCommit(false);

                // 1. blocheaza cartea 1
                System.out.println("[T1]: Update Cartea 1...");
                executeIdUpdate(conn, 1, 2024);

                Thread.sleep(2000); // Așteaptă ca T2 să blocheze Cartea 2

                // 2. incearca sa blocheza cartea 2 (care e deja blocata de T2)
                System.out.println("[T1]: Incerc update pe Cartea 2...");
                executeIdUpdate(conn, 2, 2024);

                conn.commit();
            } catch (SQLException e) {
                System.err.println("[T1] EROARE (Probabil victima Deadlock): " + e.getMessage());
            } catch (InterruptedException e) { e.printStackTrace(); }
        });

        Thread t2 = new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                conn.setAutoCommit(false);

                // 1. Blochează Cartea 2
                System.out.println("[T2]: Update Cartea 2...");
                executeIdUpdate(conn, 2, 2025);

                Thread.sleep(2000); // Așteaptă ca T1 să blocheze Cartea 1

                // 2. Încearcă să blocheze Cartea 1 (care e deja blocată de T1)
                System.out.println("[T2]: Încerc update pe Cartea 1...");
                executeIdUpdate(conn, 1, 2025);

                conn.commit();
            } catch (SQLException e) {
                System.err.println("[T2] EROARE (Probabil victima Deadlock): " + e.getMessage());
            } catch (InterruptedException e) { e.printStackTrace(); }
        });

        t1.start();
        t2.start();
    }

    private void executeIdUpdate(Connection conn, int id, int an) throws SQLException {
        String sql = "UPDATE Carti SET an_aparitie = ? WHERE id_carte = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, an);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }
}