package state;

/**
 * A class containing a move. FIELDS ARE 1 INDEXED!
 */
public class Move {
    private int move = 0; // a binary enoding of the move.
    // The first 4 bits are 0
    // The next 8 bits encode the from field (a1 = 1, a2 = 2 ... h8 = 64)
    // The next 8 bits encode the to field
    // The next 4 bits encode the moving piece according to Constants ints (max 12)
    // The next 4 bits encode the captured piece according to Constants ints (max 12) Constants.EMPTY if none captured
    // The next 4 bits encode special moves according to the following scheme:


//      code	promotion   capture	special 1	special 0	kind of move
//      0	    0	        0       0   	    0           quiet moves
//      1   	0   	    0   	0       	1       	double pawn push
//      2	    0	        0	    1	        0	        king castle
//      3	    0	        0	    1	        1	        queen castle
//      4	    0	        1	    0	        0	        captures
//      5	    0	        1	    0	        1	        en-passent-capture
//      8	    1	        0	    0	        0	        knight-promotion
//      9	    1	        0	    0	        1	        bishop-promotion
//      10  	1       	0   	1       	0       	rook-promotion
//      11  	1       	0   	1       	1       	queen-promotion
//      12  	1       	1   	0       	0       	knight-promo capture
//      13  	1       	1   	0       	1       	bishop-promo capture
//      14  	1       	1   	1       	0       	rook-promo capture
//      15  	1       	1   	1       	1       	queen-promo capture

    public Move(int from, int to, int movingPiece, int capturedPiece, int flags) {
        this.move = (from & 0xff) << 20 | (to & 0xff) << 12 |
                (movingPiece & 0xf) << 8 | (capturedPiece & 0xf) << 4| (flags & 0xf);
    }

    public Move(int move) {
        this.move = move;
    }

    public int getTo() {
        return (this.move & 0xff000) >>> 12;
    }

    public int getFrom() {
        return (this.move & 0xff00000) >>> 20;
    }

    public int getMovingPiece() {
        return (this.move & 0xf00) >>> 8;
    }

    public int getCapturedPiece() {
        return (this.move & 0xf0) >>> 4;
    }

    /**
     * WATCH OUT, DOES NOT CONTAIN THE COLOR. THIS SHOULD BE RETRIEVED FROM THE MOVINGPIECE
     * @return
     */
    public int getPromotionPiece() {
        int flags = getFlags();
        switch(flags) {
            case 8:
            case 12:
                return Constants.KNIGHT;
            case 9:
            case 13:
                return Constants.BISHOP;
            case 10:
            case 14:
                return Constants.ROOK;
            case 11:
            case 15:
                return Constants.QUEEN;
            default:
                return Constants.EMPTY;
        }
    }

    public int getFlags() {
        return this.move & 0xf;
    }

    public boolean isPromotion() {
        return (this.getFlags() & 0b1000) != 0;
    }

    public boolean isCapture() {
        return (this.getFlags() & 0b0100) != 0;
    }

    public boolean isEnPassent() {
        return this.getFlags() == 5;
    }

    public boolean isKingCastle() {
        return this.getFlags() == 2;
    }

    public boolean isQueenCastle() {
        return this.getFlags() == 3;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        int x = getFrom() % 8;
        x = x == 0 ? 8 : x; //8 % 8 will be 0, but we want 8
        int y = (getFrom() / 8) + 1;
        if (x == 8) y--;
        char xChar = (char) (x + 96); // 97 is a, 104 is h
        s.append(xChar);
        s.append(y);

        x = getTo() % 8;
        x = x == 0 ? 8 : x; //8 % 8 will be 0, but we want 8
        y = (getTo() / 8) + 1;
        if (x == 8) y--;
        xChar = (char) (x + 96); // 97 is a, 104 is h
        s.append(xChar);
        s.append(y);
        return s.toString();
    }

    public Move copy() {
        Move clone = new Move(this.move);
        clone.move = this.move;
        return clone;
    }

//    @Override
//    public String toString() {
//        return "from: " + getFrom() +
//                " to: " + getTo() +
//                " movingPiece: " + getMovingPiece() +
//                " flags: " + getFlags();
//    }
}
