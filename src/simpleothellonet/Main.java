package simpleothellonet;

import java.util.Arrays;
import java.util.List;
import org.neuroph.util.TransferFunctionType;

public class Main {

    /**
     * Main function, where we test the algorithm implementations.
     *
     * @param args Not used.
     */
    public static void main(String[] args) {
        // Training and neural network parameters
        
        // Number of times the phase training & test will be repeated
        int numberEpoch = 300;
        // Number of training games at each phase
        int learningGamesPerEpoch = 150;
        // Number of testing game at each phase (need high number for good average)
        int testingGamesPerEpoch = 50;
        // The transfer function in inner layers of the nnet
        TransferFunctionType transferFunction = TransferFunctionType.TANH;
        // The type of board encoding. Valid values are 1, 2 and 3.
        int inputPerCell = 1;
        // The number of hidden neurons. Add numbers for more hidden layers
        List<Integer> hiddenLayerSizes = Arrays.asList(50);
        // The learning rate. 0.02 has been empirically found to be good
        double learningRate = 0.02;
        // The neural networks will be saved to these files
        String networkFilename1 = "othello_1.nnet";
        String networkFilename2 = "othello_2.nnet";
        // The opponent player used to assess performance of the nnets
        ReversiPlayer validationOpponent = new MinimaxPlayer(3, MinimaxPlayer::binkleyHeuristic);
        
        // We now create the networks
        NeuralNetworkPlayer network1 = new NeuralNetworkPlayer(transferFunction, inputPerCell, hiddenLayerSizes, learningRate);
        NeuralNetworkPlayer network2 = new NeuralNetworkPlayer(transferFunction, inputPerCell, hiddenLayerSizes, learningRate);
       
        // If the networks are already trained and saved to a file, reload them with
        //NeuralNetworkPlayer network1 = new NeuralNetworkPlayer(networkFilename1);
        //NeuralNetworkPlayer network2 = new NeuralNetworkPlayer(networkFilename1);
        
        NetworkTrainer trainer = new NetworkTrainer(
                numberEpoch,
                learningGamesPerEpoch,
                testingGamesPerEpoch,
                validationOpponent,
                network1,
                network2);
        
        System.out.println("Start training...");
        trainer.train();
        
        // Save them to a file for later reuse
        network1.saveNetworkToFile(networkFilename1);
        network2.saveNetworkToFile(networkFilename2);
        
        // Test the performance against different opponents
        //trainer.test();
        
        // Play against the user
        //ReversiGame.playGame(network1, new UserPlayer());
        
    }
}
