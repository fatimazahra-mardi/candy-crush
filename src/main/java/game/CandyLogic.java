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
public static boolean hasPossibleMove(int[][] board) {
    int rows = board.length;
    int cols = board[0].length;

    for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {

            if (c + 1 < cols) {
                swap(board, r, c, r, c + 1);
                if (hasMatch(board)) {
                    swap(board, r, c, r, c + 1);
                    return true;
                }
                swap(board, r, c, r, c + 1);
            }

            if (r + 1 < rows) {
                swap(board, r, c, r + 1, c);
                if (hasMatch(board)) {
                    swap(board, r, c, r + 1, c);
                    return true;
                }
                swap(board, r, c, r + 1, c);
            }
        }
    }
    return false;
}

private static boolean hasMatch(int[][] board) {
    boolean[][] m = detectMatches(board);
    for (boolean[] row : m)
        for (boolean b : row)
            if (b) return true;
    return false;
}

private static void swap(int[][] b, int r1, int c1, int r2, int c2) {
    int t = b[r1][c1];
    b[r1][c1] = b[r2][c2];
    b[r2][c2] = t;
}






}
