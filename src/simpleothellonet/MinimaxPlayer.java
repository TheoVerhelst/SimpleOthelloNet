package simpleothellonet;

import java.util.List;
import java.util.Random;
import simpleothellonet.ReversiBoard.Color;

/**
 * Implementation of ReversiPlayer, using minimax search and the heuristic
 * function given by Kevin J. Binkley.
 */
public class MinimaxPlayer implements ReversiPlayer {

    /**
     * The depth to which the minimax algorith should explore the game tree.
     */
    private final int plyDepth;

    private final Minimax minimax = new Minimax();

    /**
     * Random number generator. It is used in the heuristic function for noise.
     */
    private static final Random RANDOM = new Random();

    public MinimaxPlayer(int plyDepth) {
        this.plyDepth = plyDepth;
    }

    @Override
    public ReversiBoard playTurn(ReversiBoard board) {
        ReversiBoard result;
        List<Node> children = board.getChildren();
        Minimax.Result minimaxMove = minimax.alphaBeta(board, plyDepth,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true,
                MinimaxPlayer::binkleyHeuristic);
        result = (ReversiBoard) minimaxMove.node;
        return result;
    }

    /**
     * Naive heuristic, which just counts the number player's pieces, and
     * substracts the number of opponent's pieces.
     *
     * @param node The game board to evaluate
     * @return The heuristic value.
     */
    static public double simpleHeuristic(Node node) {
        ReversiBoard board = (ReversiBoard) node;
        int gridSize = board.getGridSize();
        Color turnColor = board.getTurnColor();
        double count = 0;
        for (int row = 0; row < gridSize; ++row) {
            for (int col = 0; col < gridSize; ++col) {
                if (board.getValue(col, row) == turnColor) {
                    count += 1;
                } else if (board.getValue(col, row) == turnColor.getOpposite()) {
                    count -= 1;
                }
            }
        }
        return count;
    }

    /**
     * Heuristic proposed by Kevin J. Binkley, which is a weighted versions of
     * the naive heuristic above, accounting for the board strategic positions.
     * A gaussian noise is also added.
     *
     * @param node The game board to evaluate
     * @return The heuristic value.
     */
    static public double binkleyHeuristic(Node node) {
        ReversiBoard board = (ReversiBoard) node;
        int gridSize = board.getGridSize();
        Color turnColor = board.getTurnColor();
        double[][] weights = {
            {100, -25, 10, 5, 5, 10, -25, 100},
            {-25, -25, 2, 2, 2, 2, -25, -25},
            {10, 2, 5, 1, 1, 5, 2, 10},
            {5, 2, 1, 2, 2, 1, 2, 5},
            {5, 2, 1, 2, 2, 1, 2, 5},
            {10, 2, 5, 1, 1, 5, 2, 10},
            {-25, -25, 2, 2, 2, 2, -25, -25},
            {100, -25, 10, 5, 5, 10, -25, 100}
        };
        double noiseStd = 10;
        double result = 0;
        for (int row = 0; row < gridSize; ++row) {
            for (int col = 0; col < gridSize; ++col) {
                if (board.getValue(col, row) == turnColor) {
                    result += weights[row][col];
                } else if (board.getValue(col, row) == turnColor.getOpposite()) {
                    result -= weights[row][col];
                }
            }
        }
        return result + RANDOM.nextGaussian() * noiseStd;
    }

    @Override
    public void onGameOver(ReversiBoard board, Color ourColor) {
    }

}
