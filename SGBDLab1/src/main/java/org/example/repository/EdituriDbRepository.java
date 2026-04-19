package org.example.repository;

import org.example.domain.Editura;
import java.sql.*;
import java.util.*;

public class EdituriDbRepository implements Repository<Integer, Editura> {
    private String url;
    private String username;
    private String password;

    public EdituriDbRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private Editura getEdituraFromResultSet(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id_editura");
        String nume = rs.getString("nume");
        String adresa = rs.getString("adresa");
        String website = rs.getString("website");
        return new Editura(id, nume, adresa, website);
    }

    private boolean exists(Integer id) {
        String sql = "SELECT 1 FROM Edituri WHERE id_editura = ?";
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
    public Editura find(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must be not null!");
        }

        String sql = "SELECT id_editura, nume, adresa, website FROM Edituri WHERE id_editura = ?";

        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    // Poți înlocui cu o excepție personalizată dacă o ai definită (ex: InexistentEntityException)
                    return null;
                } else {
                    return getEdituraFromResultSet(resultSet);
                }
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return null;
    }

    public void add(Editura editura) {
        String sql = "INSERT INTO Edituri (nume, adresa, website) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, editura.getNume());
            statement.setString(2, editura.getAdresa());
            statement.setString(3, editura.getWebsite());
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public void update(Editura editura) {
        if (!exists(editura.getId())) {
            System.out.println("Editura inexistenta!");
            return;
        }

        String sql = "UPDATE Edituri SET nume = ?, adresa = ?, website = ? WHERE id_editura = ?";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, editura.getNume());
            statement.setString(2, editura.getAdresa());
            statement.setString(3, editura.getWebsite());
            statement.setInt(4, editura.getId());

            statement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM Edituri WHERE id_editura = ?";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    @Override
    public Iterable<Editura> getAll() {
        List<Editura> edituri = new ArrayList<>();
        String sql = "SELECT * FROM Edituri";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                edituri.add(getEdituraFromResultSet(resultSet));
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return edituri;
    }

    @Override
    public Map<Integer, Editura> getMap() {
        Map<Integer, Editura> edituraMap = new HashMap<>();
        for (Editura e : getAll()) {
            edituraMap.put(e.getId(), e);
        }
        return edituraMap;
    }

    @Override
    public void clear() {
        String sql = "DELETE FROM Edituri";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }
}