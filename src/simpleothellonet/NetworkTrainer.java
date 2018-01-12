package simpleothellonet;

import java.util.List;
import org.neuroph.util.TransferFunctionType;
import simpleothellonet.ReversiBoard.Color;

/**
 * Testing code for the Reversi game. It trains two neural network players using
 * the experimental setup described in the report.
 */
public class NetworkTrainer {

    private final int numberEpoch;
    private final int learningGamesPerEpoch;
    private final int testingGamesPerEpoch;
    private final TransferFunctionType transferFunction;
    private final int inputPerCell;
    private final List<Integer> hiddenLayerSizes;
    private final double learningRate;
    private final String networkFilename;
    private final NeuralNetworkPlayer[] networks;
    private final ReversiPlayer validationOpponent;

    public NetworkTrainer(
            int numberEpoch,
            int learningGamesPerEpoch,
            int testingGamesPerEpoch,
            TransferFunctionType transferFunction,
            int inputPerCell,
            List<Integer> hiddenLayerSizes,
            double learningRate,
            String networkFilename,
            ReversiPlayer validationOpponent) {
        this.numberEpoch = numberEpoch;
        this.learningGamesPerEpoch = learningGamesPerEpoch;
        this.testingGamesPerEpoch = testingGamesPerEpoch;
        this.transferFunction = transferFunction;
        this.inputPerCell = inputPerCell;
        this.hiddenLayerSizes = hiddenLayerSizes;
        this.learningRate = learningRate;
        this.networkFilename = networkFilename;
        this.validationOpponent = validationOpponent;

        networks = new NeuralNetworkPlayer[]{
            new NeuralNetworkPlayer(transferFunction, inputPerCell, hiddenLayerSizes, learningRate),
            new NeuralNetworkPlayer(transferFunction, inputPerCell, hiddenLayerSizes, learningRate)
        };
    }

    /**
     * Trains two neural network players, and prints the result of the learning.
     */
    public void train() {

        for (NeuralNetworkPlayer net : networks) {
            net.startLearningSession(numberEpoch * learningGamesPerEpoch);
        }

        long averageEpochDuration = 0;
        long epochStart = System.currentTimeMillis();
        for (int epoch = 0; epoch < numberEpoch; epoch++) {
            double adversarialRate = runGames(networks[0], networks[1], learningGamesPerEpoch, true);
            double testingRate = runGames(networks[0], validationOpponent, testingGamesPerEpoch, false);

            System.out.println(adversarialRate + " " + testingRate);

            for (int i = 0; i < networks.length; ++i) {
                networks[i].saveNetworkToFile(String.format(networkFilename, i));
            }

            // Estimate remaining time until end of learning session
            long epochDuration = System.currentTimeMillis() - epochStart;
            epochStart = System.currentTimeMillis();
            averageEpochDuration = ((epoch * averageEpochDuration) + epochDuration) / (epoch + 1);
            long remainingTime = ((numberEpoch - epoch) * averageEpochDuration) / 1000;
            System.out.println("% Estimated remaining time: "
                    + String.format("%d hours, %02d minutes",
                            remainingTime / 3600, (remainingTime % 3600) / 60));
        }
    }

    public void test() {
        for (int plyDepth = 1; plyDepth < 7; plyDepth++) {
            ReversiPlayer opponent = new MinimaxPlayer(plyDepth, MinimaxPlayer::binkleyHeuristic);
            double winningRate0 = runGames(networks[0], opponent, 100, false);
            double winningRate1 = runGames(networks[1], opponent, 100, false);
            System.out.println(plyDepth + " " + winningRate0 + " " + winningRate1);
        }
    }

    /**
     * Runs a certain amount of games with the given players.
     *
     * @param firstPlayer The first player.
     * @param secondPlayer Another player.
     * @param iterations The number of games that should be played.
     * @param learnFromTheseGames true if the players should learn from this game,
     * if they are instances of NeuralNetworkPlayer.
     * @return The winning rate of the first player.
     */
    private double runGames(ReversiPlayer firstPlayer,
            ReversiPlayer secondPlayer, int iterations, boolean learnFromTheseGames) {
        double winCount = 0;
        // We alternate the color of the players, so we must keep track of them
        ReversiPlayer[] players = {firstPlayer, secondPlayer};
        Color[] playerColors = {Color.Black, Color.White};

        for(ReversiPlayer player : players) {
            if(player instanceof NeuralNetworkPlayer) {
                ((NeuralNetworkPlayer) player).setLearnFromGame(learnFromTheseGames);
            }
        }

        for (int count = 0; count < iterations; count++) {
            ReversiBoard board = ReversiBoard.initialBoard();
            ReversiBoard lastBoard = board;
            // Determine which player plays first
            int activePlayer = playerColors[0] == Color.Black ? 0 : 1;
            board.swapTurn(); // Swap once so that the first turn is still black

            while (board != null) {
                board.swapTurn(); // Here we can swap safely, board is not null
                lastBoard = board;
                board = players[activePlayer].playTurn(board);
                activePlayer = 1 - activePlayer;
            }

            
            for(int i = 0; i < players.length; ++i) {
                players[i].onGameOver(lastBoard, playerColors[i]);
                if(players[i] instanceof NeuralNetworkPlayer) {
                    ((NeuralNetworkPlayer) players[i]).setLearnFromGame(learnFromTheseGames);
                }
                playerColors[i] = playerColors[i].getOpposite();
            }

            if (lastBoard.getWinner() == playerColors[0]) {
                winCount += 1;
            }
        }
        return winCount / iterations;
    }
}
