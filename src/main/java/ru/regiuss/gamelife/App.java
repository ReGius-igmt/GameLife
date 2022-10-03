package ru.regiuss.gamelife;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    public static Stage ps;

    @Override
    public void start(Stage stage) throws Exception {
        ps = stage;
        stage.setWidth(800);
        stage.setHeight(600);
        stage.setTitle("GameLife");
        Parent p = new FXMLLoader(getClass().getResource("/view/main.fxml")).load();
        stage.setScene(new Scene(p));
        stage.show();
    }
}
