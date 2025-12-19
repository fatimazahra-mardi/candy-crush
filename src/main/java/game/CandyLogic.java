package game;

public class CandyLogic {

    // Détecte les matchs de 3 ou plus
    public static boolean[][] detectMatches(int[][] board) {

        int rows = board.length;
        int cols = board[0].length;

        boolean[][] matches = new boolean[rows][cols];

        // Horizontale
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c <= cols - 3; c++) {
                int v = board[r][c];
                if (v == -1) continue;

                if (board[r][c + 1] == v && board[r][c + 2] == v) {
                    matches[r][c] = true;
                    matches[r][c + 1] = true;
                    matches[r][c + 2] = true;
                }
            }
        }

        // Verticale
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r <= rows - 3; r++) {
                int v = board[r][c];
                if (v == -1) continue;

                if (board[r + 1][c] == v && board[r + 2][c] == v) {
                    matches[r][c] = true;
                    matches[r + 1][c] = true;
                    matches[r + 2][c] = true;
                }
            }
        }

        return matches;
    }
}
