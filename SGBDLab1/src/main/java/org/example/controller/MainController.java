package org.example.controller;

import org.example.domain.Carte;
import org.example.domain.Editura;
import org.example.service.Service;
import org.example.util.HibernateUtil;
import java.sql.Connection;
import java.sql.DriverManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    private Service service;

    @FXML
    private TableView<Editura> tableEdituri;
    @FXML
    private TableColumn<Editura, Integer> colIdEditura;
    @FXML
    private TableColumn<Editura, String> colNumeEditura;

    @FXML
    private TableView<Carte> tableCarti;
    @FXML
    private TableColumn<Carte, Integer> colIdCarte;
    @FXML
    private TableColumn<Carte, String> colTitluCarte;
    @FXML
    private TableColumn<Carte, Integer> colAnCarte;
    @FXML
    private TableColumn<Carte, String> colIsbnCarte;

    @FXML
    private TextField txtTitlu, txtAn, txtIsbn;
    @FXML
    private Button btnAdd, btnUpdate, btnDelete;

    private ObservableList<Editura> modelEdituri = FXCollections.observableArrayList();
    private ObservableList<Carte> modelCarti = FXCollections.observableArrayList();

    public void setService(Service service) {
        this.service = service;
        if (this.service != null) {
            initModelEdituri();
        }
    }

    private void initModelEdituri() {
        List<Editura> list = new ArrayList<>();
        service.getAllEdituri().forEach(list::add);
        modelEdituri.setAll(list);
    }

    private void initModelCarti(Editura editura) {
        if (editura == null) {
            modelCarti.clear();
            return;
        }
        List<Carte> list = new ArrayList<>();
        service.getCartiByEditura(editura.getId()).forEach(list::add);
        modelCarti.setAll(list);
    }

    @FXML
    public void initialize() {
        colIdEditura.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNumeEditura.setCellValueFactory(new PropertyValueFactory<>("nume"));
        tableEdituri.setItems(modelEdituri);

        colIdCarte.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitluCarte.setCellValueFactory(new PropertyValueFactory<>("titlu"));
        colAnCarte.setCellValueFactory(new PropertyValueFactory<>("anAparitite")); // conform numelui din clasa ta
        colIsbnCarte.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        tableCarti.setItems(modelCarti);

        tableEdituri.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
            if (newSelection != null) {
                initModelCarti(newSelection);
            }
        });

        tableCarti.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtTitlu.setText(newSelection.getTitlu());
                txtAn.setText(String.valueOf(newSelection.getAnAparitite()));
                txtIsbn.setText(newSelection.getIsbn());
            }
        });
    }

    @FXML
    private void handleAddCarte() {
        try {
            Editura selectedEditura = tableEdituri.getSelectionModel().getSelectedItem();
            if (selectedEditura == null) {
                showAlert("Eroare", "Selectați mai întâi o editură din tabelul de sus!");
                return;
            }

            String titlu = txtTitlu.getText();
            Integer an = Integer.parseInt(txtAn.getText());
            String isbn = txtIsbn.getText();

            service.addCarte(titlu, an, isbn, selectedEditura);
            initModelCarti(selectedEditura); // Refresh tabel copil
            clearFields();
        } catch (Exception e) {
            showAlert("Eroare la adăugare", e.getMessage());
        }
    }

    @FXML
    private void handleUpdateCarte() {
        try {
            Carte selectedCarte = tableCarti.getSelectionModel().getSelectedItem();
            if (selectedCarte == null) {
                showAlert("Eroare", "Selectați o carte pentru a o modifica!");
                return;
            }

            service.updateCarte(selectedCarte.getId(), txtTitlu.getText(),
                    Integer.parseInt(txtAn.getText()), txtIsbn.getText(),
                    selectedCarte.getEditura());

            initModelCarti(tableEdituri.getSelectionModel().getSelectedItem());
            clearFields();
        } catch (Exception e) {
            showAlert("Eroare la update", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteCarte() {
        Carte selectedCarte = tableCarti.getSelectionModel().getSelectedItem();
        if (selectedCarte == null) {
            showAlert("Eroare", "Selectați o carte pentru a o șterge!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Sigur doriți să ștergeți cartea?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                service.deleteCarte(selectedCarte.getId());
                initModelCarti(tableEdituri.getSelectionModel().getSelectedItem());
                clearFields();
            }
        });
    }

    @FXML
    private void handleTestPerformanta() {
        int nrConexiuni = 100;
        String url = "jdbc:postgresql://localhost:5432/biblioteca";
        String user = "postgres";
        String pass = "123skem2";

        try {
            // --- TEST 1: FARA POOLING (JDBC PUR) ---
            long startJDBC = System.currentTimeMillis();
            for (int i = 0; i < nrConexiuni; i++) {
                try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                    if (conn.isClosed()) System.out.print("");
                }
            }
            long endJDBC = System.currentTimeMillis();
            long timpTotalJDBC = endJDBC - startJDBC;

            // --- TEST 2: CU POOLING (HikariCP) ---
            long startPool = System.currentTimeMillis();
            for (int i = 0; i < nrConexiuni; i++) {
                try (Connection conn = HibernateUtil.getDataSource().getConnection()) {
                    if (conn.isClosed()) System.out.print("");
                }
            }
            long endPool = System.currentTimeMillis();
            long timpTotalPool = endPool - startPool;

            String rezultat = String.format(
                    "Rezultate pentru %d conexiuni:\n\n" +
                            "Fara Pooling (JDBC): %d ms (Medie: %.2f ms/con)\n" +
                            "Cu Pooling (Hikari): %d ms (Medie: %.2f ms/con)\n\n" +
                            "Imbunatatire: %.2f ori mai rapid!",
                    nrConexiuni,
                    timpTotalJDBC, (double)timpTotalJDBC/nrConexiuni,
                    timpTotalPool, (double)timpTotalPool/nrConexiuni,
                    (double)timpTotalJDBC/timpTotalPool
            );

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Masurare performanta - Lab 3");
            alert.setHeaderText("Overhead-ul Crearii Conexiunilor");
            alert.setContentText(rezultat);
            alert.getDialogPane().setMinWidth(400);
            alert.showAndWait();

        } catch (SQLException e) {
            showAlert("Eroare Test", "Asigurați-vă că baza de date este pornită: " + e.getMessage());
        }
    }

    @FXML
    private void handleLeakDemo() {
        try {
            System.out.println("Se incearca deschiderea a 12 conexiuni fara a le inchide...");
            for (int i = 1; i <= 12; i++) {
                Connection conn = HibernateUtil.getDataSource().getConnection();
                System.out.println("Conexiunea " + i + " a fost ocupata.");

                if (i > 10) {
                    System.out.println("Pool-ul este acum epuizat. Urmatoarea cerere va astepta timeout-ul...");
                }
            }
        } catch (SQLException e) {
            showAlert("Connection Leak Detected",
                    "Eroare: Pool-ul de conexiuni a fost epuizat!\n" +
                            "Mesaj: " + e.getMessage());
        }
    }

    private void clearFields() {
        txtTitlu.clear();
        txtAn.clear();
        txtIsbn.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}