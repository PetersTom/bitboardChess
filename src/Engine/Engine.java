package Engine;

import GUI.Gui;
import Players.AlphaBetaPlayer;
import Players.HumanPlayer;
import Players.Player;
import Players.RandomPlayer;
import state.Board;
import state.Constants;
import state.Move;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Engine implements Runnable {

    private JFrame frame;
    private Gui gui;

    private Board board;

    private Thread t;
    private volatile boolean hasToStop = false;

    private Player whitePlayer;
    private Player blackPlayer;

    public Engine() {

        this.board = new Board();

        frame = new JFrame("Chess");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gui = new Gui();
        gui.setDoubleBuffered(true);
        gui.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (board.isWhiteToMove()) {
                    whitePlayer.mousePressed(e);
                } else {
                    blackPlayer.mousePressed(e);
                }
            }
        });

        // Initialize players after frame, board and gui, to prevent nullpointers
        whitePlayer = new HumanPlayer(Constants.WHITE, this);
        blackPlayer = new AlphaBetaPlayer(Constants.BLACK, this, 5000);

        frame.setContentPane(gui);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);

        initializeGame();
    }

    private void initializeGame() {
        gui.requestBoardRepaint(board.toString());//to start with a painted board.
        start();
    }

    /**
     * Start the game loop
     */
    private void start() {
        t = new Thread(this);
        t.start();
    }

    /**
     * Stop the game loop
     */
    public void stop() {
        hasToStop = true;
        synchronized (Constants.lock) {
            Constants.lock.notify();
        }
        this.join();
    }

    /**
     * The game loop
     */
    private Thread playerThread; //The thread that executes the players
    private boolean playerThreadRunning = false;
    @Override
    public void run() {
        while(true) {
            if (hasToStop) break; //It should stop when it needs to.
            //do a repaint
            gui.repaint();
            if (!playerThreadRunning) { //check if the player thread is already running
                if (board.isWhiteToMove()) {
                    playerThread = new Thread(whitePlayer); //if not, make a new thread and start it
                } else {
                    playerThread = new Thread(blackPlayer);
                }
                playerThread.start();
                playerThreadRunning = true;
            }

            Move m;
            if (board.isWhiteToMove()) {
                m = whitePlayer.fetchMove(); //try to fetch the move white makes
            } else {
                m = blackPlayer.fetchMove();
            }
            if (m != null) { //if there is a move
                board.makeMove(m); //execute it
                gui.requestBoardRepaint(board.toString()); // request a repaint.

                try { //try joining the player thread, as it has executed his job
                    playerThread.join();
                    playerThreadRunning = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                if (board.isCheckMate()) {
                    String color = board.isWhiteToMove() ? "black" : "white"; // the opposite color won
                    JOptionPane.showMessageDialog(getFrame(), color + " won");
                    break; // stop the game
                } else if (board.isStaleMate()) {
                    JOptionPane.showMessageDialog(getFrame(), "Stalemate");
                    break; // stop the game
                } else if (board.isPotentialDraw()) {
                    JOptionPane.showMessageDialog(getFrame(), "Draw");
                    break;
                }
            } else { // if there was not yet a move, let the thread wait untill there is
                synchronized (Constants.lock) {
                    try {
                        Constants.lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt(); // interrupt the current thread
                    }
                }
            }
        }
    }

    public void join() {
        try {
            t.join();
            if (playerThread != null) {
                playerThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("everything joined");
    }

    public Board getBoard() {
        return this.board;
    }

    public Gui getGui() {
        return this.gui;
    }

    public Frame getFrame() {
        return this.frame;
    }
}
