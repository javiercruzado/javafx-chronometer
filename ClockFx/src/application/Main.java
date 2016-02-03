package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			VBox root = (VBox) FXMLLoader.load(getClass().getResource("Clock.fxml"));
			primaryStage.setTitle("Clock");
			primaryStage.getIcons().add(new Image("javafx.png"));
			// Scene scene = new Scene(root, 480, 120);
			Scene scene = new Scene(root, 480, 180);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
