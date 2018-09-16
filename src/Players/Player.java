package Players;

import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import Engine.*;
import state.Board;
import state.Move;

/**
 * A player, subclass of Runnable to let the gui not hang when calculating a move.
 */
public abstract class Player implements Runnable {

    protected int color; // one of Constants.WHITE/Constants.BLACK (0)/(1)
    protected Move move;
    protected Engine e;
    protected Board board;

    public Player(int color, Engine e) {
        this.color = color;
        this.e = e;
        this.board = e.getBoard();
    }

    public int getColor() {
        return this.color;
    }

    /**
     * Used to fetch a move. When the move is fetched, the move is deleted in the process.
     * @return
     */
    public Move fetchMove() {
        if (move == null) {
            return move;
        } else {
            Move answer = move; // reset move to null
            move = null;
            return answer;
        }
    }

    /**
     * Returns true if there is a move to fetch, false otherwise.
     */
    public boolean isMove() {
        return move != null;
    }

    /**
     * To be overriden when a specific player type needs to check the mouse
     * @param e
     */
    public void mousePressed(MouseEvent e) {}

    /**
     * Returns a random valid move given the current situation in the board
     * @return A random move
     */
    protected Move getRandomValidMove(Board board) {
        List<Move> possibleMoves = board.getMoves();
        Collections.shuffle(possibleMoves);
        return possibleMoves.isEmpty() ? null : possibleMoves.get(0);
    }
}
