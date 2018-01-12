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
        int numberEpoch = 300;
        int learningGamesPerEpoch = 150;
        int testingGamesPerEpoch = 50;
        TransferFunctionType transferFunction = TransferFunctionType.TANH;
        int inputPerCell = 1;
        List<Integer> hiddenLayerSizes = Arrays.asList(50);
        double learningRate = 0.02;
        String networkFilename = "othello_%d.nnet";
        NetworkTrainer trainer = new NetworkTrainer(numberEpoch,
                learningGamesPerEpoch, testingGamesPerEpoch, transferFunction,
                inputPerCell, hiddenLayerSizes, learningRate, networkFilename);
        
        trainer.train();
        trainer.test();
    }
}
