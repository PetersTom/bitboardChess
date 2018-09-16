package Players;

import Engine.Engine;
import state.Constants;
import state.Move;

public class AlphaBetaPlayer extends Player {

    private long maxTimeToRun = 5000L;
    private long startTime;
    private long endTime;

    private int startDepth = 2;
    int currentDepth;
    boolean inevetableMate = false;

    public AlphaBetaPlayer(int color, Engine e, long maxTimeToRun) {
        super(color, e);
        this.maxTimeToRun = maxTimeToRun;
    }


    @Override
    public void run() {
        inevetableMate = false;
        currentDepth = startDepth;
        startTime = System.currentTimeMillis();
        endTime = startTime + maxTimeToRun;
        Node startNode = new Node(board);
        Move bestMove = null;
        try {
            while(true) {
                int value = alphaBeta(startNode, currentDepth);
                bestMove = startNode.getBestMove();
                System.out.println("Depth: " + currentDepth + " bestMove: " + bestMove + " with value: " + value + " runningtime: " + (System.currentTimeMillis() - startTime));
                currentDepth++;
                if (inevetableMate) break; // already a mate found, so no use in going on
            }
        } catch (TimeOutException e) {
            // out of time
            if (bestMove == null) { // no move found yet
                System.err.println("Random move");
                bestMove = getRandomValidMove(board);
            }
        }
        this.move = bestMove;
        synchronized (Constants.lock) {
            Constants.lock.notify();
        }
        System.out.println("Playing " + bestMove); // this.move can already be read by the main thread
    }

    /**
     * The initial call for the alphabeta algorithm
     * @param n the initial node
     * @param depth the search depth
     * @return the value of the best move
     * @throws TimeOutException whenever it searches for too long
     */
    private int alphaBeta(Node n, int depth) throws TimeOutException {
        boolean isMaximizing = (color == Constants.WHITE);
        return alphaBeta(n, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, isMaximizing);
    }

    /**
     * An implementation of the alpha beta pruning algorithm.
     * @param n the node that is currently evaluated
     * @param depth the search depth that it is currently at (counting down the deeper it goes)
     * @param alpha the current alpha
     * @param beta the current beta
     * @param maximizingPlayer whether or not this is the maximizing player that is being examined
     * @return the value of the best move that can be reached
     * @throws TimeOutException whenever it is searching for too long
     */
    private int alphaBeta(Node n, int depth, int alpha, int beta, boolean maximizingPlayer) throws TimeOutException {
        if (System.currentTimeMillis() > endTime) {
            throw new TimeOutException();
        }
        if (depth < 0 || n.isLeave()) {
            return evaluate(n);
        }
        if (maximizingPlayer) {
            int value = Integer.MIN_VALUE;
            for (Node child : n) {
                value = Math.max(value, alphaBeta(child, depth - 1, alpha, beta, false));
                if (value > alpha) {
                    alpha = value;
                    if (depth == currentDepth)
                        n.setBestMove(child.getLastMove());
                }
                if (alpha >= beta) {
                    if (beta == Integer.MAX_VALUE) inevetableMate = true;
                    return beta;
                }
            }
            return alpha;
        } else {
            int value = Integer.MAX_VALUE;
            for (Node child : n) {
                value = Math.min(value, alphaBeta(child, depth - 1, alpha, beta, true));
                if (value < beta) {
                    beta = value;
                    if (depth == currentDepth)
                        n.setBestMove(child.getLastMove());
                }
                if (alpha >= beta) {
                    if (alpha == Integer.MIN_VALUE) inevetableMate = true;
                    return alpha;
                }
            }
            return beta;
        }
    }

    private int evaluate(Node n) {
        if (n.whiteMated()) {
            return Integer.MIN_VALUE;
        } else if (n.blackMated()) {
            return Integer.MAX_VALUE;
        } else if (n.staleMate() || n.isPotentialDraw()) {
            if (n.isWhiteToMove()) {
                return Integer.MIN_VALUE + 1; // draw only slightly better than losing.
            } else {
                return Integer.MAX_VALUE - 1;
            }
        }
        return n.getPiecesValue();
    }
}
