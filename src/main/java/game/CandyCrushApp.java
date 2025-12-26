package game;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.animation.PauseTransition;
import javafx.scene.Cursor;




public class CandyCrushApp extends Application {

    @Override
    public void start(Stage stage) {

        GridPane grid = GridView.createGrid(8, 8, 100); // taille augmentée

        BorderPane root = new BorderPane();
        StackPane rootStack = new StackPane(); 
        // 🔗 connexion avec GridView
        GridView.setRootStack(rootStack);
        
            BackgroundImage bg = new BackgroundImage(
            new Image(CandyCrushApp.class.getResourceAsStream("/image/image9.jpg")),
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(
                    BackgroundSize.AUTO,
                    BackgroundSize.AUTO,
                    false,
                    false,
                    true,
                    true
            )
        );

        root.setBackground(new Background(bg));


        // placer le jeu à droite
        // placer le jeu à droite
        root.setRight(grid);

        // === SCORE PANEL CENTRÉ ===
        StackPane scoreWrapper = new StackPane();
        scoreWrapper.setPrefWidth(250); // largeur de la zone gauche
        scoreWrapper.setAlignment(Pos.CENTER);

        scoreWrapper.getChildren().add(GridView.createScorePanel());

        root.setLeft(scoreWrapper);
        BorderPane.setMargin(scoreWrapper, new Insets(40, 20, 40, 40));


        // marge à droite et en haut
        BorderPane.setMargin(grid, new Insets(
        80,  // TOP  ⬆️
        40,  // RIGHT
        80,  // BOTTOM ⬇️
        40   // LEFT
        ));

        rootStack.getChildren().add(root);
        root.setVisible(false); // on cache le jeu au départ

        // ================= LOADING SCREEN =================

        StackPane loadingOverlay = new StackPane();
        Image splash = new Image(
        CandyCrushApp.class.getResourceAsStream("/image/splash.jpeg"),
        1100,   // largeur = taille de la scène
        800,    // hauteur = taille de la scène
        false,  // preserveRatio → false = couvre tout
        true    // smooth = true (IMPORTANT)
        );


            BackgroundImage loadingBg = new BackgroundImage(
        splash,
        BackgroundRepeat.NO_REPEAT,
        BackgroundRepeat.NO_REPEAT,
        BackgroundPosition.CENTER,
        new BackgroundSize(
            100, 100,     // largeur / hauteur en %
            true, true,   // units = %
            false,        // preserve ratio OFF → cover total
            true          // cover = TRUE ⭐⭐⭐
        )
        );



        loadingOverlay.setBackground(new Background(loadingBg));



        // barre de progression
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);     // un peu plus courte (ajuste 400-460)
        progressBar.setPrefHeight(25);     // plus épaisse
        progressBar.setMinHeight(20);
        progressBar.setMaxHeight(30);
        progressBar.setStyle("""
            -fx-accent: #ff5aa5;
            -fx-control-inner-background: #ffe4f0;
        """);

        // pourcentage
        Label percentLabel = new Label("0%");
        percentLabel.setStyle(
            "-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #c2185b;"
        );

        // bouton Play (caché au début)
        Button playButton = new Button("Play Now");
        playButton.setVisible(false);
        playButton.setStyle("""
            -fx-background-color: #ff4fa1;
            -fx-text-fill: white;
            -fx-font-size: 22;
            -fx-font-weight: bold;
            -fx-padding: 10 40;
            -fx-background-radius: 30;
        """);
        playButton.setCursor(Cursor.HAND);


        // layout
        VBox loadingBox = new VBox(20, progressBar, percentLabel, playButton);
        // ✅ ICI EXACTEMENT
        loadingBox.setAlignment(Pos.BOTTOM_CENTER);
        loadingBox.setPadding(new Insets(0, 0, 60, 0));

        // ensuite seulement on l’ajoute
        loadingOverlay.getChildren().add(loadingBox);
        rootStack.getChildren().add(loadingOverlay);
        Platform.runLater(() -> {
        Node bar = progressBar.lookup(".bar");
        Node track = progressBar.lookup(".track");

    if (bar != null) {
        bar.setStyle("""
            -fx-background-insets: 0;
            -fx-background-radius: 10;
            -fx-padding: 0;
            -fx-background-color: 
                repeating-linear-gradient(
                    45deg,
                    #ff5aa5 0px,
                    #ff5aa5 10px,
                    #e91e63 10px,
                    #e91e63 20px
                );
        """);
    }

    if (track != null) {
        track.setStyle("""
            -fx-background-insets: 0;
            -fx-background-radius: 10;
            -fx-background-color: #ffe4f0;
        """);
    }
});



        Timeline loadingTimeline = new Timeline();
        final double[] progress = {0};

        loadingTimeline.getKeyFrames().add(
        new KeyFrame(Duration.millis(15), e -> {
        progress[0] += 0.01;
        if (progress[0] >= 1) {
            progress[0] = 1;
            loadingTimeline.stop();
            percentLabel.setText("100%");
            PauseTransition pause = new PauseTransition(Duration.seconds(2)); // 2 ou 3
            pause.setOnFinished(ev -> {
                progressBar.setVisible(false);
                percentLabel.setVisible(false);
                playButton.setVisible(true);
            });
            pause.play();

        } else {
            percentLabel.setText((int)(progress[0] * 100) + "%");
        }
        progressBar.setProgress(progress[0]);
        })
        );

        loadingTimeline.setCycleCount(Timeline.INDEFINITE);
        loadingTimeline.play();

        playButton.setOnAction(e -> {
        rootStack.getChildren().remove(loadingOverlay); //  retire le loading
        root.setVisible(true);                           // affiche le jeu
        });


        // la taille de la scene (jeux)
        Scene scene = new Scene(rootStack, 1100, 800);

        stage.setTitle("Candy Crush");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
