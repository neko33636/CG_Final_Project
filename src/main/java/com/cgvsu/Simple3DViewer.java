package com.cgvsu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Simple3DViewer extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/gui.fxml"));
        AnchorPane root = loader.load();
        Scene scene = new Scene(root);
        GuiController controller = loader.getController();
        controller.setScene(scene);
        stage.setScene(scene);
        stage.setTitle("Simple3DViewer");
        stage.setMinWidth(1600);
        stage.setMinHeight(900);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}