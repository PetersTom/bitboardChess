package Players;

import Engine.Engine;
import GUI.Gui;
import state.Constants;
import state.Move;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HumanPlayer extends Player {

    private Gui gui;
    private int selectedPieceSquare = 0; //the position of the selectedSquare piece (1 indexed), 0 if nothing selected
    private List<Move> possibleMoves;

    public HumanPlayer(int color, Engine e) {
        super(color, e);
        this.gui = e.getGui();
    }

    /**
     * Just wait until the player clicks on a square that makes a move.
     */
    @Override
    public void run() {
        //do nothing. This method is only here because every player should have it, wait for a valid move to be clicked
        // do however fetch the possible moves here, so it will be done only once.
        this.possibleMoves = board.getMoves();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Move moveToSet = null;
        int clickedSquare = Gui.getSquare(e.getX(), e.getY());
        int pieceType = board.getPieceType(clickedSquare);
        if (selectedPieceSquare == 0) { //nothing already selectedSquare
            if (pieceType == Constants.EMPTY) return;   //not clicked on a piece
            if (Constants.getColor(pieceType) == this.color) {//piece of correct color
                selectedPieceSquare = clickedSquare;
                List<Move> movesForClicked = possibleMoves.stream().filter(m -> m.getFrom() == clickedSquare).collect(Collectors.toList());
                gui.setSelectedPieceSquare(clickedSquare, movesForClicked);
            }
        } else {    //there is a piece selected
            // Get all the moves that are selet
            List<Move> possibleMovesForPiece = possibleMoves.stream().filter(m -> m.getFrom() == selectedPieceSquare && m.getTo() == clickedSquare).collect(Collectors.toList());
            //There could be multiple possible moves in case of a pawn promotion
            if (possibleMovesForPiece.size() == 1) {
                moveToSet = possibleMovesForPiece.get(0); // There is only one in this case
            } else if (possibleMovesForPiece.size() > 1) { // if there is more than one option, it can only be a pawn promotion
                moveToSet = askUserForPromotion(possibleMovesForPiece);
            }

            // a move is selected, or the click was not on a move square, in either case, the selected square should be "unselected"
            selectedPieceSquare = 0; // a move will be done, so unselect the square
            gui.setSelectedPieceSquare(selectedPieceSquare, null);
            if (moveToSet != null) { // if there is actually a move to set, set it and notify the waiting engine
                synchronized (Constants.lock) {
                    move = moveToSet;
                    Constants.lock.notify();
                }
            }
        }
        gui.repaint(); // ask for a repaint if a user clicks a square
    }

    public Move askUserForPromotion(List<Move> possiblePromotions) {
        String[] options = {"Knight", "Bishop", "Rook", "Queen"};
        int n = JOptionPane.showOptionDialog(e.getFrame(), "Which piece would you like to promote this pawn in to?", "Pawn Promotion", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        Optional<Move> promotionChosen = possiblePromotions.stream().filter(m -> m.getFlags() % 4 == n).findAny();
        if (promotionChosen.isPresent()) {
            return promotionChosen.get();
        } else {
            throw new IllegalArgumentException("The correct promotion is not in the argument set");
        }
    }
}
