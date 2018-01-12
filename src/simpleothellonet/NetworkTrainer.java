package simpleothellonet;

import simpleothellonet.ReversiBoard.Color;

/**
 * Testing code for the Reversi game. It trains two neural network players using
 * the experimental setup described in the report.
 */
public class NetworkTrainer {

    private final int numberEpoch;
    private final int learningGamesPerEpoch;
    private final int testingGamesPerEpoch;
    private final ReversiPlayer validationOpponent;
    private final NeuralNetworkPlayer network1;
    private final NeuralNetworkPlayer network2;

    public NetworkTrainer(
            int numberEpoch,
            int learningGamesPerEpoch,
            int testingGamesPerEpoch,
            ReversiPlayer validationOpponent,
            NeuralNetworkPlayer network1,
            NeuralNetworkPlayer network2) {
        this.numberEpoch = numberEpoch;
        this.learningGamesPerEpoch = learningGamesPerEpoch;
        this.testingGamesPerEpoch = testingGamesPerEpoch;
        this.validationOpponent = validationOpponent;
        this.network1 = network1;
        this.network2 = network2;
    }

    /**
     * Trains two neural network players, and prints the result of the learning.
     */
    public void train() {
        network1.startLearningSession(numberEpoch * learningGamesPerEpoch);
        network2.startLearningSession(numberEpoch * learningGamesPerEpoch);
        network2.setLearnFromGame(true);

        long averageEpochDuration = 0;
        long epochStart = System.currentTimeMillis();
        
        for (int epoch = 0; epoch < numberEpoch; epoch++) {
            network1.setLearnFromGame(true);
            double adversarialRate = runGames(network1, network2, learningGamesPerEpoch);
            network1.setLearnFromGame(false); // Disable learning for the sake of testing
            double testingRate = runGames(network1, validationOpponent, testingGamesPerEpoch);

            System.out.println(adversarialRate + " " + testingRate);

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
            double winningRate0 = runGames(network1, opponent, 100);
            double winningRate1 = runGames(network2, opponent, 100);
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
    private double runGames(ReversiPlayer firstPlayer, ReversiPlayer secondPlayer, int iterations) {
        double winCount = 0;
        ReversiPlayer[] players = {firstPlayer, secondPlayer};

        for (int count = 0; count < iterations; count++) {
            ReversiBoard lastBoard = ReversiGame.playGame(players[count % 2], players[(count + 1) % 2]);
            
            if ((lastBoard.getWinner() == Color.Black) == (count % 2 == 0)) {
                winCount += 1;
            }
        }
        return winCount / iterations;
    }
}
