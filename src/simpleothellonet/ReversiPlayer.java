package simpleothellonet;

/**
 * Interface for all Reversi player classes.
 */
public interface ReversiPlayer {

    /**
     * Plays a turn on the given board.
     *
     * @param board The board at the start of the turn of this player.
     * @return The new board after the player's move, or null if the game should
     * be over.
     */
    public ReversiBoard playTurn(ReversiBoard board);
    
    
    public void onGameOver(ReversiBoard board, ReversiBoard.Color ourColor);
}
