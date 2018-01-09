package simpleothellonet;

import simpleothellonet.ReversiBoard.Color;

/**
 * Testing code for the Reversi game. It trains two neural network players using
 * the experimental setup described in the report.
 */
public class NetworkTrainer {

    /**
     * Creates and train two neural network players, and prints the result of
     * the learning.
     */
    public static void train() {
        int numberEpoch = 300;
        int learningGamesPerEpoch = 150;
        int testingGamesPerEpoch = 50;

        NeuralNetworkPlayer network = new NeuralNetworkPlayer("reversi_0_02.nnet", numberEpoch * learningGamesPerEpoch, false);
        NeuralNetworkPlayer otherNetwork = new NeuralNetworkPlayer("reversi_opponent_0_02.nnet", numberEpoch * learningGamesPerEpoch, false);

        // The output is intended to be used as matlab code
        System.out.println("learning = [];");
        System.out.println("testing = [];");

        long averageEpochDuration = 0;
        long epochStart = System.currentTimeMillis();
        for (int epoch = 0; epoch < numberEpoch; epoch++) {
            System.out.println("learning(end+1) = " + runGames(network, otherNetwork, learningGamesPerEpoch, true) + ";");
            System.out.println("testing(end+1) = " + runGames(network, new MinimaxPlayer(3), testingGamesPerEpoch, false) + ";");
            network.saveNetworkToFile();
            otherNetwork.saveNetworkToFile();

            // Estimate remaining time until end of learning
            long epochDuration = System.currentTimeMillis() - epochStart;
            epochStart = System.currentTimeMillis();
            averageEpochDuration = ((epoch * averageEpochDuration) + epochDuration) / (epoch + 1);
            long remainingTime = ((numberEpoch - epoch) * averageEpochDuration) / 1000;
            System.out.println("% Estimated remaining time: "
                    + String.format("%d hours, %02d minutes",
                            remainingTime / 3600, (remainingTime % 3600) / 60));
        }
    }

    public static void test() {
        NeuralNetworkPlayer network = new NeuralNetworkPlayer("reversi_0_001.nnet", 0, true);
        NeuralNetworkPlayer otherNetwork = new NeuralNetworkPlayer("reversi_opponent_0_001.nnet", 0, true);
        for (int ply = 1; ply < 7; ply++) {
            System.out.println(ply + " " + runGames(network, new MinimaxPlayer(ply), 100, false) + " " + runGames(otherNetwork, new MinimaxPlayer(ply), 100, false));
        }
    }

    /**
     * Runs a certain amount of games with the given players.
     *
     * @param firstPlayer The first player, must be a nnet player.
     * @param secondPlayer Another player, may be any kind of Reversi player.
     * @param iterations The number of games that should be played.
     * @param learn true if the first player (and the second, if it is also a
     * nnet) should learn from this game.
     * @return The winning rate of the first player.
     */
    private static double runGames(NeuralNetworkPlayer firstPlayer, ReversiPlayer secondPlayer, int iterations, boolean learn) {
        double winCount = 0;
        ReversiPlayer[] players = {firstPlayer, secondPlayer};
        firstPlayer.setLearn(learn);

        for (int count = 0; count < iterations; count++) {
            ReversiBoard board = ReversiBoard.initialBoard();
            ReversiBoard lastBoard = board;
            int activePlayer = 0;

            while (board != null) {
                lastBoard = board;
                board = players[activePlayer].playTurn(board);
                activePlayer = 1 - activePlayer;
            }

            if (learn) {
                firstPlayer.onGameOver(lastBoard, Color.Black);
                if (secondPlayer instanceof NeuralNetworkPlayer) {
                    ((NeuralNetworkPlayer) secondPlayer).onGameOver(lastBoard, Color.White);
                }
            }
            if (lastBoard.getWinner() == Color.Black) {
                winCount += 1;
            }
        }
        return winCount / iterations;
    }
}
