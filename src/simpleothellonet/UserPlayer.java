package simpleothellonet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * Reversi player that asks the user on stdin for the move choice, and displays
 * the board as well.
 */
public class UserPlayer implements ReversiPlayer {

    /**
     * All strings that may be given in order to leave the game.
     */
    private final List<String> quitInputs = Arrays.asList("quit", "q", "exit");

    /**
     * Input reader object.
     */
    private final BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Plays a turn by showing the board to stdout and asking input to stdin.
     *
     * @param board The current board
     * @return The new board after the player's move, or null if the game is
     * over.
     */
    @Override
    public ReversiBoard playTurn(ReversiBoard board) {
        ReversiBoard result = board;
        if (result.getChildren().isEmpty()) {
            return null;
        }
        boolean validMove = false;
        while (!validMove) {
            try {
                System.out.println("What is your move?");
                System.out.print("Col: ");
                String col = inputReader.readLine();
                System.out.print("Row: ");
                String row = inputReader.readLine();
                if (quitInputs.contains(col) || quitInputs.contains(row)) {
                    return null;
                }
                int colInt = Integer.parseInt(col);
                int rowInt = Integer.parseInt(row);
                validMove = result.placeToken(colInt, rowInt);
                if (!validMove) {
                    System.out.println("Invalid move :/");
                } else {
                    System.out.println("Succesful move!");
                }
            } catch (IOException | NumberFormatException ex) {
                System.out.println("Invalid input :/");
                // continue
            }
        }
        result.swapTurn();
        return result;
    }

}
