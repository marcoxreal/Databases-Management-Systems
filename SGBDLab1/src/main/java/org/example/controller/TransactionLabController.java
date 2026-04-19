package org.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.example.demonstrations.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class TransactionLabController {
    @FXML
    private TextArea logArea;

    private final String url = "jdbc:postgresql://localhost:5432/biblioteca";
    private final String user = "postgres";
    private final String pass = "123skem2";

    private void log(String message) {
        Platform.runLater(() -> logArea.appendText(message + "\n"));
    }

    @FXML
    private void handleDirtyRead() {
        logArea.clear();
        log("--- Pornire Demo Dirty Read (Apache Derby) ---");
        // Transmiterea unei interfete de logare catre demo
        new DirtyReadDemo().run(this::log);
    }

    @FXML
    private void handleNonRepeatableRead() {
        logArea.clear();
        log("--- Pornire Demo Non-Repeatable Read (Postgres) ---");
        new NonRepeatableReadDemo(url, user, pass).run(this::log);
    }

    @FXML
    private void handlePhantomRead() {
        logArea.clear();
        log("--- Pornire Demo Phantom Read (Postgres) ---");
        new PhantomReadDemo(url, user, pass).run(this::log);
    }

    @FXML
    private void handleLostUpdate() {
        logArea.clear();
        log("--- Pornire Demo Lost Update (Postgres) ---");
        new LostUpdateDemo(url, user, pass).run(this::log);
    }

    @FXML
    private void handleDeadlock() {
        logArea.clear();
        log("--- Pornire Demo Deadlock (Postgres) ---");
        new DeadlockDemo(url, user, pass).run(this::log);
    }

    @FXML
    private void handleBatchPerformance() {
        logArea.clear();
        log("--- Pornire Test Performanță Batch ---");
        new BatchPerformanceDemo(url, user, pass).run(this::log);
    }

    @FXML
    private void handleResetDatabase() {
        logArea.clear();
        log("[SYSTEM]: Se inițiază curățarea bazei de date...");

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            Statement st = conn.createStatement();

            st.executeUpdate("TRUNCATE TABLE Carti, Edituri, Autori RESTART IDENTITY CASCADE");
            st.executeUpdate("INSERT INTO Edituri (id_editura, nume) VALUES (1, 'Editura Tehnica')");

            st.executeUpdate("INSERT INTO Carti (id_carte, titlu, an_aparitie, isbn, id_editura) " +
                    "VALUES (1, 'Manual SGBD', 2000, 'ISBN-001', 1)");
            st.executeUpdate("INSERT INTO Carti (id_carte, titlu, an_aparitie, isbn, id_editura) " +
                    "VALUES (2, 'Tranzactii SQL', 2000, 'ISBN-002', 1)");

            log("[SUCCESS]: Baza de date a fost resetată!");
            log("-> Tabelul 'Carti' conține acum ID 1 și ID 2 (An: 2000).");
            log("-> Tabelul 'Edituri' conține ID 1.");

        } catch (SQLException e) {
            log("[EROARE RESET]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetBazaDeDate() {
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            Statement st = conn.createStatement();

            st.executeUpdate("DELETE FROM Carti");
            st.executeUpdate("DELETE FROM Autori_Carti");
            st.executeUpdate("DELETE FROM Autori");
            st.executeUpdate("DELETE FROM Edituri");

            st.executeUpdate("ALTER SEQUENCE edituri_id_editura_seq RESTART WITH 1");
            st.executeUpdate("ALTER SEQUENCE carti_id_carte_seq RESTART WITH 1");

            st.executeUpdate("INSERT INTO Edituri (id_editura, nume) VALUES (1, 'Editura Test')");
            st.executeUpdate("INSERT INTO Carti (id_carte, titlu, an_aparitie, isbn, id_editura) " +
                    "VALUES (1, 'SGBD Vol 1', 2000, 'ISBN-111', 1)");
            st.executeUpdate("INSERT INTO Carti (id_carte, titlu, an_aparitie, isbn, id_editura) " +
                    "VALUES (2, 'SGBD Vol 2', 2000, 'ISBN-222', 1)");

            log("[RESET]: Baza de date a fost golită și reinițializată cu ID 1 și 2.");
        } catch (SQLException e) {
            log("[EROARE RESET]: " + e.getMessage());
        }
    }
}