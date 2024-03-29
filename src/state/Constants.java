package state;

public class Constants {
    public static final Object lock = new Object(); // a constant object to monitor the threads and wait and notify
    public static final int ROW_SIZE = 8;
    public static final long INITIAL_WHITE_ROOKS = 0b10000001L << ROW_SIZE * 7;
    public static final long INITIAL_WHITE_KNIGHTS = 0b01000010L << ROW_SIZE * 7;
    public static final long INITIAL_WHITE_BISHOPS = 0b00100100L << ROW_SIZE * 7;
    public static final long INITIAL_WHITE_QUEEN = 0b00010000L << ROW_SIZE * 7;
    public static final long INITIAL_WHITE_KING = 0b00001000L << ROW_SIZE * 7;
    public static final long INITIAL_WHITE_PAWNS = 0b11111111L << ROW_SIZE * 6;
    public static final long INITIAL_BLACK_ROOKS = 0b10000001;
    public static final long INITIAL_BLACK_KNIGHTS = 0b01000010;
    public static final long INITIAL_BLACK_BISHOPS = 0b00100100;
    public static final long INITIAL_BLACK_QUEEN = 0b00010000;
    public static final long INITIAL_BLACK_KING = 0b00001000;
    public static final long INITIAL_BLACK_PAWNS = 0B11111111 << 8;

    public static final int ROOK = 0b0010;
    public static final int BISHOP = 0b0100;
    public static final int KNIGHT = 0b0110;
    public static final int QUEEN = 0b1000;
    public static final int KING = 0b1010;
    public static final int PAWN = 0b1100;

    public static final int WHITE = 0;
    public static final int BLACK = 1;

    public static final int WHITE_ROOK = ROOK | WHITE;
    public static final int WHITE_KNIGHT = KNIGHT | WHITE;
    public static final int WHITE_BISHOP = BISHOP | WHITE;
    public static final int WHITE_QUEEN = QUEEN | WHITE;
    public static final int WHITE_KING = KING | WHITE;
    public static final int WHITE_PAWN = PAWN | WHITE;
    public static final int BLACK_ROOK = ROOK | BLACK;
    public static final int BLACK_KNIGHT = KNIGHT | BLACK;
    public static final int BLACK_BISHOP = BISHOP | BLACK;
    public static final int BLACK_QUEEN = QUEEN | BLACK;
    public static final int BLACK_KING = KING | BLACK;
    public static final int BLACK_PAWN = PAWN | BLACK;
    public static final int EMPTY = 0;

    /**
     * Gets the color of a WHITE_ROOK/BLACK_BISHOP/etc
     * @param pieceType, the pieceType to get the color from
     * @return WHITE/BLACK
     */
    public static int getColor(int pieceType) {
        return pieceType & 1; // Get the last bit, which indicates the color
    }

    public static final int[][] INITIAL_MAILBOX = new int[][]{
            {WHITE_ROOK, WHITE_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, BLACK_PAWN, BLACK_ROOK},
            {WHITE_KNIGHT, WHITE_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, BLACK_PAWN, BLACK_KNIGHT},
            {WHITE_BISHOP, WHITE_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, BLACK_PAWN, BLACK_BISHOP},
            {WHITE_QUEEN, WHITE_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, BLACK_PAWN, BLACK_QUEEN},
            {WHITE_KING, WHITE_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, BLACK_PAWN, BLACK_KING},
            {WHITE_BISHOP, WHITE_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, BLACK_PAWN, BLACK_BISHOP},
            {WHITE_KNIGHT, WHITE_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, BLACK_PAWN, BLACK_KNIGHT},
            {WHITE_ROOK, WHITE_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, BLACK_PAWN, BLACK_ROOK},
    };

    public static final long FIRST_RANK = 0xff00000000000000L;
    public static final long SECOND_RANK = 0x00ff000000000000L;
    public static final long THIRD_RANK = 0x0000ff0000000000L;
    public static final long FOURTH_RANK = 0x000000ff00000000L;
    public static final long FIFTH_RANK = 0x00000000ff000000L;
    public static final long SIXTH_RANK = 0x0000000000ff0000L;
    public static final long SEVENTH_RANK = 0x000000000000ff00L;
    public static final long EIGHTH_RANK = 0x00000000000000ffL;
    public static final long FIRST_FILE = 0x8080808080808080L;
    public static final long SECOND_FILE = 0x4040404040404040L;
    public static final long THIRD_FILE = 0x2020202020202020L;
    public static final long FOURTH_FILE = 0x1010101010101010L;
    public static final long FIFTH_FILE = 0x0808080808080808L;
    public static final long SIXTH_FILE = 0x0404040404040404L;
    public static final long SEVENTH_FILE = 0x0202020202020202L;
    public static final long EIGHTH_FILE = 0x0101010101010101L;
    public static final long[] RANK_MASK = new long[]{FIRST_RANK, SECOND_RANK, THIRD_RANK, FOURTH_RANK,
            FIFTH_RANK, SIXTH_RANK, SEVENTH_RANK, EIGHTH_RANK};
    public static final long[] FILE_MASK = new long[]{FIRST_FILE, SECOND_FILE, THIRD_FILE, FOURTH_FILE,
            FIFTH_FILE, SIXTH_FILE, SEVENTH_FILE, EIGHTH_FILE};

    public static final long NOT_A_FILE = 0x7f7f7f7f7f7f7f7fL; // ~0x8080808080808080
    public static final long NOT_H_FILE = 0xfefefefefefefefeL; // ~0x0101010101010101

    public static final long[] DIAGONAL_MASK = new long[]{ //a diagonal (/) mask indexed by the square. All compile-time
                                                          //constants so no performance impact
            0x8040201008040201L, 0x4020100804020100L, 0x2010080402010000L, 0x1008040201000000L,
            0x0804020100000000L, 0x0402010000000000L, 0x0201000000000000L, 0x0100000000000000L,

            0x0080402010080402L, 0x8040201008040201L, 0x4020100804020100L, 0x2010080402010000L,
            0x1008040201000000L, 0x0804020100000000L, 0x0402010000000000L, 0x0201000000000000L,

            0x0000804020100804L, 0x0080402010080402L, 0x8040201008040201L, 0x4020100804020100L,
            0x2010080402010000L, 0x1008040201000000L, 0x0804020100000000L, 0x0402010000000000L,

            0x0000008040201008L, 0x0000804020100804L, 0x0080402010080402L, 0x8040201008040201L,
            0x4020100804020100L, 0x2010080402010000L, 0x1008040201000000L, 0x0804020100000000L,

            0x0000000080402010L, 0x0000008040201008L, 0x0000804020100804L, 0x0080402010080402L,
            0x8040201008040201L, 0x4020100804020100L, 0x2010080402010000L, 0x1008040201000000L,

            0x0000000000804020L, 0x0000000080402010L, 0x0000008040201008L, 0x0000804020100804L,
            0x0080402010080402L, 0x8040201008040201L, 0x4020100804020100L, 0x2010080402010000L,

            0x0000000000008040L, 0x0000000000804020L, 0x0000000080402010L, 0x0000008040201008L,
            0x0000804020100804L, 0x0080402010080402L, 0x8040201008040201L, 0x4020100804020100L,

            0x0000000000000080L, 0x0000000000008040L, 0x0000000000804020L, 0x0000000080402010L,
            0x0000008040201008L, 0x0000804020100804L, 0x0080402010080402L, 0x8040201008040201L,
    };

    public static final long[] ANTIDIAGONAL_MASK = new long[]{ //same as DIAGONAL_MASK, but now for the antidiagonals (\)
            0x8000000000000000L, 0x4080000000000000L, 0x2040800000000000L, 0x1020408000000000L,
            0x0810204080000000L, 0x0408102040800000L, 0x0204081020408000L, 0x0102040810204080L,

            0x4080000000000000L, 0x2040800000000000L, 0x1020408000000000L, 0x0810204080000000L,
            0x0408102040800000L, 0x0204081020408000L, 0x0102040810204080L, 0x0001020408102040L,

            0x2040800000000000L, 0x1020408000000000L, 0x0810204080000000L, 0x0408102040800000L,
            0x0204081020408000L, 0x0102040810204080L, 0x0001020408102040L, 0x0000010204081020L,

            0x1020408000000000L, 0x0810204080000000L, 0x0408102040800000L, 0x0204081020408000L,
            0x0102040810204080L, 0x0001020408102040L, 0x0000010204081020L, 0x0000000102040810L,

            0x0810204080000000L, 0x0408102040800000L, 0x0204081020408000L, 0x0102040810204080L,
            0x0001020408102040L, 0x0000010204081020L, 0x0000000102040810L, 0x0000000001020408L,

            0x0408102040800000L, 0x0204081020408000L, 0x0102040810204080L, 0x0001020408102040L,
            0x0000010204081020L, 0x0000000102040810L, 0x0000000001020408L, 0x0000000000010204L,

            0x0204081020408000L, 0x0102040810204080L, 0x0001020408102040L, 0x0000010204081020L,
            0x0000000102040810L, 0x0000000001020408L, 0x0000000000010204L, 0x0000000000000102L,

            0x0102040810204080L, 0x0001020408102040L, 0x0000010204081020L, 0x0000000102040810L,
            0x0000000001020408L, 0x0000000000010204L, 0x0000000000000102L, 1L
    };

    public static final long[] KNIGHT_ATTACKS = new long[] { //the attacks of a knight, indexed by square
            0x0020400000000000L, 0x0010a00000000000L, 0x0088500000000000L, 0x0044280000000000L,
            0x0022140000000000L, 0x00110a0000000000L, 0x0008050000000000L, 0x0004020000000000L,

            0x2000204000000000L, 0x100010a000000000L, 0x8800885000000000L, 0x4400442800000000L,
            0x2200221400000000L, 0x1100110a00000000L, 0x0800080500000000L, 0x0400040200000000L,

            0x4020002040000000L, 0xa0100010a0000000L, 0x5088008850000000L, 0x2844004428000000L,
            0x1422002214000000L, 0x0a1100110a000000L, 0x0508000805000000L, 0x0204000402000000L,

            0x0040200020400000L, 0x00a0100010a00000L, 0x0050880088500000L, 0x0028440044280000L,
            0x0014220022140000L, 0x000a1100110a0000L, 0x0005080008050000L, 0x0002040004020000L,

            0x0000402000204000L, 0x0000a0100010a000L, 0x0000508800885000L, 0x0000284400442800L,
            0x0000142200221400L, 0x00000a1100110a00L, 0x0000050800080500L, 0x0000020400040200L,

            0x0000004020002040L, 0x000000a0100010a0L, 0x0000005088008850L, 0x0000002844004428L,
            0x0000001422002214L, 0x0000000a1100110aL, 0x0000000508000805L, 0x0000000204000402L,

            0x0000000040200020L, 0x00000000a0100010L, 0x0000000050880088L, 0x0000000028440044L,
            0x0000000014220022L, 0x000000000a110011L, 0x0000000005080008L, 0x0000000002040004L,

            0x0000000000402000L, 0x0000000000a01000L, 0x0000000000508800L, 0x0000000000284400L,
            0x0000000000142200L, 0x00000000000a1100L, 0x0000000000050800L, 0x0000000000020400L,
    };

    public static final long[] KING_ATTACKS = new long[] { //the attacks of a king, 0 indexed by the square
            0x40c0000000000000L, 0xa0e0000000000000L, 0x5070000000000000L, 0x2838000000000000L,
            0x141c000000000000L, 0x0a0e000000000000L, 0x0507000000000000L, 0x0203000000000000L,

            0xc040c00000000000L, 0xe0a0e00000000000L, 0x7050700000000000L, 0x3828380000000000L,
            0x1c141c0000000000L, 0x0e0a0e0000000000L, 0x0705070000000000L, 0x0302030000000000L,

            0x00c040c000000000L, 0x00e0a0e000000000L, 0x0070507000000000L, 0x0038283800000000L,
            0x001c141c00000000L, 0x000e0a0e00000000L, 0x0007050700000000L, 0x0003020300000000L,

            0x0000c040c0000000L, 0x0000e0a0e0000000L, 0x0000705070000000L, 0x0000382838000000L,
            0x00001c141c000000L, 0x00000e0a0e000000L, 0x0000070507000000L, 0x0000030203000000L,

            0x000000c040c00000L, 0x000000e0a0e00000L, 0x0000007050700000L, 0x0000003828380000L,
            0x0000001c141c0000L, 0x0000000e0a0e0000L, 0x0000000705070000L, 0x0000000302030000L,

            0x00000000c040c000L, 0x00000000e0a0e000L, 0x0000000070507000L, 0x0000000038283800L,
            0x000000001c141c00L, 0x000000000e0a0e00L, 0x0000000007050700L, 0x0000000003020300L,

            0x0000000000c040c0L, 0x0000000000e0a0e0L, 0x0000000000705070L, 0x0000000000382838L,
            0x00000000001c141cL, 0x00000000000e0a0eL, 0x0000000000070507L, 0x0000000000030203L,

            0x000000000000c040L, 0x000000000000e0a0L, 0x0000000000007050L, 0x0000000000003828L,
            0x0000000000001c14L, 0x0000000000000e0aL, 0x0000000000000705L, 0x0000000000000302L
    };
}
