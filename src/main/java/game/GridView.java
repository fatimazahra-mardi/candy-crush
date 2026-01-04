package game;
import javafx.scene.media.AudioClip;
import java.net.URL;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
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
    


        // level
    private static int level = 1;
    private static int BASE_TARGET = 1000;
    // private static int TARGET_SCORE = baseTarget;
    private static int TARGET_SCORE = BASE_TARGET;


    private static Label targetLabel;

    //=======================
    
    private static StackPane gameOverOverlay; // référence globale
    private static StackPane rootStack;

// ================= TIME ============
private static int timeLeft = 70; // en secondes
private static Label timeLabel;
private static Timeline timer;





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
// ================== SOUND ==================
private static final AudioClip SOUND_SWAP =
        new AudioClip(GridView.class.getResource("/sound/swap.wav").toExternalForm());

private static final AudioClip SOUND_MATCH =
        new AudioClip(GridView.class.getResource("/sound/match.wav").toExternalForm());

private static final AudioClip SOUND_WIN =
        new AudioClip(GridView.class.getResource("/sound/win.mp3").toExternalForm());

private static final AudioClip SOUND_FAIL =
        new AudioClip(GridView.class.getResource("/sound/faild.mp3").toExternalForm());


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
        play(SOUND_SWAP);

        return;
    }

    if (!areAdjacent(firstSelected, cell)) {
        unselectCell(firstSelected);
        firstSelected = null;
        return;
    }

    // 1️⃣ Swap logique uniquement
//    play(SOUND_FAIL);   // son swap invalide
swap(firstSelected, cell);


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
         play(SOUND_FAIL);

        swap(firstSelected, cell);
        comboMultiplier = 1;
        refreshView();
        
    }

    unselectCell(firstSelected);
    firstSelected = null;
    checkEndGame();
    checkNoMovesGameOver();
    if (!hasAnyPossibleMove() && !gameOver) {
    gameOver = true;
    showEndPopup(false);
}


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
        System.out.println("CHECK SCORE = " + score + " TARGET = " + TARGET_SCORE);


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


        // if (!found) return;
        // play(SOUND_MATCH);

        if (!found) {
            comboMultiplier = 1; // plus de match → fin de cascade → reset combo
             checkEndGame();
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

 if (score >= TARGET_SCORE) {
    checkEndGame();
    return;
}

if (!CandyLogic.hasPossibleMove(board)) {
    gameOver = true;
    showEndPopup(false);
}


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
// TIME GAME OVER 

      timeLabel = new Label();
        timeLabel.setFont(Font.font("Arial", 18));
        updateTimeLabel();

        VBox timeBox = createInfoBox("Time", timeLabel);
        // box.getChildren().add(timeBox);
        


        targetLabel = new Label();
        targetLabel.setFont(Font.font("Arial", 18));
        updateTargetLabel();


        VBox targetBox = createInfoBox("Target", targetLabel);
        VBox scoreBox  = createInfoBox("Score", scoreLabel);
        VBox movesBox  = createInfoBox("Moves", movesLabel);

        VBox box = new VBox(20);
        box.getChildren().addAll(title, targetBox, scoreBox, movesBox, timeBox);
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

    private static void updateTimeLabel() {
    timeLabel.setText(timeLeft + "s");
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
    if (gameOverOverlay != null) return;

    if (score >= TARGET_SCORE) {
        showEndPopup(true);
        return;
    }

    if (moves <= 0 || timeLeft <= 0 || !CandyLogic.hasPossibleMove(board)) {
        showEndPopup(false);
    }
}


// private static void showGameOverBar() {

//     Label bar = new Label("GAME OVER");
//     bar.setPrefHeight(70);
//     bar.setMaxWidth(Double.MAX_VALUE);
//     bar.setAlignment(Pos.CENTER);

//     bar.setStyle("""
//         -fx-background-color: linear-gradient(#ff4f9a, #e91e63);
//         -fx-text-fill: white;
//         -fx-font-size: 28;
//         -fx-font-weight: bold;
//     """);

//     StackPane.setAlignment(bar, Pos.TOP_CENTER);
//     bar.setTranslateY(-100);
//     rootStack.getChildren().add(bar);

//     TranslateTransition slide = new TranslateTransition(Duration.millis(450), bar);
//     slide.setToY(0);
//     slide.play();
// }
private static void showEndPopup(boolean win) {

    if (gameOverOverlay != null) return;

    // WIN FINAL AU LEVEL 4 → POPUP FINAL DIRECT
    if (win && level == 4) {
        showFinalWinPopup();
        return;
    }

    gameOver = true;
   

    if (timer != null) timer.stop();

    play(win ? SOUND_WIN : SOUND_FAIL);

    // OVERLAY SOMBRE
    gameOverOverlay = new StackPane();
    gameOverOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.55);");

    // CARTE CENTRALE (STYLE CANDY CRUSH)
    VBox card = new VBox(18);
    card.setAlignment(Pos.CENTER);
    card.setPadding(new Insets(30));
    card.setPrefWidth(520);     // largeur > hauteur
    card.setMaxHeight(260);

    card.setStyle("""
        -fx-background-color: linear-gradient(#ffe6f0, #ffd1e8);
        -fx-background-radius: 30;
        -fx-border-color: #ff4f9a;
        -fx-border-width: 4;
        -fx-border-radius: 30;
    """);

    // TITRE
    Label title = new Label(
        win ? "🎉 SWEET VICTORY 🎉" : "😢 TRY AGAIN"
    );
    title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
    title.setTextFill(Color.web("#c2185b"));

    // SOUS-TITRE
    Label subtitle = new Label(
        win ? "Level " + level + " completed" : ""
    );
    subtitle.setFont(Font.font(18));
    subtitle.setTextFill(Color.web("#6a1b9a"));

    // BOUTON
    Button action = new Button(win ? "Next Level" : "Retry");
    action.setStyle("""
        -fx-background-color: #ff4f9a;
        -fx-text-fill: white;
        -fx-font-size: 16;
        -fx-padding: 10 40;
        -fx-background-radius: 25;
    """);

    action.setOnAction(e -> {
        rootStack.getChildren().remove(gameOverOverlay);
        gameOverOverlay = null;

        if (win) {
            nextLevel();
        } else {
            resetGame();
        }
    });

    card.getChildren().addAll(title, subtitle, action);
    gameOverOverlay.getChildren().add(card);
    rootStack.getChildren().add(gameOverOverlay);

    // ANIMATION CANDY CRUSH (SCALE + FADE)
    ScaleTransition scale = new ScaleTransition(Duration.millis(400), card);
    scale.setFromX(0.7);
    scale.setFromY(0.7);
    scale.setToX(1);
    scale.setToY(1);

    FadeTransition fade = new FadeTransition(Duration.millis(400), card);
    fade.setFromValue(0);
    fade.setToValue(1);

    new ParallelTransition(scale, fade).play();
}



    // private static void resetGame() {
    // score = 0;
    // moves = 20;
    // comboMultiplier = 1;
    // gameOver = false;
    // timeLeft = 60;
    // updateTimeLabel();
    // startTimer(60);

    // updateScoreLabel();
    // updateMovesLabel();

    // refillBoard();
    // refreshView();
    // }
  private static void resetGame() {
    if (gameOverOverlay != null) {
    rootStack.getChildren().remove(gameOverOverlay);
    gameOverOverlay = null;
}

    score = 0;
    moves = 20;
    comboMultiplier = 1;
    gameOver = false;

    timeLeft = 60;
    startTimer(60);

    // recréer la matrice complètement
    board = new int[ROWS][COLS];
    gridCells = new StackPane[ROWS][COLS];

    GridPane newGrid = createGrid(ROWS, COLS, CELL_SIZE);

    BorderPane root = (BorderPane) rootStack.getChildren().get(0);
    root.setRight(newGrid);

    updateTargetLabel();
    updateScoreLabel();
    updateMovesLabel();
}


public static void startTimer(int seconds) {
    timeLeft = seconds;
    updateTimeLabel();

    if (timer != null) {
        timer.stop();
    }

    timer = new Timeline(
        new KeyFrame(Duration.seconds(1), e -> {
            if (gameOver) {
                timer.stop();
                return;
            }

            timeLeft--;
            updateTimeLabel();
// play(SOUND_FAIL);
           if (timeLeft <= 0) {
    timer.stop();
    gameOver = true;

    boolean win = score >= TARGET_SCORE;

    if (win) {
        play(SOUND_WIN);
    } else {
        play(SOUND_FAIL);
        resetGame();
        // play(SOUND_FAIL);
    }

    showEndPopup(win);
    if (timeLeft <= 0) {
    timer.stop();
    showEndPopup(score >= TARGET_SCORE);
}

}

        })
    );

    timer.setCycleCount(Timeline.INDEFINITE);
    timer.play();
}


    public static void setRootStack(StackPane root) {
    rootStack = root;

}

public static void updateTimeLabelPublic() {
    updateTimeLabel();
}

public static void decrementTime() {
    if (gameOver) return;
    timeLeft--;
    updateTimeLabel();
}
public static boolean isGameOver() {
    return gameOver;
}

public static void forceGameOver(boolean win) {
    if (gameOver) return;

    gameOver = true;

    if (timer != null) timer.stop();

    if (win) {
        play(SOUND_WIN);
    } else {
        play(SOUND_FAIL);
    }

    showEndPopup(win);
}



public static int getTimeLeft() {
    return timeLeft;
}
private static boolean hasAnyPossibleMove() {
    for (int r = 0; r < ROWS; r++) {
        for (int c = 0; c < COLS; c++) {

            if (c + 1 < COLS) {
                swapTemp(r, c, r, c + 1);
                if (hasMatch()) { swapTemp(r, c, r, c + 1); return true; }
                swapTemp(r, c, r, c + 1);
            }

            if (r + 1 < ROWS) {
                swapTemp(r, c, r + 1, c);
                if (hasMatch()) { swapTemp(r, c, r + 1, c); return true; }
                swapTemp(r, c, r + 1, c);
            }
        }
    }
    return false;
}

private static boolean hasMatch() {
    boolean[][] m = CandyLogic.detectMatches(board);
    for (int r = 0; r < ROWS; r++)
        for (int c = 0; c < COLS; c++)
            if (m[r][c]) return true;
    return false;
}

private static void swapTemp(int r1, int c1, int r2, int c2) {
    int t = board[r1][c1];
    board[r1][c1] = board[r2][c2];
    board[r2][c2] = t;
}
private static void checkNoMovesGameOver() {
    if (gameOver) return;

   if (score >= TARGET_SCORE) {
    checkEndGame();
    return;
}

if (!CandyLogic.hasPossibleMove(board)) {
    gameOver = true;
    showEndPopup(false);
}

}


// level
private static void nextLevel() {

   // STOP au level 4
   if (level >= 4) {
    showEndPopup(true); // WIN FINAL
    return;
}
    
    level++;

    TARGET_SCORE = BASE_TARGET * level;
    COLS++;

    score = 0;
    moves = 20;
    comboMultiplier = 1;
    gameOver = false;

    timeLeft = 60;
    startTimer(60);

    board = new int[ROWS][COLS];
    gridCells = new StackPane[ROWS][COLS];

    GridPane newGrid = createGrid(ROWS, COLS, CELL_SIZE);

    BorderPane root = (BorderPane) rootStack.getChildren().get(0);
    root.setRight(newGrid);

    updateTargetLabel();
    updateScoreLabel();
    updateMovesLabel();
}



// private static void rebuildGridUI() {
//     GridPane grid = (GridPane) rootStack.lookup(".grid-pane");
//     grid.getChildren().clear();
//     drawGrid(board);
// }

// designe de game over  win level 4 
private static void showFinalWinPopup() {

    gameOver = true;

    if (timer != null) timer.stop();
      play(SOUND_WIN);

    StackPane overlay = new StackPane();
    overlay.setStyle("-fx-background-color: rgba(0,0,0,0.6);");

    VBox card = new VBox(20);
    card.setAlignment(Pos.CENTER);
    card.setPadding(new Insets(30));
    card.setPrefWidth(360);
    card.setMaxHeight(280);

    card.setStyle("""
        -fx-background-color: #ffe6f0;
        -fx-background-radius: 30;
        -fx-border-color: #ff4f9a;
        -fx-border-width: 4;
        -fx-border-radius: 30;
    """);

    Label title = new Label("🏆 CONGRATULATIONS 🏆");
    title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
    title.setTextFill(Color.web("#c2185b"));

    Label subtitle = new Label("You completed all levels!");
    subtitle.setFont(Font.font(18));

    Button restart = new Button("Play Again");
    restart.setStyle("""
        -fx-background-color: #ff4f9a;
        -fx-text-fill: white;
        -fx-font-size: 16;
        -fx-padding: 10 35;
        -fx-background-radius: 20;
    """);

    restart.setOnAction(e -> {
        level = 1;
        TARGET_SCORE = BASE_TARGET;
        COLS = 8; // valeur initiale
        resetGame();
        rootStack.getChildren().remove(overlay);
    });

    card.getChildren().addAll(title, subtitle, restart);
    overlay.getChildren().add(card);
    rootStack.getChildren().add(overlay);
}

// si il faild level 4 
private static void showFailedPopup() {
    gameOver = true;

    if (timer != null) timer.stop();

    StackPane overlay = new StackPane();
    overlay.setStyle("-fx-background-color: rgba(0,0,0,0.6);");

    VBox card = new VBox(20);
    card.setAlignment(Pos.CENTER);
    card.setPadding(new Insets(30));
    card.setPrefWidth(340);

    card.setStyle("""
        -fx-background-color: #ffe6f0;
        -fx-background-radius: 30;
        -fx-border-color: #ff4f9a;
        -fx-border-width: 4;
        -fx-border-radius: 30;
    """);

    Label title = new Label("You Failed");
    title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
    title.setTextFill(Color.web("#c2185b"));

    Label subtitle = new Label("Try again this level");

    Button retry = new Button("Retry");
    retry.setStyle("""
        -fx-background-color: #ff4f9a;
        -fx-text-fill: white;
        -fx-font-size: 16;
        -fx-padding: 10 35;
        -fx-background-radius: 20;
    """);

    retry.setOnAction(e -> {
        resetGame();              // recommence le même level
        rootStack.getChildren().remove(overlay);
    });

    card.getChildren().addAll(title, subtitle, retry);
    overlay.getChildren().add(card);
    rootStack.getChildren().add(overlay);
}


private static void rebuildGridUI() {
    // Nettoyer l'affichage
    for (int r = 0; r < ROWS; r++) {
        for (int c = 0; c < COLS; c++) {
            if (gridCells[r][c] != null) {
                gridCells[r][c].getChildren().clear();
            }
        }
    }

    // Redessiner avec le nouveau board
    refreshView();
}

}


