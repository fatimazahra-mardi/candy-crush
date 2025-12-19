package game;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class CandyCrushApp extends Application {

    @Override
    public void start(Stage stage) {

        GridPane grid = GridView.createGrid(8, 8, 100); // taille augmentée

        BorderPane root = new BorderPane();

        // placer le jeu à droite
        root.setRight(grid);

        // marge à droite et en haut
        BorderPane.setMargin(grid, new javafx.geometry.Insets(40, 40, 40, 40));

        // la taille de la scene (jeux)
        Scene scene = new Scene(root, 1100, 800);

        stage.setTitle("Candy Crush");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
