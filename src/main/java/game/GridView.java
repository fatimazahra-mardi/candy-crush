package game;
import javafx.scene.media.AudioClip;
import java.net.URL;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.Cursor;

import java.util.Random;

public class GridView {

    private static StackPane[][] gridCells;
    private static int[][] board;

    private static StackPane firstSelected = null;

    private static boolean gameOver = false;

    private static int ROWS;
    private static int COLS;
    private static int CELL_SIZE;
    // ================== SCORE ==================
    private static int score = 0;
    private static Label scoreLabel;
    private static int comboMultiplier = 1; // pour la cascade (1, puis 2, puis 3...)
    // ================== MOVES ==================
    private static int moves = 20;      // nombre de coups autorisés
    private static Label movesLabel;    // affichage des coups restants
    // ================== TARGET ==================
    private static final int TARGET_SCORE = 6000;
    private static Label targetLabel;

    //=======================
    
    private static StackPane rootStack;





    private static final Random random = new Random();

    private static final String[] IMAGES = {
            "/image/image1.jpg",
            "/image/image2.jpg",
            "/image/image3.jpg",
            "/image/image4.jpg",
            "/image/image5.jpg",
            "/image/image6.jpg",
            "/image/image7.jpg",
            "/image/image8.jpg"

    };


    // ================== SOUND ==================
private static final AudioClip SOUND_SWAP  = loadSound("/sound/swap.wav");
private static final AudioClip SOUND_MATCH = loadSound("/sound/match.wav");
private static final AudioClip SOUND_GAME_OVER = loadSound("/sound/game_over.wav");

private static AudioClip loadSound(String path) {
    URL url = GridView.class.getResource(path);
    if (url == null) {
        System.out.println("Sound not found: " + path);
        return null;
    }
    return new AudioClip(url.toExternalForm());
}

private static void play(AudioClip clip) {
    if (clip != null) {
        clip.play();
    }
}


    // ================== INIT ==================
    public static GridPane createGrid(int rows, int cols, int cellSize) {

        ROWS = rows;
        COLS = cols;
        CELL_SIZE = cellSize;

        board = new int[rows][cols];
        gridCells = new StackPane[rows][cols];

        GridPane grid = new GridPane();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                board[r][c] = generateSafeCandy(r, c);

                StackPane cell = createCell(r, c);
                gridCells[r][c] = cell;
                grid.add(cell, c, r);
            }
        }

        refreshView();
        // init score UI si pas encore créé
        if (scoreLabel == null) {
            scoreLabel = new Label();
            scoreLabel.setFont(Font.font(28));
        }
        updateScoreLabel();

        return grid;
    }

    private static StackPane createCell(int r, int c) {
        StackPane cell = new StackPane();
        cell.setPrefSize(CELL_SIZE, CELL_SIZE);
        cell.setStyle("-fx-background-color:#f5f5f5; -fx-border-color:#dddddd;");
        cell.setUserData(new int[]{r, c});

        cell.setOnMouseClicked(e -> handleClick(cell));
        return cell;
    }

    // ================== CLICK ==================
  private static void handleClick(StackPane cell) {

    if (gameOver || moves <= 0) return;

    if (firstSelected == null) {
        firstSelected = cell;
        selectCell(cell);
        return;
    }

    if (!areAdjacent(firstSelected, cell)) {
        unselectCell(firstSelected);
        firstSelected = null;
        return;
    }

    // 1️⃣ Swap logique uniquement
    swap(firstSelected, cell);
    play(SOUND_SWAP);

    // 2️⃣ Vérifier s’il y a un match
    boolean[][] matches = CandyLogic.detectMatches(board);
    boolean hasMatch = false;

    for (int r = 0; r < ROWS; r++) {
        for (int c = 0; c < COLS; c++) {
            if (matches[r][c]) {
                hasMatch = true;
                break;
            }
        }
        if (hasMatch) break;
    }

    moves--;                 // 1️⃣ on consomme un move
    updateMovesLabel();      // 2️⃣ on met à jour l’UI

    if (hasMatch) {
        // ✔️ Coup valide → afficher le swap
        refreshView();
        resolveWithAnimation();


    } else {
        // ❌ Coup invalide → annuler immédiatement
         play(SOUND_GAME_OVER);
        swap(firstSelected, cell);
        comboMultiplier = 1;
        refreshView();
        
    }

    unselectCell(firstSelected);
    firstSelected = null;
    checkEndGame();

}

    private static boolean areAdjacent(StackPane a, StackPane b) {
        int[] p1 = (int[]) a.getUserData();
        int[] p2 = (int[]) b.getUserData();
        return Math.abs(p1[0] - p2[0]) + Math.abs(p1[1] - p2[1]) == 1;
    }

    private static void swap(StackPane a, StackPane b) {
        int[] p1 = (int[]) a.getUserData();
        int[] p2 = (int[]) b.getUserData();
        int t = board[p1[0]][p1[1]];
        board[p1[0]][p1[1]] = board[p2[0]][p2[1]];
        board[p2[0]][p2[1]] = t;
    }

    // ================== RESOLUTION ==================
    private static void resolveWithAnimation() {

        boolean[][] matches = CandyLogic.detectMatches(board);
        boolean found = false;

        int removedCount = 0;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (matches[r][c]) {
                    found = true;
                    removedCount++;          // 1 on compte les bonbons supprimés
                    animateRemove(r, c);     // 2 on lance l’animation de suppression
                }
            }
        }


        if (!found) return;
        play(SOUND_MATCH);

        if (!found) {
            comboMultiplier = 1; // plus de match → fin de cascade → reset combo
            return;
        }

        play(SOUND_MATCH);

        // SCORE : 10 points par bonbon * multiplicateur de combo
        int base = 10;
        score += removedCount * base * comboMultiplier;
        comboMultiplier++;
        updateScoreLabel();



        PauseTransition wait = new PauseTransition(Duration.millis(350));
        wait.setOnFinished(e -> {
            applyGravity();
            refillBoard();
            refreshView();
            resolveWithAnimation(); // cascade
        });
        wait.play();
    }

    private static void animateRemove(int r, int c) {

        ImageView iv = (ImageView) gridCells[r][c].getChildren().get(0);

        FadeTransition ft = new FadeTransition(Duration.millis(300), iv);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            board[r][c] = -1;
            gridCells[r][c].getChildren().clear();
        });
        ft.play();
    }

    // ================== GRAVITY ==================
    private static void applyGravity() {

        for (int c = 0; c < COLS; c++) {
            int write = ROWS - 1;

            for (int r = ROWS - 1; r >= 0; r--) {
                if (board[r][c] != -1) {
                    board[write][c] = board[r][c];
                    write--;
                }
            }

            while (write >= 0) {
                board[write][c] = -1;
                write--;
            }
        }
    }

    // ================== REFILL ==================
    private static void refillBoard() {

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] == -1) {
                    board[r][c] = random.nextInt(IMAGES.length);
                }
            }
        }
    }
    
    private static void updateScoreLabel() {
    scoreLabel.setText(String.valueOf(score));
        // 🎬 Animation Candy Crush (score)
        ScaleTransition st = new ScaleTransition(Duration.millis(150), scoreLabel);
        st.setFromX(1);
        st.setFromY(1);
        st.setToX(1.2);
        st.setToY(1.2);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    public static VBox createScorePanel() {

        Label title = new Label("Candy Crush");
        title.setFont(Font.font("Arial", 26));

        scoreLabel = new Label("0");
        scoreLabel.setFont(Font.font("Arial", 20));

        movesLabel = new Label();
        movesLabel.setFont(Font.font("Arial", 18));
        updateMovesLabel();

        targetLabel = new Label();
        targetLabel.setFont(Font.font("Arial", 18));
        updateTargetLabel();


        VBox targetBox = createInfoBox("Target", targetLabel);
        VBox scoreBox  = createInfoBox("Score", scoreLabel);
        VBox movesBox  = createInfoBox("Moves", movesLabel);

        VBox box = new VBox(20);
        box.getChildren().addAll(title, targetBox, scoreBox, movesBox);
        box.setAlignment(Pos.CENTER);

        VBox wrapper = new VBox();
        wrapper.setAlignment(Pos.CENTER);     // centre verticalement
        wrapper.setPadding(new Insets(30, 0, 30, 0)); // marge haut / bas
        wrapper.getChildren().add(box);




        box.setStyle(
            "-fx-background-color: #f6c1d1;" +
            "-fx-border-color: #c85a7a;" +
            "-fx-border-width: 5;" +
            "-fx-background-radius: 25;" +
            "-fx-border-radius: 25;" +
            "-fx-padding: 25;"
        );

        box.setPrefWidth(220);
        box.setAlignment(Pos.TOP_CENTER);

        return wrapper;
        
    }

    private static VBox createInfoBox(String titleText, Label valueLabel) {

    Label title = new Label(titleText);
    title.setStyle("""
        -fx-font-size: 14;
        -fx-text-fill: #880e4f;
        -fx-font-weight: bold;
    """);

    valueLabel.setStyle("""
        -fx-font-size: 22;
        -fx-text-fill: #c2185b;
        -fx-font-weight: bold;
    """);

    VBox box = new VBox(6, title, valueLabel);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(12));
    box.setPrefWidth(190);

    box.setStyle("""
        -fx-background-color: #ffd6e7;
        -fx-background-radius: 16;
        -fx-border-color: #c2185b;
        -fx-border-width: 3;
        -fx-border-radius: 16;
    """);

    return box;
    }




    // ================== VIEW ==================
    private static void refreshView() {

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {

                gridCells[r][c].getChildren().clear();
                int type = board[r][c];
                if (type == -1) continue;

                ImageView iv = new ImageView(
                        new Image(GridView.class.getResourceAsStream(IMAGES[type]))
                );

                iv.setFitWidth(CELL_SIZE - 10);
                iv.setFitHeight(CELL_SIZE - 10);
                iv.setPreserveRatio(true);
                iv.setEffect(new DropShadow(6, Color.rgb(0,0,0,0.4)));

                gridCells[r][c].getChildren().add(iv);
            }
        }
    }
//--------------------------------

private static int generateSafeCandy(int r, int c) {
    int type;
    do {
        type = random.nextInt(IMAGES.length);
    } while (
        (c >= 2 && board[r][c - 1] == type && board[r][c - 2] == type) ||
        (r >= 2 && board[r - 1][c] == type && board[r - 2][c] == type)
    );
    return type;
}
    private static void selectCell(StackPane c) {
        c.setStyle("-fx-background-color:#e0e0e0; -fx-border-color:gold; -fx-border-width:3;");
    }

    private static void unselectCell(StackPane c) {
        c.setStyle("-fx-background-color:#f5f5f5; -fx-border-color:#dddddd;");
    }

    private static void updateMovesLabel() {
    movesLabel.setText(String.valueOf(moves));
    }

   private static void updateTargetLabel() {
    targetLabel.setText(String.valueOf(TARGET_SCORE));
    }


    private static void checkEndGame() {
    if (gameOver) return;

    if (score >= TARGET_SCORE) {
        gameOver = true;
        showEndPopup(true);
    }
    else if (moves <= 0) {
        gameOver = true;
        showEndPopup(false);
    }

    }

    private static void showEndPopup(boolean win) {

    StackPane overlay = new StackPane();
    overlay.setStyle("-fx-background-color: rgba(0,0,0,0.6);");

    VBox popup = new VBox(20);
    popup.setAlignment(Pos.CENTER);
    popup.setPadding(new Insets(30));
    // 🔹 largeur et hauteur contrôlées
    popup.setMaxWidth(360);
    popup.setPrefHeight(420);      // 👈 hauteur fixe (clé !)
    popup.setMaxHeight(420);
    popup.setMinHeight(380);
    StackPane.setMargin(popup, new Insets(60, 0, 60, 0));


    popup.setStyle("""
        -fx-background-color: #ffd6e7;
        -fx-background-radius: 25;
        -fx-border-color: #c2185b;
        -fx-border-width: 4;
        -fx-border-radius: 25;
    """);

    Label title = new Label(win ? " 🎉 You Win!" : " 😢 You Failed!");
    title.setFont(Font.font("Arial", FontWeight.BOLD, 26));
    title.setTextFill(Color.web("#b0003a"));

    Label scoreTitle = new Label("Final Score");
    scoreTitle.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 16));
    scoreTitle.setTextFill(Color.web("#7a1c3d"));

    Label movesLeftLabel = null;
    if (moves > 0) {
        movesLeftLabel = new Label("You crushed it 💥 with " + moves + " moves left!");
        movesLeftLabel.setFont(
            Font.font("Arial", FontWeight.SEMI_BOLD, 17)
        );

        movesLeftLabel.setTextFill(
            Color.web("#c2185b") // rose Candy Crush
        );

    }

    Label scoreValue = new Label(String.valueOf(score));
    scoreValue.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 36));
    scoreValue.setTextFill(Color.web("#c2185b"));

    Button retry = new Button("Retry");
    retry.setStyle("""
        -fx-background-color: #ff4f9a;
        -fx-text-fill: white;
        -fx-font-size: 16;
        -fx-padding: 10 30;
        -fx-background-radius: 20;
    """);
    retry.setCursor(Cursor.HAND);

    retry.setOnAction(e -> {
        resetGame();
        ((Pane) overlay.getParent()).getChildren().remove(overlay);
    });

    popup.getChildren().add(title);
    popup.getChildren().add(scoreTitle);
    popup.getChildren().add(scoreValue);

    if (movesLeftLabel != null) {
        popup.getChildren().add(movesLeftLabel);
    }

    popup.getChildren().add(retry);

    overlay.getChildren().add(popup);

    rootStack.getChildren().add(overlay);
    
    }

    private static void resetGame() {
    score = 0;
    moves = 20;
    comboMultiplier = 1;
    gameOver = false;

    updateScoreLabel();
    updateMovesLabel();

    refillBoard();
    refreshView();
    }



    public static void setRootStack(StackPane root) {
    rootStack = root;

    }





}


