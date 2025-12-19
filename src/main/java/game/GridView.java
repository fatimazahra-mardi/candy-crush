package game;
import javafx.scene.media.AudioClip;
import java.net.URL;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Random;

public class GridView {

    private static StackPane[][] gridCells;
    private static int[][] board;

    private static StackPane firstSelected = null;

    private static int ROWS;
    private static int COLS;
    private static int CELL_SIZE;

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

    if (hasMatch) {
        // ✔️ Coup valide → afficher le swap
        refreshView();
        resolveWithAnimation();
    } else {
        // ❌ Coup invalide → annuler immédiatement
         play(SOUND_GAME_OVER);
        swap(firstSelected, cell);
        refreshView();
    }

    unselectCell(firstSelected);
    firstSelected = null;
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

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (matches[r][c]) {
                    found = true;
                    animateRemove(r, c);
                }
            }
        }

        if (!found) return;
        play(SOUND_MATCH);


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
}


