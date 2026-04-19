package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controller.MainController;
import org.example.repository.CartiHibernateRepository;
import org.example.repository.Repository;
import org.example.repository.factory.RepositoryEntity;
import org.example.repository.factory.RepositoryFactory;
import org.example.service.Service;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        try {
            var edituraRepo = RepositoryFactory.getInstance().createRepository(RepositoryEntity.EDITURI);
            var autorRepo = RepositoryFactory.getInstance().createRepository(RepositoryEntity.AUTORI);
            var carteRepo = RepositoryFactory.getInstance().createRepository(RepositoryEntity.CARTI);

            Service service = new Service(edituraRepo, autorRepo, (Repository) carteRepo);

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);

            MainController mainController = fxmlLoader.getController();
            mainController.setService(service);

            stage.setTitle("Hibernate & Connection Pooling");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) { launch(); }
}