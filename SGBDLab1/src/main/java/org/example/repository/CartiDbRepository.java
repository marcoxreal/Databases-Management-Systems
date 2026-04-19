package org.example.repository;

import org.example.domain.Carte;
import org.example.domain.Editura;
import java.sql.*;
import java.util.*;

public class CartiDbRepository implements Repository<Integer, Carte> {
    private String url;
    private String username;
    private String password;

    public CartiDbRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private Carte getCarteFromResultSet(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id_carte");
        String titlu = rs.getString("titlu");
        Integer anAparitie = rs.getInt("an_aparitie");
        String isbn = rs.getString("isbn");

        // Reconstituim obiectul Editura (minim ID-ul, restul pot fi luate prin join daca e nevoie)
        // Pentru simplitatea Repository-ului, aducem datele editurii prin coloanele din DB
        Integer idEditura = rs.getInt("id_editura");
        String numeEditura = rs.getString("nume_editura");
        Editura ed = new Editura(idEditura, numeEditura, null, null);

        return new Carte(id, titlu, anAparitie, isbn, ed);
    }

    private boolean exists(Integer id) {
        String sql = "SELECT 1 FROM Carti WHERE id_carte = ?";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return false;
    }

    @Override
    public Carte find(Integer id) {
        String sql = "SELECT c.*, e.nume AS nume_editura FROM Carti c " +
                "JOIN Edituri e ON c.id_editura = e.id_editura WHERE c.id_carte = ?";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return getCarteFromResultSet(resultSet);
                }
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return null;
    }

    /**
     * Metoda cruciala pentru interfata Master-Detail
     */
    public Iterable<Carte> findByEditura(Integer idEditura) {
        List<Carte> carti = new ArrayList<>();
        String sql = "SELECT c.*, e.nume AS nume_editura FROM Carti c " +
                "JOIN Edituri e ON c.id_editura = e.id_editura " +
                "WHERE c.id_editura = ?";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idEditura);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    carti.add(getCarteFromResultSet(resultSet));
                }
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return carti;
    }

    public void add(Carte carte) {
        String sql = "INSERT INTO Carti (titlu, an_aparitie, isbn, id_editura) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, carte.getTitlu());
            statement.setInt(2, carte.getAnAparitite());
            statement.setString(3, carte.getIsbn());
            statement.setInt(4, carte.getEditura().getId());
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public void update(Carte carte) {
        if (!exists(carte.getId())) return;

        String sql = "UPDATE Carti SET titlu = ?, an_aparitie = ?, isbn = ?, id_editura = ? WHERE id_carte = ?";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, carte.getTitlu());
            statement.setInt(2, carte.getAnAparitite());
            statement.setString(3, carte.getIsbn());
            statement.setInt(4, carte.getEditura().getId());
            statement.setInt(5, carte.getId());
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM Carti WHERE id_carte = ?";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    @Override
    public Iterable<Carte> getAll() {
        List<Carte> carti = new ArrayList<>();
        String sql = "SELECT c.*, e.nume AS nume_editura FROM Carti c " +
                "JOIN Edituri e ON c.id_editura = e.id_editura";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                carti.add(getCarteFromResultSet(resultSet));
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return carti;
    }

    @Override
    public Map<Integer, Carte> getMap() {
        Map<Integer, Carte> map = new HashMap<>();
        for (Carte c : getAll()) {
            map.put(c.getId(), c);
        }
        return map;
    }

    @Override
    public void clear() {
        String sql = "DELETE FROM Carti";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }
}