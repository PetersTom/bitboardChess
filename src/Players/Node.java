package Players;

import state.Board;
import state.Move;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A node that uses the same board when iterating over its children
 */
public class Node implements Iterable<Node> {
    private Board b;
    private Move bestMove;

    public Node(Board b) {
        this.b = b;
    }

    /**
     * Iterate over the child nodes
     * @return
     */
    @Override
    public Iterator<Node> iterator() {
        return new Iterator<Node>() {
            List<Move> possibleMoves = new ArrayList<>(b.getMoves());

            @Override
            public boolean hasNext() {
                return possibleMoves.size() != 0;
            }

            @Override
            public Node next() {
                Move nextMove = possibleMoves.remove(0);
                b.makeMove(nextMove);
                Node toReturn = new Node(b.copy());
                b.unMakeMove(nextMove);
                return toReturn;
            }
        };
    }

    /**
     * Finds out if this node is a leave
     * @return If this node is a leave
     */
    public boolean isLeave() {
        return b.getMoves().isEmpty() || b.isPotentialDraw();
    }

    /**
     * Gets the last move that was played and that reached this node.
     * @return the last move played
     */
    public Move getLastMove() {
        return b.getLastMove();
    }

    public int getPiecesValue() {
        int value = 0;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                value += b.getPieceValue(x, y);
            }
        }
        return value;
    }

    public Move getBestMove() {
        return this.bestMove;
    }

    public void setBestMove(Move b) {
        this.bestMove = b;
    }

    public boolean whiteMated() {
        return b.isCheckMate() && b.isWhiteToMove();
    }

    public boolean blackMated() {
        return b.isCheckMate() && !b.isWhiteToMove();
    }

    public boolean staleMate() {
        return b.isStaleMate();
    }

    public boolean isPotentialDraw() {
        return b.isPotentialDraw();
    }

    public boolean isWhiteToMove() {
        return b.isWhiteToMove();
    }
}
