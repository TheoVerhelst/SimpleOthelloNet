package simpleothellonet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.IntUnaryOperator;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.TransferFunctionType;
import simpleothellonet.ReversiBoard.Color;

/**
 * Main class of this work, this is the implementation of a Reversi player that
 * learn using Temporal Difference Learning (TDL) and a multilayer perceptron.
 * It also uses a linearly decreasing eps-greedy move selection scheme.
 */
public class NeuralNetworkPlayer implements ReversiPlayer {

    /**
     * The number of inputs of the neural network per grid cell.
     */
    static final private int INPUT_PER_CELL = 2;

    static final private int NUMBER_CELLS = ReversiBoard.getGridSize() * ReversiBoard.getGridSize();

    static final private int INPUT_SIZE = NUMBER_CELLS * INPUT_PER_CELL;

    /**
     * The number of neurons in the hidden layer.
     */
    static final private int HIDDEN_SIZE = 50;

    /**
     * Filename to which to save the neural network when the learning is done.
     */
    private final String nnetFilename;

    /**
     * Instance of the Neuroph neural network
     */
    private final MultiLayerPerceptron neuralNetwork;

    /**
     * The learning rule object of the neural network. This is an implementation
     * detail, necessary to perform online learning.
     */
    private final BackPropagation learning = new BackPropagation();

    /**
     * Board state of the previous turn, translated to network input. We need
     * this for TD learning.
     */
    private double[] previousBoardInput = null;

    /**
     * True if learning should be performed in the current game.
     */
    private boolean learn = true;

    /**
     * A random number generator. Used for eps-greedy move selection.
     */
    private final Random random = new Random();

    /**
     * Initial value of epsilon.
     */
    private final double epsilon_0 = 0.1;

    /**
     * The number of games that will be performed in the current training
     * session. We need this to linearly decrease epsilon.
     */
    private final double numberOfLearningGames;

    /**
     * A game counter. We need this to linearly decrease epsilon.
     */
    private double learningGameCount = 0;

    /**
     * A list of functions that are used to generate symmetries of the board.
     */
    private List<IntUnaryOperator> symmetryMappings;

    /**
     * Constructor.
     *
     * @param nnetFilename The filename of the file to which save the nnet.
     * @param numberOfLearningGames The number of games that will be performed
     * in this learning session.
     * @param load true to load the nnet from the filename nnetFilename.
     */
    public NeuralNetworkPlayer(String nnetFilename, int numberOfLearningGames, boolean load) {
        this.nnetFilename = nnetFilename;
        this.numberOfLearningGames = numberOfLearningGames;
        if (load) {
            System.out.println("Loading nnet from " + nnetFilename);
            neuralNetwork = (MultiLayerPerceptron) MultiLayerPerceptron.createFromFile(nnetFilename);
        } else {
            neuralNetwork = new MultiLayerPerceptron(TransferFunctionType.TANH, INPUT_SIZE, HIDDEN_SIZE, 1);
            neuralNetwork.randomizeWeights();
            neuralNetwork.setLearningRule(learning);
            learning.setBatchMode(false);
            learning.setLearningRate(0.02);
        }
        generateSymmetryMappings();
    }

    @Override
    public ReversiBoard playTurn(ReversiBoard board) {
        Color ourColor = board.getTurnColor();
        // Find the next board with the highest evaluation by the neural network
        ReversiBoard bestBoard = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        List<Node> children = board.getChildren();

        // With probability epsilon, select a random child rather than the best one
        if (random.nextDouble() <= getEpsilon() && !children.isEmpty() && learn) {
            bestBoard = (ReversiBoard) children.get(random.nextInt(children.size()));
            bestValue = evaluateBoard(bestBoard, ourColor);
        } else {
            // Find the best child board
            for (Node child : children) {
                double value = evaluateBoard((ReversiBoard) child, ourColor);
                if (value > bestValue) {
                    bestBoard = (ReversiBoard) child;
                    bestValue = value;
                }
            }
        }

        if (bestBoard != null) {
            if (learn) {
                if (previousBoardInput != null) {
                    // Learn on this prediction, from the previous board (see TD learning)
                    learnFromBoard(previousBoardInput, bestValue);
                }
                previousBoardInput = extractInput(bestBoard, ourColor);
            }
            bestBoard.swapTurn();
        }

        return bestBoard;
    }

    public void setLearn(boolean learn) {
        this.learn = learn;
    }

    /**
     * Perform tasks needed at the end of the game, such as learning from the
     * game outcome.
     *
     * @param board The final board.
     * @param ourColor The color of this player.
     */
    public void onGameOver(ReversiBoard board, Color ourColor) {
        double outcome;
        Color winner = board.getWinner();
        if (winner == ourColor) {
            outcome = 1;
        } else if (winner == ourColor.getOpposite()) {
            outcome = -1;
        } else {
            outcome = 0;
        }

        // Learn on this prediction, from the previous board (see TD learning)
        learnFromBoard(previousBoardInput, outcome);
        previousBoardInput = null;
        learningGameCount += 1;

    }

    public void saveNetworkToFile() {
        neuralNetwork.save(nnetFilename);
    }

    private void learnFromBoard(double[] boardInput, double outcome) {
        DataSet trainingSet = new DataSet(INPUT_SIZE, 1);
        // Learn from all symmetries of the board.
        for (double[] symmetry : generateSymmetries(boardInput)) {
            trainingSet.addRow(new DataSetRow(symmetry, new double[]{outcome}));
        }
        learning.doOneLearningIteration(trainingSet);
    }

    /**
     * Creates an array of double suitable to be fed to the neural network, from
     * a board state.
     *
     * @param board The board state.
     * @param ourColor The color of this player on this board.
     * @return An array of double of size INPUT_SIZE.
     */
    private double[] extractInput(ReversiBoard board, Color ourColor) {
        // Create input vector
        double[] input = new double[INPUT_SIZE];
        for (int i = 0; i < NUMBER_CELLS; ++i) {
            int row = i / ReversiBoard.getGridSize();
            int col = i % ReversiBoard.getGridSize();
            Color cellValue = board.getValue(col, row);
            input[i * INPUT_PER_CELL + 0] = (cellValue == ourColor ? 1 : -1);
            input[i * INPUT_PER_CELL + 1] = (cellValue == ourColor.getOpposite() ? 1 : -1);
        }
        return input;
    }

    private double evaluateBoard(ReversiBoard board, Color ourColor) {
        // Predict a value with the neural network
        neuralNetwork.setInput(extractInput(board, ourColor));
        neuralNetwork.calculate();
        double[] output = neuralNetwork.getOutput();
        assert (output.length == 1);
        return output[0];
    }

    /**
     * Calculates the value of epsilon, it decreases as the number of games
     * counter approaches numberOfLearningGames.
     *
     * @return epsilon
     */
    private double getEpsilon() {
        return epsilon_0 * (1 - (learningGameCount / numberOfLearningGames));
    }

    /**
     * Calculates all 8 symmetries of the given board.
     *
     * @param boardInput The board to reflect along the symmetry axes.
     * @return A list of reflected boards.
     */
    private List<double[]> generateSymmetries(double[] boardInput) {
        List<double[]> result = new ArrayList<>(symmetryMappings.size() + 1);
        result.add(boardInput);
        for (IntUnaryOperator mapping : symmetryMappings) {
            double[] symmetry = new double[INPUT_SIZE];
            for (int i = 0; i < NUMBER_CELLS; ++i) {
                int inputIdx = i * INPUT_PER_CELL;
                int mappedIdx = mapping.applyAsInt(i) * INPUT_PER_CELL;
                for (int j = 0; j < INPUT_PER_CELL; ++j) {
                    symmetry[inputIdx + j] = boardInput[mappedIdx + j];
                }
            }
            result.add(symmetry);
        }
        return result;
    }

    /**
     * Generates all symmetry mapping functions, and put them in
     * symmetryMappings.
     */
    private void generateSymmetryMappings() {
        int gridSize = ReversiBoard.getGridSize();
        // Horizontal flip
        IntUnaryOperator hFlip = idx -> {
            int row = idx / gridSize;
            int col = idx % gridSize;
            return row * ReversiBoard.getGridSize() + (gridSize - col - 1);
        };
        // Horizontal flip
        IntUnaryOperator vFlip = idx -> {
            int row = idx / gridSize;
            int col = idx % gridSize;
            return (gridSize - row - 1) * ReversiBoard.getGridSize() + col;
        };
        // Flip along the diagonal going from top-left to bottom-right
        IntUnaryOperator dFlip = idx -> {
            int row = idx / gridSize;
            int col = idx % gridSize;
            return col * ReversiBoard.getGridSize() + row;
        };
        // Note that we do not include the identity, in order to save unnecessary
        // calculations when it comes to compute all symmetries.
        symmetryMappings = Arrays.asList(hFlip, vFlip, dFlip, hFlip.compose(vFlip),
                dFlip.compose(vFlip), hFlip.compose(dFlip), hFlip.compose(vFlip).compose(dFlip));
    }
}
