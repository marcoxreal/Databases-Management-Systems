package org.example.demonstrations;

import java.sql.*;
import java.util.function.Consumer;

public class DirtyReadDemo {

//    Tranzacția A: Modifica anul unei carti, asteapta, apoi da Rollback.

//    Tranzactia B: citeste in timp ce A asteapta.

    // URL pentru Derby in memorie
    private static final String DERBY_URL = "jdbc:derby:memory:dirtyReadDB;create=true";

    public void run(Consumer<String> logger) {
        try (Connection setupConn = DriverManager.getConnection(DERBY_URL)) {
            setupTable(setupConn);

            logger.accept("=== DEMONSTRATIE 1.A: DIRTY READ (Permisiv: READ UNCOMMITTED) ===");

            Thread tA = new Thread(() -> {
                try (Connection conn = DriverManager.getConnection(DERBY_URL)) {
                    conn.setAutoCommit(false);
                    logger.accept("[T-A]: BEGIN TRANSACTION");

                    String sql = "UPDATE Carti SET an_aparitie = 2026 WHERE id_carte = 1";
                    try (Statement st = conn.createStatement()) {
                        st.executeUpdate(sql);
                    }
                    logger.accept("[T-A]: UPDATE Carti SET an_aparitie = 2026 (ne-comis)");

                    Thread.sleep(2000); // asteapta ca B sa citeasca

                    conn.rollback();
                    logger.accept("[T-A]: ROLLBACK efectuat!");
                } catch (Exception e) { e.printStackTrace(); }
            });

            Thread tB = new Thread(() -> {
                try (Connection conn = DriverManager.getConnection(DERBY_URL)) {
                    // setam nivelul permisiv
                    conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                    conn.setAutoCommit(false);
                    logger.accept("[T-B]: BEGIN TRANSACTION (READ UNCOMMITTED)");

                    Thread.sleep(1000); // ne asiguram ca A a făcut update-ul

                    try (Statement st = conn.createStatement();
                         ResultSet rs = st.executeQuery("SELECT an_aparitie FROM Carti WHERE id_carte = 1")) {
                        if (rs.next()) {
                            logger.accept("[T-B]: Valoare citita: " + rs.getInt(1));
                        }
                    }
                    conn.commit();
                    logger.accept("[T-B]: COMMIT");
                } catch (Exception e) { e.printStackTrace(); }
            });

            tA.start(); tB.start();
            tA.join(); tB.join();

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupTable(Connection conn) throws SQLException {
        try (Statement s = conn.createStatement()) {
            s.execute("CREATE TABLE Carti (id_carte INT PRIMARY KEY, titlu VARCHAR(50), an_aparitie INT)");
            s.execute("INSERT INTO Carti VALUES (1, 'Tranzactii Moderne', 2020)");
        }
    }
}