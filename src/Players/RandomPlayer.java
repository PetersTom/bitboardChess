package Players;

import Engine.Engine;
import GUI.Gui;
import state.Constants;
import state.Move;

import java.util.List;

public class RandomPlayer extends Player {

    private int waitTime;

    public RandomPlayer(int color, Engine e, int waitTime) {
        super(color, e);
        this.waitTime = waitTime;
    }

    /**
     * Just wait until the player clicks on a square that makes a move.
     */
    @Override
    public void run() {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        this.move = getRandomValidMove(this.board);
        synchronized (Constants.lock) { // synchronized on the engine
            Constants.lock.notify();
        }
    }
}
