package simpleothellonet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the Reversi game and its rules. It represents a board
 * state, and gives methods helping to implement ReversiPlayer.
 */
public class ReversiBoard implements Node {

    /**
     * All possible color on the board. An empty cell is represented by null.
     */
    public enum Color {
        Black,
        White;

        public Color getOpposite() {
            return this == Black ? White : Black;
        }
    }

    /**
     * The length of a side of th board grid.
     */
    final static private int GRID_SIZE = 8;

    /**
     * Actual values in this instance of the game board.
     */
    final private Color[][] grid = new Color[GRID_SIZE][GRID_SIZE];

    /**
     * Old board leading to this state, the parent node.
     */
    final private ReversiBoard previousBoard;

    /**
     * Indicate whose player turn is.
     */
    private Color turnColor;

    /**
     * Returns the initial board according to classic rules of Reversi.
     *
     * @return The initial board.
     */
    public static ReversiBoard initialBoard() {
        return new ReversiBoard();
    }

    /**
     * Copy constructor.
     *
     * @param previousBoard The instance to copy.
     */
    public ReversiBoard(ReversiBoard previousBoard) {
        this.previousBoard = previousBoard;
        // Copy the content of the previous board
        for (int row = 0; row < GRID_SIZE; ++row) {
            System.arraycopy(previousBoard.grid[row], 0, grid[row], 0, GRID_SIZE);
        }
        this.turnColor = previousBoard.turnColor;
    }

    /**
     * Implementation of initialBoard, by the mean of a private constructor.
     */
    private ReversiBoard() {
        previousBoard = null;
        // Reversi game rule: black plays first
        turnColor = Color.Black;
        for (int row = 0; row < GRID_SIZE; ++row) {
            for (int col = 0; col < GRID_SIZE; ++col) {
                grid[row][col] = null;
            }
        }
        // Initial board state
        grid[3][3] = Color.White;
        grid[4][4] = Color.White;
        grid[3][4] = Color.Black;
        grid[4][3] = Color.Black;
    }

    @Override
    public String toString() {
        Map<Color, String> charMap = new HashMap<>();
        charMap.put(Color.Black, "\u2588\u2588");
        charMap.put(Color.White, "\u2591\u2591");
        charMap.put(null, "  ");
        String result = "0 1 2 3 4 5 6 7 \n";
        for (int row = 0; row < GRID_SIZE; ++row) {
            for (int col = 0; col < GRID_SIZE; ++col) {
                result += charMap.get(grid[row][col]);
            }
            result += row + "\n";
        }
        return result;
    }

    @Override
    public Node getParent() {
        return previousBoard;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> result = new ArrayList<>();

        // Iterate over the whole grid
        for (int row = 0; row < GRID_SIZE; ++row) {
            for (int col = 0; col < GRID_SIZE; ++col) {
                if (grid[row][col] == null) {
                    ReversiBoard newBoard = new ReversiBoard(this);
                    boolean validMove = newBoard.placeToken(col, row);
                    if (validMove) {
                        result.add(newBoard);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Performs an attempt to place a token on the board at the given position,
     * and if it is a valid move, proceeds to the token flip.
     *
     * @param col The col of the cell.
     * @param row The row of the cell.
     * @return True if this is a valid move, false otherwise.
     */
    public boolean placeToken(int col, int row) {
        int[] stepsCol = new int[]{1, 1, 0, -1, -1, -1, 0, 1};
        int[] stepsRow = new int[]{0, 1, 1, 1, 0, -1, -1, -1};
        int numberCheckingDirection = stepsCol.length;

        // If there is a cell from which start a line flip
        if (grid[row][col] != null) {
            return false;
        }

        boolean validMove = false;
        // Test if there is a possible line, in all eight directions
        for (int direction = 0; direction < numberCheckingDirection; ++direction) {
            int stepCol = stepsCol[direction];
            int stepRow = stepsRow[direction];
            int count = countOpponentLineLength(col, row, stepCol, stepRow);

            // If there is a line of opponent's cells suitable to
            // do a line flip
            if (count > 0) {
                validMove = true;
                // Flip the line
                flipLine(col, row, stepCol, stepRow, count);
                // Put a token on the cell
                grid[row][col] = turnColor;
            }
        }
        return validMove;
    }

    public void swapTurn() {
        turnColor = turnColor.getOpposite();
    }

    public Color getTurnColor() {
        return turnColor;
    }

    public static int getGridSize() {
        return GRID_SIZE;
    }

    public Color getValue(int col, int row) {
        return grid[row][col];
    }

    /**
     * Counts the tokens and return who is the winner.
     *
     * @return The color of the winner.
     */
    public Color getWinner() {
        Map<Color, Integer> counts = new HashMap<>();
        counts.put(Color.Black, 0);
        counts.put(Color.White, 0);
        counts.put(null, 0);
        for (int row = 0; row < GRID_SIZE; ++row) {
            for (int col = 0; col < GRID_SIZE; ++col) {
                counts.put(grid[row][col], counts.get(grid[row][col]) + 1);
            }
        }

        if (counts.get(Color.Black) > counts.get(Color.White)) {
            return Color.Black;
        } else if (counts.get(Color.Black) < counts.get(Color.White)) {
            return Color.White;
        } else {
            return null;
        }
    }

    /**
     * Counts how many opposite cells there is in a line starting from the given
     * position, and going in the specified direction. The starting position is
     * not included in the count, since there should be one of our token at this
     * place.
     *
     * @param col The column of the starting position.
     * @param row The row of the starting position.
     * @param stepCol X-increment representing the direction of the line.
     * @param stepRow Y-increment representing the direction of the line.
     * @return The number of opponent's token on the line, or 0 if there is no
     * such line.
     */
    private int countOpponentLineLength(int col, int row, int stepCol, int stepRow) {
        // Iterate over the cells in the given direction, and count how many
        // opponent cells there is.
        int checkedCol = col;
        int checkedRow = row;
        Color checkedCell;
        int opponentLineCount = -1;

        do {
            opponentLineCount += 1;
            checkedCol += stepCol;
            checkedRow += stepRow;
            if (validCoord(checkedCol, checkedRow)) {
                checkedCell = grid[checkedRow][checkedCol];
            } else {
                // The line ends by the edge of the grid
                return 0;
            }
        } while (checkedCell == turnColor.getOpposite());

        return checkedCell == turnColor ? opponentLineCount : 0;
    }

    /**
     * Flip the token on the given line. The arguments are similar to the ones
     * given to countOpponentLineLenth.
     *
     * @param col The column of the starting position.
     * @param row The row of the starting position.
     * @param stepCol X-increment representing the direction of the line.
     * @param stepRow Y-increment representing the direction of the line.
     * @param count The length of the line to flip.
     */
    private void flipLine(int col, int row, int stepCol, int stepRow, int count) {
        for (int i = 1; i <= count; ++i) {
            Color cell = grid[row + i * stepRow][col + i * stepCol];
            grid[row + i * stepRow][col + i * stepCol] = cell.getOpposite();
        }
    }

    static private boolean validCoord(int col, int row) {
        return col >= 0 && col < GRID_SIZE && row >= 0 && row < GRID_SIZE;
    }
}
