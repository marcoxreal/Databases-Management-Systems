package org.example.repository;

import org.example.domain.Autor;
import java.sql.*;
import java.util.*;

public class AutoriDbRepository implements Repository<Integer, Autor> {
    private String url;
    private String username;
    private String password;

    public AutoriDbRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private Autor getAutorFromResultSet(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id_autor");
        String nume = rs.getString("nume_autor");
        String nationalitate = rs.getString("nationalitate");
        return new Autor(id, nume, nationalitate);
    }

    private boolean exists(Integer id) {
        String sql = "SELECT 1 FROM Autori WHERE id_autor = ?";
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
    public Autor find(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must be not null!");
        }

        String sql = "SELECT id_autor, nume_autor, nationalitate FROM Autori WHERE id_autor = ?";

        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                } else {
                    return getAutorFromResultSet(resultSet);
                }
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return null;
    }

    public void add(Autor autor) {
        String sql = "INSERT INTO Autori (nume_autor, nationalitate) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, autor.getNume());
            statement.setString(2, autor.getNationalitate());
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public void update(Autor autor) {
        if (!exists(autor.getId())) {
            System.out.println("Autor inexistent!");
            return;
        }

        String sql = "UPDATE Autori SET nume_autor = ?, nationalitate = ? WHERE id_autor = ?";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, autor.getNume());
            statement.setString(2, autor.getNationalitate());
            statement.setInt(3, autor.getId());

            statement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM Autori WHERE id_autor = ?";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    @Override
    public Iterable<Autor> getAll() {
        List<Autor> autori = new ArrayList<>();
        String sql = "SELECT * FROM Autori";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                autori.add(getAutorFromResultSet(resultSet));
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return autori;
    }

    @Override
    public Map<Integer, Autor> getMap() {
        Map<Integer, Autor> autorMap = new HashMap<>();
        for (Autor a : getAll()) {
            autorMap.put(a.getId(), a);
        }
        return autorMap;
    }

    @Override
    public void clear() {
        String sql = "DELETE FROM Autori";
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }
}