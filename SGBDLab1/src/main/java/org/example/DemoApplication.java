//package org.example;
//
//import javafx.application.Application;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
//import org.example.controller.TransactionLabController;
//import java.io.IOException;
//import java.net.URL;
//
//public class DemoApplication extends Application {
//
//    @Override
//    public void start(Stage stage) throws IOException {
//        try {
//            var resources = getClass().getClassLoader()
//                    .getResources("org/example")
//                    .asIterator();
//            while (resources.hasNext()) {
//                System.out.println("Found: " + resources.next());
//            }
//        } catch (Exception e) { e.printStackTrace(); }
//        String fxmlPath = "/org/example/transaction-lab-view.fxml";
//        URL fxmlLocation = getClass().getResource(fxmlPath);
//
//        if (fxmlLocation == null) {
//            fxmlLocation = getClass().getResource("/org.example/transaction-lab-view.fxml");
//        }
//
//        if (fxmlLocation == null) {
//            System.err.println("CRITICAL: FXML negasit la: " + fxmlPath);
//            return;
//        }
//
//        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
//        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
//        stage.setTitle("SGBD Demonstrations");
//        stage.setScene(scene);
//        stage.show();
//    }
//
//    public static void main(String[] args) {
//        launch();
//    }
//}