package tests;

import state.Constants;
import state.Move;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoveTest {
    @Test
    void getFrom() {
        Move m = new Move(1, 0, 0, Constants.EMPTY, 0);
        assertEquals(1, m.getFrom());
        m = new Move(64, 0, 0,Constants.EMPTY, 0);
        assertEquals(64, m.getFrom());
        m = new Move(2, 2, 2, Constants.EMPTY, 2);
        assertEquals(2, m.getFrom());
    }

    @Test
    void getTo() {
        Move m = new Move(0, 1, 0, Constants.EMPTY, 0);
        assertEquals(1, m.getTo());
        m = new Move(0, 64, 0, Constants.EMPTY, 0);
        assertEquals(64, m.getTo());
        m = new Move(2, 2, 2, Constants.EMPTY, 2);
        assertEquals(2, m.getTo());
    }

    @Test
    void getMovingPiece() {
        Move m = new Move(0, 1, Constants.WHITE_KNIGHT, Constants.EMPTY, 0);
        assertEquals(Constants.WHITE_KNIGHT, m.getMovingPiece());
        m = new Move(0, 64, Constants.EMPTY, Constants.EMPTY, 0);
        assertEquals(Constants.EMPTY, m.getMovingPiece());
        m = new Move(2, 2, Constants.BLACK_QUEEN, Constants.EMPTY, 2);
        assertEquals(Constants.BLACK_QUEEN, m.getMovingPiece());
    }

    @Test
    void getPromotionPiece() {
        Move m = new Move(0, 1, 0, Constants.EMPTY, 8);
        assertEquals(Constants.KNIGHT, m.getPromotionPiece());
        m = new Move(0, 64, 0,Constants.EMPTY, 0);
        assertEquals(Constants.EMPTY, m.getPromotionPiece());
        m = new Move(2, 2, 0, Constants.EMPTY, 15);
        assertEquals(Constants.QUEEN, m.getPromotionPiece());
    }

    @Test
    void getFlags() {
        Move m = new Move(0, 1, 0, Constants.EMPTY, 0);
        assertEquals(0, m.getFlags());
        m = new Move(0, 64, 0, Constants.EMPTY, 15);
        assertEquals(15, m.getFlags());
        m = new Move(2, 2, 0, Constants.EMPTY, 3);
        assertEquals(3, m.getFlags());
    }

}