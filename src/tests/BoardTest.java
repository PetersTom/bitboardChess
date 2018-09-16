package tests;

import state.Bitboard;
import state.Board;
import state.Constants;
import state.Move;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {
    /**
     * 1. Na3
     */
    @Test
    void makeMoveKnight() {
        Board b = new Board();

        Move m = new Move(2, 17, Constants.WHITE_KNIGHT, Constants.EMPTY, 0);
        b.makeMove(m);

        // Check the mailbox representation
        assertEquals("rnbqkbnr/pppppppp/8/8/8/N7/PPPPPPPP/R1BQKBNR b KQkq - 1 1", b.toString());

        long[] bitboards = getBitboards(b);

        // everything except the white knights should be the same
        assertEquals(Constants.INITIAL_WHITE_ROOKS, bitboards[Constants.WHITE_ROOK]);
        assertEquals(Constants.INITIAL_WHITE_BISHOPS, bitboards[Constants.WHITE_BISHOP]);
        assertEquals(Constants.INITIAL_WHITE_QUEEN, bitboards[Constants.WHITE_QUEEN]);
        assertEquals(Constants.INITIAL_WHITE_KING, bitboards[Constants.WHITE_KING]);
        assertEquals(Constants.INITIAL_WHITE_PAWNS, bitboards[Constants.WHITE_PAWN]);
        assertEquals(Constants.INITIAL_BLACK_ROOKS, bitboards[Constants.BLACK_ROOK]);
        assertEquals(Constants.INITIAL_BLACK_KNIGHTS, bitboards[Constants.BLACK_KNIGHT]);
        assertEquals(Constants.INITIAL_BLACK_BISHOPS, bitboards[Constants.BLACK_BISHOP]);
        assertEquals(Constants.INITIAL_BLACK_QUEEN, bitboards[Constants.BLACK_QUEEN]);
        assertEquals(Constants.INITIAL_BLACK_KING, bitboards[Constants.BLACK_KING]);
        assertEquals(Constants.INITIAL_BLACK_PAWNS, bitboards[Constants.BLACK_PAWN]);
        assertEquals(0x0200800000000000L, bitboards[Constants.WHITE_KNIGHT]);
    }

    /**
     * The initial position. Tests the FEN notation
     */
    @Test
    void initial() {
        Board b = new Board();
        assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", b.toString());
    }

    /**
     * Tests a move with the queen
     * 1. c4 h5
     * 2. Qa4
     */
    @Test
    void makeMoveQueen() {
        Board b = new Board();

        Move first = new Move(11, 27, Constants.WHITE_PAWN, Constants.EMPTY, 0);
        b.makeMove(first);
        Move second = new Move(56, 40, Constants.BLACK_PAWN, Constants.EMPTY, 0);
        b.makeMove(second);
        Move third = new Move(4, 25, Constants.WHITE_QUEEN, Constants.EMPTY, 0);
        b.makeMove(third);

        // Check the mailbox representation
        assertEquals("rnbqkbnr/ppppppp1/8/7p/Q1P5/8/PP1PPPPP/RNB1KBNR b KQkq - 1 2", b.toString());

        long[] bitboards = getBitboards(b);

        assertEquals(Constants.INITIAL_WHITE_ROOKS, bitboards[Constants.WHITE_ROOK]);
        assertEquals(Constants.INITIAL_WHITE_KNIGHTS, bitboards[Constants.WHITE_KNIGHT]);
        assertEquals(Constants.INITIAL_WHITE_BISHOPS, bitboards[Constants.WHITE_BISHOP]);
        assertEquals(0x0000008000000000L, bitboards[Constants.WHITE_QUEEN]);
        assertEquals(Constants.INITIAL_WHITE_KING, bitboards[Constants.WHITE_KING]);
        assertEquals(0x00df002000000000L, bitboards[Constants.WHITE_PAWN]);
        assertEquals(Constants.INITIAL_BLACK_ROOKS, bitboards[Constants.BLACK_ROOK]);
        assertEquals(Constants.INITIAL_BLACK_KNIGHTS, bitboards[Constants.BLACK_KNIGHT]);
        assertEquals(Constants.INITIAL_BLACK_BISHOPS, bitboards[Constants.BLACK_BISHOP]);
        assertEquals(Constants.INITIAL_BLACK_QUEEN, bitboards[Constants.BLACK_QUEEN]);
        assertEquals(Constants.INITIAL_BLACK_KING, bitboards[Constants.BLACK_KING]);
        assertEquals(0x000000000100fe00L, bitboards[Constants.BLACK_PAWN]);
    }

    /**
     * Tests a capture
     * 1. e4 d5
     * 2. exd5
     */
    @Test
    void capture() {
        Board b = new Board();

        Move first = new Move(13, 29, Constants.WHITE_PAWN, Constants.EMPTY, 0);
        b.makeMove(first);
        Move second = new Move(52, 36, Constants.BLACK_PAWN, Constants.EMPTY, 0);
        b.makeMove(second);
        Move third = new Move(29, 36, Constants.WHITE_PAWN, Constants.BLACK_PAWN, 4);
        b.makeMove(third);

        // Check the mailbox representation
        assertEquals("rnbqkbnr/ppp1pppp/8/3P4/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 2", b.toString());

        long[] bitboards = getBitboards(b);

        assertEquals(Constants.INITIAL_WHITE_ROOKS, bitboards[Constants.WHITE_ROOK]);
        assertEquals(Constants.INITIAL_WHITE_KNIGHTS, bitboards[Constants.WHITE_KNIGHT]);
        assertEquals(Constants.INITIAL_WHITE_BISHOPS, bitboards[Constants.WHITE_BISHOP]);
        assertEquals(Constants.INITIAL_WHITE_QUEEN, bitboards[Constants.WHITE_QUEEN]);
        assertEquals(Constants.INITIAL_WHITE_KING, bitboards[Constants.WHITE_KING]);
        assertEquals(0x00f7000010000000L, bitboards[Constants.WHITE_PAWN]);
        assertEquals(Constants.INITIAL_BLACK_ROOKS, bitboards[Constants.BLACK_ROOK]);
        assertEquals(Constants.INITIAL_BLACK_KNIGHTS, bitboards[Constants.BLACK_KNIGHT]);
        assertEquals(Constants.INITIAL_BLACK_BISHOPS, bitboards[Constants.BLACK_BISHOP]);
        assertEquals(Constants.INITIAL_BLACK_QUEEN, bitboards[Constants.BLACK_QUEEN]);
        assertEquals(Constants.INITIAL_BLACK_KING, bitboards[Constants.BLACK_KING]);
        assertEquals(0x000000000000ef00, bitboards[Constants.BLACK_PAWN]);
    }

    /**
     * Tests the promotion to queen
     * 1. h4 g5
     * 2. hxg5 h6
     * 3. gxh6 Nf6
     * 4 h7 Rg8
     * 5 hxg8=Q
     */
    @Test
    void promotionQueen() {
        Board b = new Board();

        Move first = new Move(16, 32, Constants.WHITE_PAWN, Constants.EMPTY, 0);
        b.makeMove(first);
        Move second = new Move(55, 39, Constants.BLACK_PAWN, Constants.EMPTY, 0);
        b.makeMove(second);
        Move third = new Move(32, 39, Constants.WHITE_PAWN,Constants.BLACK_PAWN, 4);
        b.makeMove(third);
        Move fourth = new Move(56, 48, Constants.BLACK_PAWN, Constants.EMPTY, 0);
        b.makeMove(fourth);
        Move fifth = new Move(39, 48, Constants.WHITE_PAWN, Constants.BLACK_PAWN, 4);
        b.makeMove(fifth);
        Move sixth = new Move(63, 46, Constants.BLACK_KNIGHT, Constants.EMPTY, 0);
        b.makeMove(sixth);
        Move seventh = new Move(48, 56, Constants.WHITE_PAWN, Constants.EMPTY, 0);
        b.makeMove(seventh);
        Move eighth = new Move(64, 63, Constants.BLACK_ROOK, Constants.EMPTY, 0);
        b.makeMove(eighth);
        Move nineth = new Move(56, 63, Constants.WHITE_PAWN, Constants.BLACK_ROOK,15);
        b.makeMove(nineth);

        // Check the mailbox representation
        assertEquals("rnbqkbQ1/pppppp2/5n2/8/8/8/PPPPPPP1/RNBQKBNR b KQq - 0 5", b.toString());

        long[] bitboards = getBitboards(b);

        assertEquals(Constants.INITIAL_WHITE_ROOKS, bitboards[Constants.WHITE_ROOK]);
        assertEquals(Constants.INITIAL_WHITE_KNIGHTS, bitboards[Constants.WHITE_KNIGHT]);
        assertEquals(Constants.INITIAL_WHITE_BISHOPS, bitboards[Constants.WHITE_BISHOP]);
        assertEquals(0x1000000000000002L, bitboards[Constants.WHITE_QUEEN]);
        assertEquals(Constants.INITIAL_WHITE_KING, bitboards[Constants.WHITE_KING]);
        assertEquals(0x00fe000000000000L, bitboards[Constants.WHITE_PAWN]);
        assertEquals(0x0000000000000080L, bitboards[Constants.BLACK_ROOK]);
        assertEquals(0x0000000000040040L, bitboards[Constants.BLACK_KNIGHT]);
        assertEquals(Constants.INITIAL_BLACK_BISHOPS, bitboards[Constants.BLACK_BISHOP]);
        assertEquals(Constants.INITIAL_BLACK_QUEEN, bitboards[Constants.BLACK_QUEEN]);
        assertEquals(Constants.INITIAL_BLACK_KING, bitboards[Constants.BLACK_KING]);
        assertEquals(0x000000000000fc00, bitboards[Constants.BLACK_PAWN]);
    }

    /**
     * Test en passant
     * 1. h4 h6
     * 2. h5 g5
     * 3. hxg6
     */
    @Test
    void enPassent() {
        Board b = new Board();

        Move first = new Move(16, 32, Constants.WHITE_PAWN, Constants.EMPTY, 0);
        b.makeMove(first);
        Move second = new Move(56, 48, Constants.BLACK_PAWN, Constants.EMPTY, 0);
        b.makeMove(second);
        Move third = new Move(32, 40, Constants.WHITE_PAWN, Constants.EMPTY, 0);
        b.makeMove(third);
        Move fourth = new Move(55, 39, Constants.BLACK_PAWN, Constants.EMPTY, 0);
        b.makeMove(fourth);
        Move fifth = new Move(40, 47, Constants.WHITE_PAWN, Constants.BLACK_PAWN, 5);
        b.makeMove(fifth);

        // Check the mailbox representation
        assertEquals("rnbqkbnr/pppppp2/6Pp/8/8/8/PPPPPPP1/RNBQKBNR b KQkq - 0 3", b.toString());

        long[] bitboards = getBitboards(b);

        assertEquals(Constants.INITIAL_WHITE_ROOKS, bitboards[Constants.WHITE_ROOK]);
        assertEquals(Constants.INITIAL_WHITE_KNIGHTS, bitboards[Constants.WHITE_KNIGHT]);
        assertEquals(Constants.INITIAL_WHITE_BISHOPS, bitboards[Constants.WHITE_BISHOP]);
        assertEquals(Constants.INITIAL_WHITE_QUEEN, bitboards[Constants.WHITE_QUEEN]);
        assertEquals(Constants.INITIAL_WHITE_KING, bitboards[Constants.WHITE_KING]);
        assertEquals(0x00fe000000020000L, bitboards[Constants.WHITE_PAWN]);
        assertEquals(Constants.INITIAL_BLACK_ROOKS, bitboards[Constants.BLACK_ROOK]);
        assertEquals(Constants.INITIAL_BLACK_KNIGHTS, bitboards[Constants.BLACK_KNIGHT]);
        assertEquals(Constants.INITIAL_BLACK_BISHOPS, bitboards[Constants.BLACK_BISHOP]);
        assertEquals(Constants.INITIAL_BLACK_QUEEN, bitboards[Constants.BLACK_QUEEN]);
        assertEquals(Constants.INITIAL_BLACK_KING, bitboards[Constants.BLACK_KING]);
        assertEquals(0x000000000001fc00, bitboards[Constants.BLACK_PAWN]);
    }

    /**
     * Tests castling on whites kingside
     * 1. e4 Nh6
     * 2. Bd3 Ng8
     * 3. Nf3 Nh6
     * 4. O-O
     */
    @Test
    void whiteKingCastle() {
        Board b = new Board();

        Move first = new Move(13, 29, Constants.WHITE_PAWN, Constants.EMPTY, 0);
        b.makeMove(first);
        Move second = new Move(63, 48, Constants.BLACK_KNIGHT, Constants.EMPTY, 0);
        b.makeMove(second);
        Move third = new Move(6, 20, Constants.WHITE_BISHOP, Constants.EMPTY, 0);
        b.makeMove(third);
        Move fourth = new Move(48, 63, Constants.BLACK_KNIGHT, Constants.EMPTY, 0);
        b.makeMove(fourth);
        Move fifth = new Move(7, 22, Constants.WHITE_KNIGHT, Constants.EMPTY, 0);
        b.makeMove(fifth);
        Move sixth = new Move(63, 48, Constants.BLACK_KNIGHT, Constants.EMPTY, 0);
        b.makeMove(sixth);
        Move seventh = new Move(5, 7, Constants.WHITE_KING, Constants.EMPTY, 2);
        b.makeMove(seventh);

        // Check the mailbox representation
        assertEquals("rnbqkb1r/pppppppp/7n/8/4P3/3B1N2/PPPP1PPP/RNBQ1RK1 b kq - 6 4", b.toString());

        long[] bitboards = getBitboards(b);

        assertEquals(0x8400000000000000L, bitboards[Constants.WHITE_ROOK]);
        assertEquals(0x4000040000000000L, bitboards[Constants.WHITE_KNIGHT]);
        assertEquals(0x2000100000000000L, bitboards[Constants.WHITE_BISHOP]);
        assertEquals(Constants.INITIAL_WHITE_QUEEN, bitboards[Constants.WHITE_QUEEN]);
        assertEquals(0x0200000000000000L, bitboards[Constants.WHITE_KING]);
        assertEquals(0x00f7000800000000L, bitboards[Constants.WHITE_PAWN]);
        assertEquals(Constants.INITIAL_BLACK_ROOKS, bitboards[Constants.BLACK_ROOK]);
        assertEquals(0x0000000000010040L, bitboards[Constants.BLACK_KNIGHT]);
        assertEquals(Constants.INITIAL_BLACK_BISHOPS, bitboards[Constants.BLACK_BISHOP]);
        assertEquals(Constants.INITIAL_BLACK_QUEEN, bitboards[Constants.BLACK_QUEEN]);
        assertEquals(Constants.INITIAL_BLACK_KING, bitboards[Constants.BLACK_KING]);
        assertEquals(Constants.INITIAL_BLACK_PAWNS, bitboards[Constants.BLACK_PAWN]);
    }

    /**
     * Tests castling on whites queenside
     * 1. d4 Nh6
     * 2. Be3 Ng8
     * 3. Na3 Nh6
     * 4. Qd2 Ng8
     * 5. O-O-O
     */
    @Test
    void whiteQueenCastle() {
        Board b = new Board();

        Move first = new Move(12, 28, Constants.WHITE_PAWN, Constants.EMPTY, 0);
        b.makeMove(first);
        Move second = new Move(63, 48, Constants.BLACK_KNIGHT, Constants.EMPTY, 0);
        b.makeMove(second);
        Move third = new Move(3, 21, Constants.WHITE_BISHOP, Constants.EMPTY, 0);
        b.makeMove(third);
        Move fourth = new Move(48, 63, Constants.BLACK_KNIGHT, Constants.EMPTY, 0);
        b.makeMove(fourth);
        Move fifth = new Move(2, 17, Constants.WHITE_KNIGHT, Constants.EMPTY, 0);
        b.makeMove(fifth);
        Move sixth = new Move(63, 48, Constants.BLACK_KNIGHT, Constants.EMPTY, 0);
        b.makeMove(sixth);
        Move seventh = new Move(4, 12, Constants.WHITE_QUEEN, Constants.EMPTY, 0);
        b.makeMove(seventh);
        Move eighth = new Move(48, 63, Constants.BLACK_KNIGHT, Constants.EMPTY, 0);
        b.makeMove(eighth);
        Move nineth = new Move(5, 3, Constants.WHITE_KING, Constants.EMPTY, 3);
        b.makeMove(nineth);

        // Check the mailbox representation
        assertEquals("rnbqkbnr/pppppppp/8/8/3P4/N3B3/PPPQPPPP/2KR1BNR b kq - 8 5", b.toString());

        long[] bitboards = getBitboards(b);

        assertEquals(0x1100000000000000L, bitboards[Constants.WHITE_ROOK]);
        assertEquals(0x0200800000000000L, bitboards[Constants.WHITE_KNIGHT]);
        assertEquals(0x0400080000000000L, bitboards[Constants.WHITE_BISHOP]);
        assertEquals(0x0010000000000000L, bitboards[Constants.WHITE_QUEEN]);
        assertEquals(0x2000000000000000L, bitboards[Constants.WHITE_KING]);
        assertEquals(0x00ef001000000000L, bitboards[Constants.WHITE_PAWN]);
        assertEquals(Constants.INITIAL_BLACK_ROOKS, bitboards[Constants.BLACK_ROOK]);
        assertEquals(Constants.INITIAL_BLACK_KNIGHTS, bitboards[Constants.BLACK_KNIGHT]);
        assertEquals(Constants.INITIAL_BLACK_BISHOPS, bitboards[Constants.BLACK_BISHOP]);
        assertEquals(Constants.INITIAL_BLACK_QUEEN, bitboards[Constants.BLACK_QUEEN]);
        assertEquals(Constants.INITIAL_BLACK_KING, bitboards[Constants.BLACK_KING]);
        assertEquals(Constants.INITIAL_BLACK_PAWNS, bitboards[Constants.BLACK_PAWN]);
    }

    /**
     * Tests castling on blacks kingside
     * 1. Na3 e5
     * 2. Nb1 Ba3
     * 3. Nxa3 Nf6
     * 4. Nb1 O-O
     */
    @Test
    void blackKingCastle() {
        Board b = new Board();

        Move first = new Move(2, 17, Constants.WHITE_KNIGHT, Constants.EMPTY, 0);
        b.makeMove(first);
        Move second = new Move(53, 37, Constants.BLACK_PAWN, Constants.EMPTY, 0);
        b.makeMove(second);
        Move third = new Move(17, 2, Constants.WHITE_KNIGHT, Constants.EMPTY, 0);
        b.makeMove(third);
        Move fourth = new Move(62, 17, Constants.BLACK_BISHOP, Constants.EMPTY, 0);
        b.makeMove(fourth);
        Move fifth = new Move(2, 17, Constants.WHITE_KNIGHT, Constants.BLACK_BISHOP, 4);
        b.makeMove(fifth);
        Move sixth = new Move(63, 46, Constants.BLACK_KNIGHT, Constants.EMPTY, 0);
        b.makeMove(sixth);
        Move seventh = new Move(17, 2, Constants.WHITE_KNIGHT, Constants.EMPTY, 0);
        b.makeMove(seventh);
        Move eighth = new Move(61, 63, Constants.BLACK_KING, Constants.EMPTY, 2);
        b.makeMove(eighth);

        // Check the mailbox representation
        assertEquals("rnbq1rk1/pppp1ppp/5n2/4p3/8/8/PPPPPPPP/RNBQKBNR w KQ - 3 5", b.toString());

        long[] bitboards = getBitboards(b);

        assertEquals(Constants.INITIAL_WHITE_ROOKS, bitboards[Constants.WHITE_ROOK]);
        assertEquals(Constants.INITIAL_WHITE_KNIGHTS, bitboards[Constants.WHITE_KNIGHT]);
        assertEquals(Constants.INITIAL_WHITE_BISHOPS, bitboards[Constants.WHITE_BISHOP]);
        assertEquals(Constants.INITIAL_WHITE_QUEEN, bitboards[Constants.WHITE_QUEEN]);
        assertEquals(Constants.INITIAL_WHITE_KING, bitboards[Constants.WHITE_KING]);
        assertEquals(Constants.INITIAL_WHITE_PAWNS, bitboards[Constants.WHITE_PAWN]);
        assertEquals(0x0000000000000084L, bitboards[Constants.BLACK_ROOK]);
        assertEquals(0x0000000000040040L, bitboards[Constants.BLACK_KNIGHT]);
        assertEquals(0x0000000000000020L, bitboards[Constants.BLACK_BISHOP]);
        assertEquals(Constants.INITIAL_BLACK_QUEEN, bitboards[Constants.BLACK_QUEEN]);
        assertEquals(0x0000000000000002L, bitboards[Constants.BLACK_KING]);
        assertEquals(0x0000000800f700L, bitboards[Constants.BLACK_PAWN]);
    }

    /**
     * Gets the private bitboards to test them
     * @param b the board object
     * @return the bitboards variable
     */
    private long[] getBitboards(Board b) {
        Field bitboardsfield;
        long[] bitboards = new long[12];
        try {
            bitboardsfield = b.getClass().getDeclaredField("bitboards");
            bitboardsfield.setAccessible(true);
            bitboards = (long[]) bitboardsfield.get(b);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return bitboards;
    }


    @Test
    void testShifting() {
        long start = 0x55aa55aa55aa55aaL;
        assertEquals(0x0055aa55aa55aa55L, Bitboard.nortOne(start));
        assertEquals(0x002a552a552a552aL, Bitboard.noEaOne(start));
        assertEquals(0x2a552a552a552a55L, Bitboard.eastOne(start));
        assertEquals(0x552a552a552a5500L, Bitboard.soEaOne(start));
        assertEquals(0xaa55aa55aa55aa00L, Bitboard.soutOne(start));
        assertEquals(0x54aa54aa54aa5400L, Bitboard.soWeOne(start));
        assertEquals(0xaa54aa54aa54aa54L, Bitboard.westOne(start));
        assertEquals(0x00aa54aa54aa54aaL, Bitboard.noWeOne(start));
    }

    @Test
    void leastSignificant() {
        long start = 0x55aa55aa55aa55aaL;
        assertEquals(2L, Bitboard.leastSignificantBit(start));
        assertEquals(0x55aa55aa55aa55a8L, Bitboard.resetLeastSignificantBit(start));
    }

    @Test
    void popCount() {
        long toTest = 0x0010800402010408L;
        assertEquals(7, Bitboard.getPopCount(toTest));
    }

    @Test
    void enPassentSquare() {
        Stack<Integer> enPassentSquare = new Stack<>();
        enPassentSquare.push(10); //b2
        Board b = new Board();
        Field enPassentSquareField;
        try {
            enPassentSquareField = b.getClass().getDeclaredField("enPassentSquare");
            enPassentSquareField.setAccessible(true);
            enPassentSquareField.set(b, enPassentSquare); // set the field in the board variable
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        assertEquals(0x0040000000000000L, b.getEnPassenSquareBitboard());
    }

    @Test
    void noEnPassentSquare() {
        Stack<Integer> enPassentSquare = new Stack<>();
        enPassentSquare.push(0); //no en passent square
        Board b = new Board();
        Field enPassentSquareField;
        try {
            enPassentSquareField = b.getClass().getDeclaredField("enPassentSquare");
            enPassentSquareField.setAccessible(true);
            enPassentSquareField.set(b, enPassentSquare); // set the field in the board variable
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        assertEquals(0, b.getEnPassenSquareBitboard());
    }

    @Test
    void unMakeMove() {
        // First does a normal knight move and then undoes it (makeMoveKnight() checks if this makeMove is correct)
        Board b = new Board();

        Move m = new Move(2, 17, Constants.WHITE_KNIGHT, Constants.EMPTY, 0);
        b.makeMove(m);

        b.unMakeMove(m);
        // Check the mailbox representation
        assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", b.toString());

        long[] bitboards = getBitboards(b);

        // everything except the white knights should be the same
        assertEquals(Constants.INITIAL_WHITE_ROOKS, bitboards[Constants.WHITE_ROOK]);
        assertEquals(Constants.INITIAL_WHITE_KNIGHTS, bitboards[Constants.WHITE_KNIGHT]);
        assertEquals(Constants.INITIAL_WHITE_BISHOPS, bitboards[Constants.WHITE_BISHOP]);
        assertEquals(Constants.INITIAL_WHITE_QUEEN, bitboards[Constants.WHITE_QUEEN]);
        assertEquals(Constants.INITIAL_WHITE_KING, bitboards[Constants.WHITE_KING]);
        assertEquals(Constants.INITIAL_WHITE_PAWNS, bitboards[Constants.WHITE_PAWN]);
        assertEquals(Constants.INITIAL_BLACK_ROOKS, bitboards[Constants.BLACK_ROOK]);
        assertEquals(Constants.INITIAL_BLACK_KNIGHTS, bitboards[Constants.BLACK_KNIGHT]);
        assertEquals(Constants.INITIAL_BLACK_BISHOPS, bitboards[Constants.BLACK_BISHOP]);
        assertEquals(Constants.INITIAL_BLACK_QUEEN, bitboards[Constants.BLACK_QUEEN]);
        assertEquals(Constants.INITIAL_BLACK_KING, bitboards[Constants.BLACK_KING]);
        assertEquals(Constants.INITIAL_BLACK_PAWNS, bitboards[Constants.BLACK_PAWN]);
    }

    @Test
    void unCapture() {
        Board b = new Board();
        Move first = new Move(13, 29, Constants.WHITE_PAWN, Constants.EMPTY, 0);
        b.makeMove(first);
        Move second = new Move(52, 36, Constants.BLACK_PAWN, Constants.EMPTY, 0);
        b.makeMove(second);
        Move third = new Move(29, 36, Constants.WHITE_PAWN, Constants.BLACK_PAWN, 4);
        b.makeMove(third);

        b.unMakeMove(third);

        // Check the mailbox representation
        assertEquals("rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 2", b.toString());

        long[] bitboards = getBitboards(b);

        assertEquals(Constants.INITIAL_WHITE_ROOKS, bitboards[Constants.WHITE_ROOK]);
        assertEquals(Constants.INITIAL_WHITE_KNIGHTS, bitboards[Constants.WHITE_KNIGHT]);
        assertEquals(Constants.INITIAL_WHITE_BISHOPS, bitboards[Constants.WHITE_BISHOP]);
        assertEquals(Constants.INITIAL_WHITE_QUEEN, bitboards[Constants.WHITE_QUEEN]);
        assertEquals(Constants.INITIAL_WHITE_KING, bitboards[Constants.WHITE_KING]);
        assertEquals(0x00f7000800000000L, bitboards[Constants.WHITE_PAWN]);
        assertEquals(Constants.INITIAL_BLACK_ROOKS, bitboards[Constants.BLACK_ROOK]);
        assertEquals(Constants.INITIAL_BLACK_KNIGHTS, bitboards[Constants.BLACK_KNIGHT]);
        assertEquals(Constants.INITIAL_BLACK_BISHOPS, bitboards[Constants.BLACK_BISHOP]);
        assertEquals(Constants.INITIAL_BLACK_QUEEN, bitboards[Constants.BLACK_QUEEN]);
        assertEquals(Constants.INITIAL_BLACK_KING, bitboards[Constants.BLACK_KING]);
        assertEquals(0x000000001000ef00, bitboards[Constants.BLACK_PAWN]);
    }

    @Test
    void unEnPassent() {
        Board b = new Board();

        Move first = new Move(16, 32, Constants.WHITE_PAWN, Constants.EMPTY, 0);
        b.makeMove(first);
        Move second = new Move(56, 48, Constants.BLACK_PAWN, Constants.EMPTY, 0);
        b.makeMove(second);
        Move third = new Move(32, 40, Constants.WHITE_PAWN, Constants.EMPTY, 0);
        b.makeMove(third);
        Move fourth = new Move(55, 39, Constants.BLACK_PAWN, Constants.EMPTY, 0);
        b.makeMove(fourth);
        Move fifth = new Move(40, 47, Constants.WHITE_PAWN, Constants.BLACK_PAWN, 5);
        b.makeMove(fifth);

        b.unMakeMove(fifth);

        // Check the mailbox representation
        assertEquals("rnbqkbnr/pppppp2/7p/6pP/8/8/PPPPPPP1/RNBQKBNR w KQkq g6 0 3", b.toString());

        long[] bitboards = getBitboards(b);

        assertEquals(Constants.INITIAL_WHITE_ROOKS, bitboards[Constants.WHITE_ROOK]);
        assertEquals(Constants.INITIAL_WHITE_KNIGHTS, bitboards[Constants.WHITE_KNIGHT]);
        assertEquals(Constants.INITIAL_WHITE_BISHOPS, bitboards[Constants.WHITE_BISHOP]);
        assertEquals(Constants.INITIAL_WHITE_QUEEN, bitboards[Constants.WHITE_QUEEN]);
        assertEquals(Constants.INITIAL_WHITE_KING, bitboards[Constants.WHITE_KING]);
        assertEquals(0x00fe000001000000L, bitboards[Constants.WHITE_PAWN]);
        assertEquals(Constants.INITIAL_BLACK_ROOKS, bitboards[Constants.BLACK_ROOK]);
        assertEquals(Constants.INITIAL_BLACK_KNIGHTS, bitboards[Constants.BLACK_KNIGHT]);
        assertEquals(Constants.INITIAL_BLACK_BISHOPS, bitboards[Constants.BLACK_BISHOP]);
        assertEquals(Constants.INITIAL_BLACK_QUEEN, bitboards[Constants.BLACK_QUEEN]);
        assertEquals(Constants.INITIAL_BLACK_KING, bitboards[Constants.BLACK_KING]);
        assertEquals(0x000000000201fc00, bitboards[Constants.BLACK_PAWN]);
    }

    @Test
    void unPromote() {
        Board b = new Board();

        Move first = new Move(16, 32, Constants.WHITE_PAWN, Constants.EMPTY, 0);
        b.makeMove(first);
        Move second = new Move(55, 39, Constants.BLACK_PAWN, Constants.EMPTY, 0);
        b.makeMove(second);
        Move third = new Move(32, 39, Constants.WHITE_PAWN,Constants.BLACK_PAWN, 4);
        b.makeMove(third);
        Move fourth = new Move(56, 48, Constants.BLACK_PAWN, Constants.EMPTY, 0);
        b.makeMove(fourth);
        Move fifth = new Move(39, 48, Constants.WHITE_PAWN, Constants.BLACK_PAWN, 4);
        b.makeMove(fifth);
        Move sixth = new Move(63, 46, Constants.BLACK_KNIGHT, Constants.EMPTY, 0);
        b.makeMove(sixth);
        Move seventh = new Move(48, 56, Constants.WHITE_PAWN, Constants.EMPTY, 0);
        b.makeMove(seventh);
        Move eighth = new Move(64, 63, Constants.BLACK_ROOK, Constants.EMPTY, 0);
        b.makeMove(eighth);
        Move nineth = new Move(56, 63, Constants.WHITE_PAWN, Constants.BLACK_ROOK,15);
        b.makeMove(nineth);

        b.unMakeMove(nineth);

        // Check the mailbox representation
        assertEquals("rnbqkbr1/pppppp1P/5n2/8/8/8/PPPPPPP1/RNBQKBNR w KQq - 1 5", b.toString());

        long[] bitboards = getBitboards(b);

        assertEquals(Constants.INITIAL_WHITE_ROOKS, bitboards[Constants.WHITE_ROOK]);
        assertEquals(Constants.INITIAL_WHITE_KNIGHTS, bitboards[Constants.WHITE_KNIGHT]);
        assertEquals(Constants.INITIAL_WHITE_BISHOPS, bitboards[Constants.WHITE_BISHOP]);
        assertEquals(0x1000000000000000L, bitboards[Constants.WHITE_QUEEN]);
        assertEquals(Constants.INITIAL_WHITE_KING, bitboards[Constants.WHITE_KING]);
        assertEquals(0x00fe000000000100L, bitboards[Constants.WHITE_PAWN]);
        assertEquals(0x0000000000000082L, bitboards[Constants.BLACK_ROOK]);
        assertEquals(0x0000000000040040L, bitboards[Constants.BLACK_KNIGHT]);
        assertEquals(Constants.INITIAL_BLACK_BISHOPS, bitboards[Constants.BLACK_BISHOP]);
        assertEquals(Constants.INITIAL_BLACK_QUEEN, bitboards[Constants.BLACK_QUEEN]);
        assertEquals(Constants.INITIAL_BLACK_KING, bitboards[Constants.BLACK_KING]);
        assertEquals(0x000000000000fc00, bitboards[Constants.BLACK_PAWN]);
    }

    @Test
    void testUnCapture() {
        Board b = new Board();
        Move first = new Move(9, 25, Constants.WHITE_PAWN, Constants.EMPTY, 1);
        b.makeMove(first);
        Move second = new Move(50, 34, Constants.BLACK_PAWN, Constants.EMPTY, 1);
        b.makeMove(second);
        Move third = new Move(25, 34, Constants.WHITE_PAWN, Constants.BLACK_PAWN, 4);
        b.makeMove(third);

        b.unMakeMove(third);

        assertEquals("rnbqkbnr/p1pppppp/8/1p6/P7/8/1PPPPPPP/RNBQKBNR w KQkq b6 0 2", b.toString());

        long[] bitboards = getBitboards(b);

        assertEquals(Constants.INITIAL_WHITE_ROOKS, bitboards[Constants.WHITE_ROOK]);
        assertEquals(Constants.INITIAL_WHITE_KNIGHTS, bitboards[Constants.WHITE_KNIGHT]);
        assertEquals(Constants.INITIAL_WHITE_BISHOPS, bitboards[Constants.WHITE_BISHOP]);
        assertEquals(Constants.INITIAL_WHITE_QUEEN, bitboards[Constants.WHITE_QUEEN]);
        assertEquals(Constants.INITIAL_WHITE_KING, bitboards[Constants.WHITE_KING]);
        assertEquals(0x007f008000000000L, bitboards[Constants.WHITE_PAWN]);
        assertEquals(Constants.INITIAL_BLACK_ROOKS, bitboards[Constants.BLACK_ROOK]);
        assertEquals(Constants.INITIAL_BLACK_KNIGHTS, bitboards[Constants.BLACK_KNIGHT]);
        assertEquals(Constants.INITIAL_BLACK_BISHOPS, bitboards[Constants.BLACK_BISHOP]);
        assertEquals(Constants.INITIAL_BLACK_QUEEN, bitboards[Constants.BLACK_QUEEN]);
        assertEquals(Constants.INITIAL_BLACK_KING, bitboards[Constants.BLACK_KING]);
        assertEquals(0x000000004000bf00, bitboards[Constants.BLACK_PAWN]);
    }

    @Test
    void customFen() {
        long[] bitboards = new long[14];
        bitboards[Constants.WHITE_ROOK] = Constants.INITIAL_WHITE_ROOKS;
        bitboards[Constants.WHITE_KNIGHT] = Constants.INITIAL_WHITE_KNIGHTS;
        bitboards[Constants.WHITE_BISHOP] = Constants.INITIAL_WHITE_BISHOPS;
        bitboards[Constants.WHITE_QUEEN] = Constants.INITIAL_WHITE_QUEEN;
        bitboards[Constants.WHITE_PAWN] = 0x00fe000000000100L;
        bitboards[Constants.WHITE_KING] = Constants.INITIAL_WHITE_KING;
        bitboards[Constants.BLACK_ROOK] = 0x0000000000000082L;
        bitboards[Constants.BLACK_KNIGHT] = 0x0000000000040040L;
        bitboards[Constants.BLACK_BISHOP] = Constants.INITIAL_BLACK_BISHOPS;
        bitboards[Constants.BLACK_QUEEN] = Constants.INITIAL_BLACK_QUEEN;
        bitboards[Constants.BLACK_KING] = Constants.INITIAL_BLACK_KING;
        bitboards[Constants.BLACK_PAWN] = 0x000000000000fc00;

        Board b = new Board("rnbqkbr1/pppppp1P/5n2/8/8/8/PPPPPPP1/RNBQKBNR w KQq - 1 5");
        assertEquals("rnbqkbr1/pppppp1P/5n2/8/8/8/PPPPPPP1/RNBQKBNR w KQq - 1 5", b.toString());

        bitboards = getBitboards(b);

        assertEquals(Constants.INITIAL_WHITE_ROOKS, bitboards[Constants.WHITE_ROOK]);
        assertEquals(Constants.INITIAL_WHITE_KNIGHTS, bitboards[Constants.WHITE_KNIGHT]);
        assertEquals(Constants.INITIAL_WHITE_BISHOPS, bitboards[Constants.WHITE_BISHOP]);
        assertEquals(Constants.INITIAL_WHITE_QUEEN, bitboards[Constants.WHITE_QUEEN]);
        assertEquals(Constants.INITIAL_WHITE_KING, bitboards[Constants.WHITE_KING]);
        assertEquals(0x00fe000000000100L, bitboards[Constants.WHITE_PAWN]);
        assertEquals(0x0000000000000082L, bitboards[Constants.BLACK_ROOK]);
        assertEquals(0x0000000000040040L, bitboards[Constants.BLACK_KNIGHT]);
        assertEquals(Constants.INITIAL_BLACK_BISHOPS, bitboards[Constants.BLACK_BISHOP]);
        assertEquals(Constants.INITIAL_BLACK_QUEEN, bitboards[Constants.BLACK_QUEEN]);
        assertEquals(Constants.INITIAL_BLACK_KING, bitboards[Constants.BLACK_KING]);
        assertEquals(0x000000000000fc00, bitboards[Constants.BLACK_PAWN]);
    }

    @Test
    void splitRayTest() {
        long ray =      0xff00000000000000L;
        long split =    0x1000000000000000L;
        long[] splitted = Bitboard.splitRay(ray, split);
        assertEquals(0xe000000000000000L, splitted[1]);
        assertEquals(0x0f00000000000000L, splitted[0]);
    }

    @Test
    void splitRayDiagonal() {
        long ray =      0x8040201008040201L; // long normal diagonal
        long split =   0x0000200000000000L; //c3
        long[] splitted = Bitboard.splitRay(ray, split);
        assertEquals(0x8040000000000000L, splitted[1]);
        assertEquals(0x0000001008040201L, splitted[0]);
    }

    @Test
    void splitEdge() {
        long ray =      0xff00000000000000L;
        long split =    0x8000000000000000L;
        long[] splitted = Bitboard.splitRay(ray, split);
        assertEquals(0x0000000000000000L, splitted[1]);
        assertEquals(0x7f00000000000000L, splitted[0]);
    }
}